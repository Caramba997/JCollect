package dev.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class MisuseExamples {
	
	private static int NUMBER = -4;

	public static void main(String[] args) {
		if (args.length == 1) {
			switch (args[0]) {
			case "index1": Index1(); break;
			case "index2": Index2(); break;
			case "index3": Index3(); break;
			case "index4": Index4(); break;
			case "index5": Index5(); break;
			case "index6": Index6(3); break;
			case "index7": Index7(); break;
			case "index8": Index8(); break;
			case "index9": Index9(); break;
			case "index10": Index10(1); break;
			case "null1": Null1(); break;
			case "null2": Null2(); break;
			case "null3": Null3(); break;
			case "null4": Null4(); break;
			case "null5": Null5(); break;
			case "null6": Null6(); break;
			case "null7": Null7(null); break;
			case "unsupported1": Unsupported1(); break;
			case "unsupported2": Unsupported2(); break;
			case "unsupported3": Unsupported3(); break;
			case "unsupported4": Unsupported4(); break;
			case "combined1": Combined1(); break;
			default: System.out.println("There is no such function (" + args[0] + ")!");
			}
		}
		else {
			System.out.println("No function selected!");
		}
	}
	
	public static void Index1() {
		List<String> list = new ArrayList<>();
		list.add("Element");
		list.get(2); //Error: There is no element at index 0
	}
	
	public static void Index2() {
		List<String> list = new Stack<>();
		list.add("Element");
		list.get(-1); //Error: Negative index
	}
	
	public static void Index3() {
		int index = -3;
		List<String> list = new ArrayList<>();
		list.add("Element");
		list.remove(index); //Error: Negative index
	}
	
	public static void Index4() {
		int index = returnNegative(1);
		List<String> list = new LinkedList<>();
		list.add(index, "Element"); //Error: Negative index
	}
	
	private static int returnNegative(int i) {
		int result = -5;
		if (i > 5) {
			return 0;
		}
		else return result;
	}
	
	public static void Index5() {
		int index = Integer.parseInt("-2");
		List<String> list = new LinkedList<>();
		list.get(index); //Error: Negative index
	}
	
	public static void Index6(int elements) {
		List<String> list = new Vector<>();
		for (int i = 0; i < elements; i++) {
			list.add("Element");
		}
		for (int i = 0; i <= 3; i++) {
			list.get(i); //Error: Index out of bounds for i=3 when elements > 2
		}
	}
	
	public static void Index7() {
		List<Object> list = new ArrayList<>();
		list.get(ExternalCode.returnUnknownNumber()); //Error: Index out of bounds
	}
	
	public static void Index8() {
		List<Integer> list = new Stack<>();
		list.add(NUMBER, 3); //Error: Negative index
	}
	
	public static void Index9() {
		List<String> list = new ArrayList<>();
		list.add("One");
		List<String> list2 = list;
		list2.remove(1); //Error: Index out of bounds
	}
	
	public static String Index10(int index) {
		List<String> list = new ArrayList<>();
		return list.get(index); //Error for any given index
	}
	
	public static void Null1() {
		List<String> list = new CopyOnWriteArrayList<>();
		list.addAll(null); //Error: null
	}
	
	public static void Null2() {
		Object o = null;
		List<Object> list = new LinkedList<>();
		list.add(o);
		List<Object> list2 = null;
		list.addAll(list2); //Error: null
	}
	
	public static void Null3() {
		Object o = null;
		List<Object> list = new LinkedList<>();
		list.add(o);
		List<Object> list2 = null;
		if (list.size() >= 1) {
			list.addAll(1, list2); //Error: null
		}
	}
	
	public static void Null4() {
		List<Object> list = new Stack<>();
		list.add(new Object());
		list.add(null);
		List<Object> list2 = list.subList(0, 1);
		list = null;
		list2.add(new Object());
		list2.removeAll(list); //Error: null
	}
	
	public static void Null5() {
		List<Object> list = new Stack<>();
		list.addAll(returnNull()); //Error: null
	}
	
	private static List<Object> returnNull() {
		List<Object> list = new ArrayList<>();
		if (Integer.parseInt("42") > 0) {
			list = null;
		}
		return list;
	}
	
	public static void Null6() {
		List<Object> list = new LinkedList<>();
		List<Object> list2 = ExternalCode.returnUnknownList();
		if (list.containsAll(list2)) { //Error: null
			return;
		}
	}
	
	public static void Null7(List<String> list) {
		List<String> list2 = new ArrayList<>();
		list2.addAll(list);
	}
	
	public static void Unsupported1() {
		List<Object> list = new LinkedList<>();
		list = Collections.unmodifiableList(list);
		list.add(new Object()); //Error: Unsupported operation
	}
	
	public static void Unsupported2() {
		List<Object> list = new LinkedList<>();
		list.add(new Object());
		list = Collections.unmodifiableList(list);
		int index = 0;
		if (!list.isEmpty()) {
			list.remove(index); //Error: Unsupported operation
		}
	}
	
	public static void Unsupported3() {
		List<String> list = returnUnmodifiable();
		list.clear(); //Error: Unsupported operation
	}
	
	public static void Unsupported4() {
		List<String> list = returnUnmodifiable();
		List<String> list2 = list;
		list = new ArrayList<>();
		list2.clear(); //Error: Unsupported operation
	}
	
	private static List<String> returnUnmodifiable() {
		List<String> list = new LinkedList<>();
		list.add("Only");
		return Collections.unmodifiableList(list);
	}
	
	public static void Combined1() {
		List<Object> list = new LinkedList<>();
		int index = -1;
		List<Object> list2 = null;
		list = Collections.unmodifiableList(list);
		list.addAll(index, list2); //Error: Everything wrong
	}

}
