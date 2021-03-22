package lava3.db;

/**
* A Row is a single entry in a Table.
*/
public interface Row {
	//sqlite has a 64-bit rowid, but I use a 32-bit one
	//if the row hasn't been inserted yet then the rowid is 0
	public int getRowId();

	//I don't specify getter or setter methods, but the class should have these
}