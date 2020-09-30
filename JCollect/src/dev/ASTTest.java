package dev;

import java.util.ArrayList;
import java.util.List;

public class ASTTest {

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("ONE");
		list.add(list.get(0));
		list.add(method());
	}
	
	public static String method() {
		return null;
	}
	
	/*private String s;
	
	private ASTTest(String param) {
		s = evalParam(param);
		List<String> list = new ArrayList<>();
		list.add(s);
	}
	
	public static void main(String[] args) {
		new ASTTest("TEST");
	}
	
	public String evalParam(String param) {
		if (param != null) {
			return "E";
		}
		return "U";
	}*/

}
