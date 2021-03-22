package lava3.db;
import lava3.Memory;
import lava3.MethodInfo;
import lava3.Cell;

/**
* This emulates an Sqlite database.  I may use one later.
*/
public class Database implements Memory {
	private static Database instance;

	public static Database getInstance() {
		if (instance==null) {
			instance = new Database();
		}
		return instance;
	}

	public ItemTable itemTable;
	public NameTable nameTable;
	public MethodTable methodTable;
	public ItemIndexTable itemIndexTable;

	private String[] tableNames;

	private Database() {
		itemTable = new ItemTable();
		nameTable = new NameTable();
		methodTable = new MethodTable();
		itemIndexTable = new ItemIndexTable();
		tableNames = new String[]{ itemTable.getTableName(), nameTable.getTableName(),
			methodTable.getTableName(), itemIndexTable.getTableName()};
	}

	/**
	* Get a list of all table names
	*/
	public String[] getTableNames() {return tableNames;}

	public char getClassId(String className) {
		return itemTable.getClassId(className);
	}

	/**
	* Add a new class name to the memory.
	* This only assigns a class id to it and does not physically load the class.
	* We may need to refer to a class before it is loaded, or to a system class.
	*
	* This must be the canonical form, which is like java/lang/String
	*/
	public char newClass(String jClassName) {
		//first see if it already exists
		char cid = itemTable.getClassId(jClassName);
		if (cid>0) {
			return cid;
		} else {
			Item it = new Item(jClassName);
			return (char)itemTable.insert( it);
		}
	}

	/**
	* Create a new object of the given class. This doesn't initialize it,
	* it just assigns the number
	*/
	public char newObject(char classId) {
		Item obj = new Item(classId);
		return (char)itemTable.insert(obj);
	}

	/**
	* Allocate a new int array.  Length is limited to 127
	* This is used for any primitive array type.
	* See JVM opcode newarray
	*/
	public char newIntArray(byte length) {
 		return itemTable.newIntArray(length);
	}

	/**
	* See JVM anewarray
	*/
	public char newObjectArray(char classId,byte length) {
		//the classId here is the class of each item.  Just ignore it for now
		return itemTable.newObjectArray(length);
	}

	public char newStringArray(byte length) {
		return itemTable.newStringArray(length);
	}

	/**
	* Get the length of any array.
	* see arraylength
	*/
	public byte getLength(char arrayid) {
		Item a = (Item)itemTable.read(arrayid);
		return a.length;
	}

	/**
	* a Name is a fully qualified name such as java/lang/System:out:java/io/PrintStream.
	* A name has a different numbering system than a class or object.
	*/
	public char newName(byte type,char classId,String jname) {
		//see if it already exists
		char nid = nameTable.getNameId(jname);
		if (nid>0) {
			return nid;
		} else {
			Name n=new Name(type,classId,jname);
			return (char)nameTable.insert(n);
		}
	}

	public char getNameId(String name) {
		return nameTable.getNameId(name);
	}

	/**
	* For use by the class loader only.
	* store a new method.  Return the id, which is the nameid
	* The name needs to be registered first
	*/
	public char newMethod(MethodInfo m) {
		return (char)methodTable.insert((Row)m);
	}

	//this might return null, check it
	public MethodInfo getMethod(char nameId) {
		return (MethodInfo)methodTable.read(nameId);
	}

	/**
	* Store value. This has multiple uses:
	*	1. store a constant (int or string) in a constant pool
	*  	2. store a name in a constant pool, including a method name
	*	3. store a static object associated with a class.  This is stored
	*		in slot 0
	*	4. store a static value
	*	5. store a value in an object field
	*	6. store a value in an array
	*
	* The return value is the theoretical position in memory. It is calculated by
	* 65536 * itemid + x. Usually the x can never be zero, but if the itemid is
	* a class, then the static object associated with the class is in slot 0.
	*/
	public int store(char item,char x,Cell value) {
		return itemIndexTable.insert(item,x,value);
	}

	public Cell load(char item,char x) {
		return (Cell)itemIndexTable.read(item,x);
	}

	/**
	* put a value in a field.
	* Note that this has the same signature as store above
	*/
	public void putField(char objId,char fieldId,Cell value) {
		itemIndexTable.insert(objId,fieldId,value);
	}

	/**
	* Static fields are stored in the static_value of the Name object
	*/
	public void putStatic(char fieldId,Cell value) {
		//get the Name object
		Name na = (Name)nameTable.read(fieldId);
		na.setStaticValue(value);
	}

	public Cell getField(char objId,char fieldId) {
		return (Cell)itemIndexTable.read(objId,fieldId);
	}

	public Cell getStatic(char fieldId) {
		Name na = (Name)nameTable.read(fieldId);
		return na.getStaticValue();
	}

	/**
	* aastore
	* iastore
	* Note the familiar method signature
	*/
	public void arrayStore(char arrayId,char index,Cell value) {
		store(arrayId,index,value);
	}

	/**
	* aaload
	* iaload
	* Note the familiar method signature
	*/
	public Cell arrayLoad(char arrayId,char index) {
		return load(arrayId,index);
	}

}