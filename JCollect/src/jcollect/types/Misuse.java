package jcollect.types;

/**
 * An abstract data type for misuses. The fields are all public accessable
 * @author Finn Carstensen
 */
public class Misuse {
	
	public static final String IMPORTANCE_WARNING = "WARNING";
	public static final String IMPORTANCE_MISUSE = "MISUSE";
	
	public String type;
	public int line;
	public String importance;
	public String description;
	
	/**
	 * Creates a new misuse
	 * @param type The type of the misuse
	 * @param line The code line where the misuse was found
	 * @param description A description of the misuse
	 * @param importance The importance of the misuse to be checked by the user
	 */
	public Misuse(String type, int line, String description, String importance) {
		this.type = type;
		this.line = line;
		this.importance = importance;
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "{Type=" + type + ", Line=" + line + ", Description=" + description + ", Importance=" + importance + "}";
		
	}
}