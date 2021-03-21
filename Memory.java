package lava3;

/**
* This list methods that we expect memory to give us.
*/
public interface Memory {
	/**
	* Get the classId of the class, which is a number from
	* 1..65535.  A 0 means the class does not exist
	*
	* The className may be in one of these formats:
	*	java.lang.String - with periods
	*	java/lang/String - with slashes. This is the "canonical" format
	*	Ljava/lang/String;
	*
	* Arrays also have classes and these will look like
	*	[I - for array
	* 	[Ljava/lang/String;
	*
	* Primitive types do not have classes.
	*/
	public char getClassId(String className);

	/**
	* Add a new class name to the memory.
	* This only assigns a class id to it and does not physically load the class.
	* We may need to refer to a class before it is loaded, or to a system class.
	*
	* This must be the canonical form, which is like java/lang/String
	*/
	public char newClass(String jClassName);

	/**
	* Create a new object of the given class. This doesn't initialize it,
	* it just assigns the number
	*/
	public char newObject(char classId);

	/**
	* Allocate a new int array.  Length is limited to 127
	* This is used for any primitive array type.
	* See JVM opcode newarray
	*/
	public char newIntArray(byte length);

	/**
	* See JVM anewarray
	*/
	public char newObjectArray(char classId,byte length);

	public char newStringArray(byte length);

	/**
	* Get the length of any array.
	* see arraylength
	*/
	public byte getLength(char arrayid);

	/**
	* a Name is a fully qualified name such as java/lang/System:out:java/io/PrintStream.
	* A name has a different numbering system than a class or object.
	*/
	public char newName(byte type,char classId,String name);

	/**
	* For use by the class loader only.
	* store a new method.  Return the id, which is the nameid
	*/
	public char newMethod(MethodInfo m);

	public MethodInfo getMethod(char nameId);

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
	public int store(char item,char x,Cell value);

	public Cell load(char item,char x);

	/**
	* put a value in a field.
	* Note that this has the same signature as store above
	*/
	public void putField(char objId,char fieldId,Cell value);

	/**
	* PutStatic assumes there is a global variable with the name value.
	* That's not the way I do it.  I require these steps:
	*	1. given the name id, get the class id, which is stored with it in the name table
	*	2. get the static object, which is stored in slot zero with the class id
	*	3. put the value, using the putField method above.
	* The field of course must be static
	*/
	public void putStatic(char fieldId,Cell value);

	public Cell getField(char objId,char fieldId);

	public Cell getStatic(char fieldId);

	/**
	* aastore
	* iastore
	* Note the familiar method signature
	*/
	public void arrayStore(char arrayId,char index,Cell value);

	/**
	* aaload
	* iaload
	* Note the familiar method signature
	*/
	public Cell arrayLoad(char arrayId,char index);

}