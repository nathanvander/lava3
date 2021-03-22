package lava3.db;
import java.util.*;

public class NameTable implements Table {
	public final static int CAPACITY = 2048;	//arbitrary limit
	public final static int BASE = 256;
	private static char counter=256;		//start counting at 256

	Name[] names = new Name[CAPACITY];
	HashMap<String,Character> nameIndex = new HashMap<String,Character>();

	public NameTable() {}

	public String getTableName() {return "name";}

	//get the names of the columns.  The order doesn't matter
	public String[] columns() {return Name.columns;}

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public String typeof(String column) {return Name.typeof(column);}

	/**
	* Return the sql code that would create the table.
	*/
	public String createTableSql() {
		return "CREATE TABLE IF NOT EXISTS name ( "
			+"id INTEGER PRIMARY KEY, "
			+"type INTEGER NOT NULL, "
			+"class_id INTEGER, "
			+"jname TEXT, "
			+"is_static INTEGER, "
			+"static_value TEXT) ";
	}

	public int insert(Row data) {
		char nid = counter++;
		Name na = (Name)data;
		na.id=nid;				//should this be setid?
		names[nid-BASE]=na;		//insert na
		nameIndex.put(na.jname,Character.valueOf(nid));
		return (int)nid;
	}

	public Row read(int rowid) {
		return (Row)names[rowid-BASE];
	}

	public void update(int rowid,Row update) {
		names[rowid-BASE]=(Name)update;
	}

	public char getNameId(String jname) {
		if (jname==null) {throw new IllegalArgumentException("name may not be null");}
		Character cc= nameIndex.get(jname);
		if (cc==null) {
			return (char)0;
		} else {
			return cc.charValue();
		}
	}

	public String getName(char id) {
		Name na = names[id];
		if (na==null) {
			return null;
		} else {
			return na.jname;
		}
	}

	public int rows() {
		return counter-BASE-1;
	}

	//for debugging
	public void dump() {
		System.out.println("NameTable:");
		//start with 1
		for (int i=BASE;i<counter;i++) {
			Name na = names[i-BASE];
			if (na==null) {
				System.out.println(i+"=null");
				continue;
			} else if (na.type==Name.T_FIELD) {
				System.out.println(i+": (CID)"+(int)na.class_id+" (FIELD)"+na.jname);
			} else if (na.type==Name.T_METHOD) {
				System.out.println(i+": (CID)"+(int)na.class_id+" (METHOD)"+na.jname);
			} else {
				//ignore the other types for now, they are runtime
				System.out.println(i+": ");
			}
		}
	}
}