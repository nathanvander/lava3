package lava3;

/**
* MethodInfo consists of the bytecode and other surrounding information which
* is enough to invoke the code
*/
public interface MethodInfo {
	/**
	* Get the class id this method is a part of
	*/
	public char getClassId();

	/**
	* get the name id of this method
	*/
	public char getNameId();

	/**
	* get the number of params. Params must be in the range 0..3
	* We know the classid because it is associated with the name
	*/
	public byte params();

	public boolean isStatic();

	/**
	* Get the byte code. This is the same code that is stored in the classfile, I don't
	* prepend the number of params to it.
	*/
	public byte[] getCode();
}