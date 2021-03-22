package lava3.db;
import java.util.*;
/**
* This is the table itself.  This is implements as an array of Item, with an index
* for classnames
*/
public class ItemTable implements Table {
	public final static int CAPACITY = 2048;	//arbitrary limit
	private char counter=1;		//start counting at 1

	//the 0th element is null
	Item[] items = new Item[CAPACITY];
	HashMap<String,Character> classNameIndex = new HashMap<String,Character>();

	public ItemTable() {}

	public String getTableName() {return "item";}

	//get the names of the columns.  The order doesn't matter
	public String[] columns() {return Item.columns;}

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public String typeof(String column) {return Item.typeof(column);}

	/**
	* Return the sql code that would create the table.
	*/
	public String createTableSql() {
		return "CREATE TABLE IF NOT EXISTS item ( "
			+"id INTEGER PRIMARY KEY, "
			+"type INTEGER NOT NULL, "
			+"class_name TEXT, "
			+"class_id INTEGER, "
			+"length INTEGER) ";

			//+"static_obj_id INTEGER)";
	}

	public int insert(Row data) {
		char cid = counter++;
		Item it = (Item)data;
		it.id=cid;			//should this be setid?
		items[cid]=it;		//insert it
		if (it.type == Item.T_CLASS) {
			classNameIndex.put(it.class_name,Character.valueOf(cid));
		}
		return (int)cid;
	}

	public Row read(int rowid) {
		return items[rowid];
	}

	public void update(int rowid,Row update) {
		items[rowid]=(Item)update;
	}

	//for this purpose, we count row 0, even though it is null
	public int rows() {return counter-1;}

	//a 0 means invalid
	public char getClassId(String className) {
		Character cc= classNameIndex.get(className);
		if (cc==null) {
			return (char)0;
		} else {
			return cc.charValue();
		}
	}

	public String getClassName(char cid) {
		Item it = items[cid];
		return it.class_name;
	}

	//convenience method for inserting a class
	//class name will be like java/lang/String
	//it must not start with L or end with ;
	public char newClass(String jClassName) {
		if (jClassName==null) {throw new IllegalArgumentException("class name may not be null");}
		//check to see if it is already there
		char classId = getClassId(jClassName);
		if (classId>0) {
			return classId;
		} else {
			Item it = new Item(jClassName);
			return (char)insert(it);
		}
	}

	//convenience method for creating n object from the classname
	public char newObject(String jClassName) {
		if (jClassName==null) {throw new IllegalArgumentException("class name may not be null");}
		//check to see if it is already there
		char classId = getClassId(jClassName);
		if (classId==0) {
			throw new IllegalArgumentException("class not found "+jClassName);
		} else {
			Item it = new Item(classId);
			return (char)insert(it);
		}
	}

	public char newIntArray(byte length) {
		char classId = getClassId(Item.INT_ARRAY_CLASS);
		if (classId==0) {
			classId=(char)insert(new Item(Item.INT_ARRAY_CLASS));
		}
		//nuar comes from newarray, which primitive types use
		Item nuar = new Item(Item.T_INT_ARRAY,classId,length);
		return (char)insert(nuar);
	}

	//length can be zero
	public char newObjectArray(byte length) {
		char classId = getClassId(Item.OBJ_ARRAY_CLASS);
		if (classId==0) {
			classId=(char)insert(new Item(Item.OBJ_ARRAY_CLASS));
		}
		Item anoar = new Item(Item.T_OBJ_ARRAY,classId,length);
		return (char)insert(anoar);
	}

	//new String array
	public char newStringArray(byte length) {
		char classId = getClassId(Item.STR_ARRAY_CLASS);
		if (classId==0) {
			classId=(char)insert(new Item(Item.STR_ARRAY_CLASS));
		}
		Item ansar = new Item(Item.T_STR_ARRAY,classId,length);
		return (char)insert(ansar);
	}

	//for arrays
	//0 means invalid, as arrays would never have a zero length
	public byte getLength(char id) {
		Item it = (Item)items[id];
		if (it==null) {
			return (byte)0;
		} else {
			return it.length;
		}
	}

	//for debugging
	public void dump() {
		System.out.println("ItemTable:");
		//start with 1
		for (int i=1;i<counter;i++) {
			Item it = items[i];
			if (it.type==Item.T_CLASS) {
				System.out.println(i+": (CLASS)"+it.class_name);
			} else {
				//ignorethe other types for now, they are runtime
				System.out.println(i+": ");
			}
		}
	}

}