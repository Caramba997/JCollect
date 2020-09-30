package jcollect.types;

/**
 * An abstract data type for method call properties. The fields are all public accessable
 * @author Finn Carstensen
 */
public class CallProperties {

	public String variable;
	public String method;
	public int line;
	
	/**
	 * Creates a new CallProperties object
	 * @param variable The variable on which the method is called
	 * @param method The method
	 * @param line The line of the call in the source code
	 */
	public CallProperties(String variable, String method, int line) {
		this.variable = variable;
		this.method = method;
		this.line = line;
	}
	
}
