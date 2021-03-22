package lava3.db;
import lava3.Cell;
import java.util.*;

/**
* The ItemIndexTable is essentially a table of global variables.
* I have an Item, which is a noun and can be a class, object or array.
* Each Item has indexes, which are adjectives, and which hold the value.
* This has multiple uses:
*	1. store a constant (int or string) in a constant pool
*  	2. store a name in a constant pool, including a method name
*	3. store a static object associated with a class.  This is stored in slot 0
*	4. store a static value
*	5. store a value in an object field
*	6. store a value in an array
*
* The return value is the theoretical position in memory. It is calculated by
* 65536 * itemid + x. Usually the x can never be zero, but if the itemid is
* a class, then the static object associated with the class is in slot 0.
*
* I am using a cell for the value.  Perhaps I should split it up to include a string
* int and char.
*/
public class ItemIndexTable {
	HashMap<Integer,Cell> item_index=new HashMap<Integer,Cell>();

	public String getTableName() {return "item_index";}

	//get the names of the columns.  The order doesn't matter
	public String[] columns() {return new String[]{"key","value"};}

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public String typeof(String column) {
		switch(column) {
			case "key": return "integer";
			case "value": return "text";
			default: return "null";
		}
	}

	/**
	* Return the sql code that would create the table.
	*/
	public String createTableSql() {
		return "CREATE TABLE IF NOT EXISTS item_index ( "
			+"key INTEGER PRIMARY KEY, "
			+"value TEXT) ";
	}

	public int insert(Row data) {
		//use the other insert method
		return -1;
	}

	public int insert(char itemId,char x,Cell value) {
		Integer key = new Integer( itemId*65536+x);
		item_index.put(key,value);
		return key.intValue();
	}

	public Row read(int rowid) {
		return item_index.get(Integer.valueOf(rowid));
	}

	public Row read(char itemId,char x) {
		return item_index.get(Integer.valueOf(itemId*65536+x));
	}

	public void update(int rowid,Row update) {
		item_index.put(Integer.valueOf(rowid),(Cell)update);
	}

	//the number of rows in the table.
	public int rows() {
		return item_index.size();
	}

	public void dump() {
		System.out.println("ItemIndexTable:");
		Set keys = item_index.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			Integer k = (Integer)iter.next();
			int i = k.intValue();
			int cid = i / 65536;
			int nid = i % 65536;
			Cell value = (Cell)item_index.get(k);
			System.out.println("item "+cid+"; index "+nid+" = "+value.toString());
		}
	}
}