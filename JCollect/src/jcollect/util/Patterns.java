package jcollect.util;

public class Patterns {
	
	public static final String[] VARINDEXCHECKS = {
		"$VAR$.size()>$VALUE$",
		"$VALUE$<$VAR$.size()"
	};
	
	public static final String[] NEGATIVEINDEXCHECKS = {
		"$VAR$.size()>$VALUE$",
		"$VALUE$<$VAR$.size()",
		"$VALUE$>0",
		"$VALUE$>=0",
		"0<$VALUE$",
		"0<=$VALUE$"
	};
	
	public static final String[] EMPTYINDEXCHECKS = {
		"$VAR$.size()>0",
		"0<$VAR$.size()",
		"!$VAR$.isEmpty()",
		"$VAR$.isEmpty()==false"
	};
	
	public static final String[] NUMINDEXCHECKS = {
		"$VAR$.size()>",
		"$VAR$.size()>=",
		"<$VAR$.size()",
		"<=$VAR$.size()",
		"$VAR$.size()==",
		"==$VAR$.size()"
	};
	
	/**
	 * Fill in the given pattern with contextual values
	 * @param pattern The pattern, one of the patterns available in this class
	 * @param var The variable
	 * @param value The value, may be a variable or number
	 * @return The filled in pattern
	 */
	public static String[] getIndexPatterns(String[] pattern, String var, String value) {
		String[] filledPattern = new String[pattern.length];
		if (var != null && value != null) {
			for (int i = 0; i < pattern.length; i++) {
				filledPattern[i] = pattern[i].replace("$VAR$", var).replace("$VALUE$", value);
			}
		}
		return filledPattern;
	}
	
}
