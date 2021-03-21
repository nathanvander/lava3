package lava3;
import lava3.db.*;

/**
* This emulates native code.
*/
public class Native {
	public final static String PARSEINT="java/lang/Integer:parseInt:(Ljava/lang/String;)I";
	public final static String PRINTLN_I="java/io/PrintStream:println:(I)V";
	public final static String PRINTLN="java/io/PrintStream:println:(Ljava/lang/String;)V";
	public final static String SB_INIT="java/lang/StringBuilder:<init>:()V";
	public final static String SB_APPEND="java/lang/StringBuilder:append:(Ljava/lang/String;)Ljava/lang/StringBuilder;";
	public final static String SB_APPEND_I="java/lang/StringBuilder:append:(I)Ljava/lang/StringBuilder;";
	public final static String SB2S="java/lang/StringBuilder:toString:()Ljava/lang/String;";
	public final static String OBJ_INIT="java/lang/Object:<init>:()V";
	public final static String SYS_OUT = "java/lang/System:out:Ljava/io/PrintStream;";

	public static void log(boolean debug,String s) {
		if (debug) System.out.println(s);
	}

	/**
	* registerNames(). If you do this more than once by mistake, that's ok, because each
	* new class or new name checks to see if it already exists in the database.
	*/
	public static void registerNames() {
		Database db = Database.getInstance();
		char cid =db.newClass("java/lang/Object");
		db.newName(Name.T_METHOD,cid,OBJ_INIT);

		cid=db.newClass("java/lang/Integer");
		char nid=db.newName(Name.T_METHOD,cid,PARSEINT);

		cid=db.newClass("java/io/PrintStream");
		db.newName(Name.T_METHOD,cid,PRINTLN_I);
		db.newName(Name.T_METHOD,cid,PRINTLN);

		cid=db.newClass("java/lang/StringBuilder");
		db.newName(Name.T_METHOD,cid,SB_INIT);
		db.newName(Name.T_METHOD,cid,SB_APPEND);
		db.newName(Name.T_METHOD,cid,SB_APPEND_I);
		db.newName(Name.T_METHOD,cid,SB2S);

		//we need to create a static object for System.out
		//get the nid.  It should already exist
		char outid = db.getNameId(SYS_OUT);
		if (outid==0) {
			log(true,"expecting "+SYS_OUT+" to already exist");
		} else {
			//assign the static value to be the class 'java/io/PrintStream'
			//that doesn't make any sense - it should be an object of that type instead
			//but it is just a placeholder
			char jipid = db.getClassId("java/io/PrintStream");
			db.putStatic(outid,new Cell(jipid));
			log(true,"set static value for "+(int)outid+" to "+(int)jipid);
		}
	}

	//should I pass in a debug?
	public static boolean emulate(char mid,BlackBox box) {
		Database db = Database.getInstance();
		Name nm = (Name)db.nameTable.read(mid);
		String methodName = nm.jname;

		if (methodName.equals(PARSEINT)) {
			PARSEINT(box);
			return true;
		} else if (methodName.equals(PRINTLN_I)) {
			PRINTLN_I(box);
			return true;
		} else if (methodName.equals(PRINTLN)) {
			PRINTLN(box);
			return true;
		} else if (methodName.equals(SB_INIT)) {
			SB_INIT(box);
			return true;
		} else if (methodName.equals(SB_APPEND)) {
			SB_APPEND(box,db);
			return true;
		} else if (methodName.equals(SB_APPEND_I)) {
			SB_APPEND_I(box,db);
			return true;
		} else if (methodName.equals(SB2S)) {
			SB2S(box,db);
			return true;
		} else if (methodName.equals(OBJ_INIT)) {
			Object_init(box,db);
			return true;
		} else {
			log(true,"unable to emulate '"+methodName+"'");
			return false;
		}
	}

	//this is the emulation of static
	//"java/lang/Integer.parseInt:(Ljava/lang/String;)I"
	//public static void static_Integer_parseInt_String_I(BlackBox box) {
	public static void PARSEINT(BlackBox box) {
		//log(true, "emulating PARSEINT");
		//log(true,box.dumpStack());
		Cell c = box.POP();
		String str = c.toString().trim();
		//----------------------------------
		//this is the native code
		int i=java.lang.Integer.parseInt(str);
		//----------------------------------
		box.IPUSH(i);
	}

	//"java/io/PrintStream.println:(I)V";
	public static void PRINTLN_I (BlackBox box) {
		int ival = box.IPOP();			//int ref
		Cell oref = box.POP();		//java.lang.System.out:PrintStream object
		//----------------------------------
		//this is the native code
		System.out.println(ival);
		//----------------------------------
	}

	//this is the emulation of "java/io/PrintStream.println:(Ljava/lang/String;)V"
	public static void PRINTLN(BlackBox box) {
		Cell sval = box.POP();		//string
		Cell oref = box.POP();		//java.lang.System.out:PrintStream object
		//----------------------------------
		//this is the native code
		System.out.println(sval.toString());
		//----------------------------------
	}

	/**
	* SB_INIT. This is short for java/lang/StringBuilder:<init>:()V
	* The java code shows that this creates a AbstractStringBuilder with an initial capacity of 16.
	* Input/Output.  The oref is passed in on the stack, and nothing is passed back.
	*/
	public static void SB_INIT(BlackBox box) {
		//just use Cell.append to make this work
		Cell oref = box.POP();
		//we just consumed an oref and we don't need to put anything back on the stack
		//we are going to store a string in slot 1
	}

	/**
	* SB_APPEND.  Ths is short for StringBuilder:append:(Ljava/lang/String;)
	* This passes is 2 argument on the stack, oref and (String) sval.
	* This puts the oref back on the stack.
	*/
	public static void SB_APPEND(BlackBox box,Database db) {
		//pop the string to append off the stack
		Cell sval = box.POP();
		//get the stringbuilder object
		Cell oref = box.POP();
		//get the aref, which is in slot 1 of the stringbuilder object
		Cell xs = db.arrayLoad(oref.toChar(),(char)1);

		if (xs==null) {
			db.arrayStore(oref.toChar(),(char)1,sval);
		} else {
			xs.append(sval);
			//we don't need to restore this
		}

		//return the oref
		box.PUSH(oref);
	}

	/**
	* SB_APPEND_1. This is short for java/lang/StringBuilder:append:(I)Ljava/lang/StringBuilder;
	* This int is stored *AS AN INT* on the stack. We convert the int *TO A STRING IN BASE 10*
	* and append it.
	*/
	public static void SB_APPEND_I(BlackBox box,Database db) {
		//pop the int to append off the stack
		Cell ival = box.POP();
		//convert it to a String
		//--------------------------
		//native code.  Convert the int to a string in base-10
		String sval = java.lang.String.valueOf(ival.toInt());
		//----------------------------
		Cell c = new Cell(sval);
		//push it back on the stack
		box.PUSH(c);
		//now the other method can handle this
		SB_APPEND(box,db);
	}

	/**
	* SB2S.  This is short for java/lang/StringBuilder.toString:()Ljava/lang/String;
	* Input/Output.  The oref is passed in on the stack, and the string value is passed back.
	*/
	public static void SB2S(BlackBox box,Database db) {
		Cell oref = box.POP();
		Cell xs = db.arrayLoad(oref.toChar(),(char)1);
		box.PUSH(xs);
	}

	/**
	* Object_init().  This is short for "java/lang/Object.<init>:()V"
	* There is nothing to do.  In my system, an object is just a number.
	*/
	public static void Object_init(BlackBox box,Database db) {
		Cell oref = box.POP();
		if (oref==null) {log(true,"Native.Object_init: oref is null");}
		Item it = (Item)db.itemTable.read(oref.toChar());
		char cid = it.class_id;
		String objClass = db.itemTable.getClassName(cid);
		log(true,"and God blessed his child "+(int)oref.toChar()+" from the tribe of "+objClass+" and told him to live long and prosper");
		//return void

	}
}