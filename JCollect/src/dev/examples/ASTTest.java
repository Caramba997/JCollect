package dev.examples;

import java.util.List;

public class ASTTest {

	public Object misuse(List<Object> l, int i) {
		return l.get(i);
	}
	/*public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		try {
			list.add("ONE");
			list.add(list.get(0));
			list.add(method());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String method() {
		return null;
	}
	
	private String s;
	
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
