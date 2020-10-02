package dev.examples;

import java.util.List;
import java.util.Stack;

public class ExternalCode {

	public static int returnUnknownNumber() {
		return 1;
	}
	
	public static List<Object> returnUnknownList() {
		return null;
	}
	
	public static List<Object> returnNotNullList() {
		return new Stack<Object>();
	}
	
}
