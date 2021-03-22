package lava3.db;
import lava3.Cell;
/**
* A Name looks like: java/lang/System:out:java/lang/PrintStream
* I use a colon to join the parts.
*
* A Name is an adjective. It describes the noun Item in some way.
* Any Name can be theoretically connected with any Item.
*
* The id for a name begins at 256.
*
* The is_static field is not really needed but it could be helpful in debugging.
* This is true if the jvm field or method has the static attribute.
* For static fields, the static object is stored in the class pool item 0
* For static methods, the method code already figures this out because
* static methods are not passed in the object ref.
*/
public class Name implements Row {
	public static byte T_FIELD = (byte)9;
	public static byte T_METHOD = (byte)10;
	public static byte T_IFACE_METHOD = (byte)11;
	public static String[] columns = new String[] {"id","type","class_id",
		"jname","is_static","static_value"};

	//fields
	protected char id;			//primary key
	public final byte type;
	public final char class_id;
	public final String jname;
	//I am changing is_static to not be final because we don't always know if it is static
	public boolean is_static;
	public Cell static_value;
	//this could also have a static value

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public static String typeof(String column) {
		switch(column) {
			case "id": return "integer";
			case "type": return "integer";
			case "class_id": return "integer";
			case "jname": return "text";
			case "is_static": return "integer";	//boolean are considered integers
			case "static_value": return "text";	//cell is considered text
			default: return "null";
		}
	}

	public Name(byte t, char cid, String n) {
		type=t;
		class_id=cid;
		jname=n;
	}

	public char getId() {return id;}

	public int getRowId() {return (int)id;}

	public boolean isStatic() {return is_static;}

	public void setStaticValue(Cell v) {
		is_static = true;
		static_value = v;
	}

	public Cell getStaticValue() { return static_value;}
}