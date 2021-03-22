package lava3.db;

/**
* An Item is either a class, object, int array, String array or object array.
* There could be other types of arrays as well, but that is all we have for now
* These share certain similarities. They all have elements or attributes
*/
public class Item implements Row {
	public static byte T_CLASS = (byte)7;		//from CONSTANT_Class value
	public static byte T_OBJECT = (byte)20;		//arbitrary
	public static byte T_INT_ARRAY = (byte)21;	//arbitrary
	public static byte T_STR_ARRAY = (byte)22;	//arbitrary
	public static byte T_OBJ_ARRAY = (byte)23;	//arbitrary
	public static String OBJ_CLASS = "java/lang/Object;";
	public static String INT_ARRAY_CLASS = "[I";
	public static String OBJ_ARRAY_CLASS = "[Ljava/lang/Object;";
	public static String STR_ARRAY_CLASS = "[Ljava/lang/String;";
	public static String[] columns = new String[] {"id","type","class_name",
		"class_id","length","static_obj_id"};

	//fields
	//this are all final except for id
	protected char id;					//primary key.  This is 0 until it is inserted
	public final byte type;			//one of the 4 above
	public final String class_name;	//if this is a class, or null otherwise
	public final char class_id;		//for object or array, the type of object or array
	public final byte length;		//for array. We don't want big arrays

	/**
	* Constructor for a class
	*/
	public Item(String class_name) {
		if (class_name==null) throw new IllegalArgumentException("class_name may not be null");
		type = T_CLASS;
	 	this.class_name = class_name;
	 	//class_id is only for the use of objects and arrays
	 	class_id = (char)0;
	 	length = (byte)0;
	}

	/**
	* Constructor for an object.  Pass in the class_id
	*/
	public Item(char class_id) {
		if (class_id==0) throw new IllegalArgumentException("class_id may not be zero");
		type = T_OBJECT;
	 	class_name = null;
	 	this.class_id = class_id;
	 	length = (byte)0;
	}

	/**
	* Constructor for an array
	*/
	public Item(byte t,char cid,byte len) {
		if (t==0 || cid==0) throw new IllegalArgumentException("type or class_id may not be zero");
		if (len>127) throw new IllegalArgumentException(len+" is out of bounds");
		type = t;
	 	class_name = null;
	 	this.class_id = cid;
	 	length = len;
	}

	protected void setId(char id) {this.id=id;}

	public char getId() {return id;}

	//this is 0 until it is inserted
	public int getRowId() { return (int)id;}

	//only for arrays
	public int getLength() {return (int)length;}

	//get the names of the columns.  The order doesn't matter
	public static String[] columns() {return columns;}

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public static String typeof(String column) {
		switch(column) {
			case "id": return "integer";
			case "type": return "integer";
			case "class_name": return "text";
			case "class_id": return "integer";
			case "length": return "integer";
			default: return "null";
		}
	}
}