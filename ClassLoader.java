package lava3;
import java.io.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.Type;
import lava3.*;
import lava3.db.*;

/**
* This loads one class at a time.
* All the names, which are field and method names are stored in the name table,
* and associated with the class in the ItemIndex table.
* The main method is assigned its own name and stored in slot 0 for the class in the ItemIndex table.
*
* External classes, fields and methods are saved in the Item and Name tables.
*/

public class ClassLoader {
	//public final static byte CONSTANT_Fieldref = (byte)9;
	//public final static byte CONSTANT_Methodref = (byte)10;
	public static String MAIN = "main:([Ljava/lang/String;)V";
	public static String CLINIT = "<clinit>:()V";
	public static String INIT= "<init>:()V";

	Database db;
	boolean debug;
	String dir;

	//for testing
	public static void main(String[] args) throws IOException {
		ClassLoader loader = new ClassLoader(true);
		loader.loadClass(args[0]);
	}

	public ClassLoader(boolean debug) {
		db = Database.getInstance();
		this.debug=debug;
		//get dir
		dir = System.getProperty("dir");
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	//replace the periods with slashes
	public static String canonicalName(String className) {
		return className.replace('.', '/');
	}

	/**
	* loadClass.  pass in the class name, without the .class extension.
	* This must either be in the same directory, or relative to a directory specified
	* by the -Ddir system property.
	*/
	public void loadClass(String classFileName) throws IOException {
		String cf = null;
		if (dir!=null) {
			cf = dir + canonicalName(classFileName) + ".class";
		} else {
			cf = canonicalName(classFileName) + ".class";
		}

		log("loading class file "+cf);
		ClassParser classp = new ClassParser(cf);
		JavaClass jclass = null;
		try {
			jclass = classp.parse();
		} catch(ClassFormatException x) {
			throw new IOException("ClassFormatException: "+x.getMessage());
		}
		//get the classname
		String jcname = jclass.getClassName();
		String jcname2 = canonicalName(jcname);
		char cid=db.newClass(jcname2);
		log("stored class "+jcname2+" as "+(int)cid);

		//register the main method name
		String mainName = jcname2 + ":" + MAIN;
		char nid=db.newName(Name.T_METHOD,cid,mainName);
		log("stored name "+mainName+" as "+(int)nid);

		String clinitName = jcname2 + ":" + CLINIT;
		char nid2=db.newName(Name.T_METHOD,cid,clinitName);
		log("stored name "+clinitName+" as "+(int)nid2);

		//load constantpool
		loadConstantPool(jcname2,cid,jclass.getConstantPool());

		//load methods
		loadMethods(jcname2,cid,jclass);

		//register native names
		//add all of the native names
		Native.registerNames();

		//dump what we have
		db.itemTable.dump();
		db.nameTable.dump();
		db.methodTable.dump(db);
		db.itemIndexTable.dump();

	}

	public void loadConstantPool(String claz,char cid,ConstantPool cpool) {
		String str = null;
		int class_index = 0;
		int natx = 0;
		String full_name = null;

		//start at 1
		for (int ix=1;ix<cpool.getLength();ix++) {
			Constant k = cpool.getConstant(ix);
			if (k==null) continue;
			byte tag=k.getTag();
			switch(tag) {
				case 3:		//int
					//store this as a class constant
					ConstantInteger ci = (ConstantInteger)k;
					int a = ci.getBytes();
					Cell celli = new Cell(a);
					//store it in the ItemIndexTable
					db.store(cid,(char)ix,celli);
					break;
				case 4:		//float
					//ConstantFloat cf = (ConstantFloat)k;
					//float f = cf.getBytes();
					log(ix+": float - skipping");
					break;
				case 7:		//class
					ConstantClass cc=(ConstantClass)k;
					str=cc.getBytes(cpool);
					//this should have the slashes in it
					char ncid=db.newClass(str);
					db.store(cid,(char)ix,new Cell(ncid));
					break;
				case 8:		//string
					ConstantString cs=(ConstantString)k;
					str=cs.getBytes(cpool);
					Cell cs2 = new Cell(str);
					db.store(cid,(char)ix,cs2);
					break;
				case 9:		//field
					ConstantFieldref cfr = (ConstantFieldref)k;
					class_index = cfr.getClassIndex();
					natx = cfr.getNameAndTypeIndex();
					full_name = getFullName(cpool,class_index,natx);
					//1. assign a CID to the fieldname class, if it doesn't already exist
					ConstantClass myClass=(ConstantClass)cpool.getConstant(class_index);
					String fcname=myClass.getBytes(cpool);
					char nfcid=db.newClass(fcname);
					//2. create a name
					char nid = db.newName(Name.T_FIELD,nfcid,full_name);
					//3. put it in the pool
					Cell cnid = new Cell(nid);
					db.store(cid,(char)ix,cnid);
					break;
				case 10:	//methodref
					ConstantMethodref cmr = (ConstantMethodref)k;
					class_index = cmr.getClassIndex();
					natx = cmr.getNameAndTypeIndex();
					full_name = getFullName(cpool,class_index,natx);
					//1. assign the cid, if it doesn't already exist
					//1. assign a CID to the fieldname class, if it doesn't already exist
					ConstantClass myClass2=(ConstantClass)cpool.getConstant(class_index);
					String mcname=myClass2.getBytes(cpool);
					char nmcid=db.newClass(mcname);
					//2. create a name
					char nid2 = db.newName(Name.T_METHOD,nmcid,full_name);
					//3. put it in the pool
					Cell cnid2 = new Cell(nid2);
					db.store(cid,(char)ix,cnid2);
					break;
				case 11:	//ifacemethodref
					//store method name
					//ConstantInterfaceMethodref cimr = (ConstantInterfaceMethodref)k;
					//ignore for now
					log(ix+": iface method ref - skipping");
					break;
				case 1: //log(ix+": utf8");
					break;
				case 12: //log(ix+": cnat");
					break;
				default: log(ix+": "+tag);
			}
		}
	}

	public String getFullName(ConstantPool cpool,int cx,int natx) {
		//get the class of the reference - it might be this one or could be different
		ConstantClass myClass=(ConstantClass)cpool.getConstant(cx);
		String cname=myClass.getBytes(cpool);
		ConstantNameAndType myCnat=(ConstantNameAndType)cpool.getConstant(natx);
		String fname=myCnat.getName(cpool);
		String fsig=myCnat.getSignature(cpool);
		String complete_name = cname + ":" + fname + ":" + fsig;
		return complete_name;
	}

	//-------------------------------------------

	//claz is the class name with slashes
	public void loadMethods(String claz,char cid,JavaClass jclass) {
		Method[] ma = jclass.getMethods();
		for (int i=0;i<ma.length;i++) {
			Method m = ma[i];

			//get the name
			String mname = m.getName();

			//get the descriptor. BCEL calls it the signature
			String sig = m.getSignature();
			//similar to getFullName method above
			String complete_name = claz + ":" +mname + ":"+sig;
			log("creating method "+complete_name);

			//all we need is the name id
			char nid = db.getNameId(complete_name);
			if (nid==0) {
				nid = db.newName(Name.T_METHOD,cid,complete_name);
				log("name "+complete_name+" added as "+(int)nid);
			}

			boolean isStatic = m.isStatic();
			int params = m.getArgumentTypes().length;

			//---------------
			//init has 1 param
			if (mname.equals("<init>")) {
				params = 1;
			}
			//--------------
			log(complete_name+" has "+params+" params");
			byte[] mcode = m.getCode().getCode();

			//now store it in the method table
			MethodRow mr = new MethodRow(nid, cid,isStatic,(byte)params,mcode);
			char mid = db.newMethod( (MethodInfo)mr);
		}
	}
}
