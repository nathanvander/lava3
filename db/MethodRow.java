package lava3.db;
import lava3.MethodInfo;

/**
* A Method is more than just code.  It has the method name and params.
* I could add more fields too if needed, like return type
* The primary key here is the nameId, so this can be seen as an extension of the Name table.
*
* I renamed this MethodRow to distinguish it from org.apache.bcel.classfile.Method
*/
public class MethodRow implements Row,MethodInfo {
	public static String[] columns=new String[] {"id","class_id","is_static","params","code"};

	//fields
	public final char id;				//this is the same as the name_id
	public final char class_id;			//same as in name table
	public final boolean is_static;		//same as in name table
	public final byte params;
	public final byte[] code;

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public static String typeof(String column) {
		switch(column) {
			case "id": return "integer";
			case "class_id": return "integer";
			case "is_static": return "integer";
			case "params": return "integer";
			case "code": return "blob";
			default: return "null";
		}
	}

	public MethodRow(char id,char class_id,boolean is_static,byte params,byte[] code) {
		this.id=id;
		this.class_id=class_id;
		this.is_static=is_static;
		this.params=params;
		this.code=code;
	}

	public int getRowId() {return (int)id;}

	/**
	* Get the class id this method is a part of
	*/
	public char getClassId() {return class_id;}

	/**
	* get the name id of this method
	*/
	public char getNameId() {return id;}

	/**
	* get the number of params. Params must be in the range 0..3
	* We know the classid because it is associated with the name
	*/
	public byte params() {return params;}

	public boolean isStatic() {return is_static;}

	/**
	* Get the byte code. This is the same code that is stored in the classfile, I don't
	* prepend the number of params to it.
	*/
	public byte[] getCode() {return code;}
}