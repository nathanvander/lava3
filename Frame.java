package lava3;
import lava3.db.*;

public class Frame {
	int level = 0;
	MethodInfo mi;
	byte[] code;
	//instruction pointer for the method
	//it always points to the next code to be executed
	int IP = 0;

	public Frame() {}

	public Frame(int lev,MethodInfo m) {
		level = lev;
		mi = m;
		code = m.getCode();
	}

	public char getClassId() {return mi.getClassId();}

	//I don't expect this to be called very often, but it is here
	public String getClassName() {
		Database db = Database.getInstance();
		Item i = (Item)db.itemTable.read( getClassId());
		return i.class_name;
	}

	public char getMethodId() { return mi.getNameId();}

	public String getMethodName() {
		Database db = Database.getInstance();
		Name n = (Name)db.nameTable.read( getMethodId());
		return n.jname;
	}

	public int params() {return (int)mi.params();}

	public byte NEXT() {return code[IP++];}

	//this is a relative jump.  The caller is responsible for adjusting the
	//offset, which I believe is -3
	public void JUMP(short rel) {
		IP = IP + (int)rel;
	}

	public String dump() {
		StringBuffer sb = new StringBuffer("========");
		sb.append("level="+level);
		sb.append("class="+(int)getClassId());
		sb.append("method="+(int)getMethodId()+" ("+getMethodName()+")");
		sb.append("params="+params());
		sb.append("IP="+IP);
		return sb.toString();
	}

	protected void finalize() {
		System.out.println("frame #"+level+": "+getMethodName()+" is complete");
	}
}