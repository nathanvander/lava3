package lava3.db;

/**
* This has common methods that tables use.  Even though it doesn't make sense
* to have more than one instance of the same table, they should be implemented
* as objects so we have a common interface.
*/
public interface Table {

	/**
	* Return the table name.  This is usually the same as the object but it
	* may be slightly different, like all in lower-case.
	*/
	public String getTableName();

	//get the names of the columns.  The order doesn't matter
	public String[] columns();

	//return one of these types:
	//	"null", "integer", "real", "text", or "blob"
	public String typeof(String column);

	/**
	* Return the sql code that would create the table.
	*/
	public String createTableSql();

	public int insert(Row data);

	public Row read(int rowid);

	public void update(int rowid,Row update);

	//the number of rows in the table.
	public int rows();

	//this should have select methods, but we don't specify this

	//we don't have delete
}