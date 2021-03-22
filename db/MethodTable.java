package lava3.db;

/**
* MethodTable.
* The id is the same as the NameTable.
* This is implemented as a sparse array with no index.  It could be stored in a byte array
*/
public class MethodTable implements Table {
	public final static int CAPACITY = 2048;	//arbitrary limit
	public final static int BASE = 256;

	MethodRow[] methods = new MethodRow[CAPACITY];

	public MethodTable() {}

	public String getTableName() {return "method";}

	//get the names of the columns.  The order doesn't matter
	public String[] columns() {return MethodRow.columns;}

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public String typeof(String column) {return MethodRow.typeof(column);}

	/**
	* Return the sql code that would create the table.
	*/
	public String createTableSql() {
		return "CREATE TABLE IF NOT EXISTS method ( "
			+"id INTEGER PRIMARY KEY, "
			+"class_id INTEGER, "
			+"is_static INTEGER, "
			+"params INTEGER, "
			+"code BLOB";
	}

	public int insert(Row data) {
		MethodRow m = (MethodRow)data;
		methods[m.id-BASE]=m;
		return (int)m.id;
	}

	public Row read(int rowid) {
		return (Row)methods[rowid-BASE];
	}

	public void update(int rowid,Row update) {
			methods[rowid-BASE]=(MethodRow)update;
	}

	//the number of rows in the table.
	public int rows() {
		//this isn't accurate and I'm not even going to pretend
		return 0;
	}

	//for debugging
	public void dump(Database db) {
		System.out.println("MethodTable:");
		//start with 1. There is no counter here so just go up to 100
		for (int i=BASE;i<350;i++) {
			MethodRow m = methods[i-BASE];
			if (m!=null) {
				Name n = (Name)db.nameTable.read(i);
				System.out.println(i+": "+n.jname);
			}
		}
	}
}