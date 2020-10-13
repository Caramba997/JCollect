package dev.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoMisuseExamples {
	
	private static final int NUMBER = 0;
	
	public static void main(String[] args) {
		Index1();
		Index2();
		Index3();
		Index4();
		Index5();
		Index6();
		Index7();
		Index8();
		Null1();
		Null2();
		Null3();
		Null4();
		Null5();
		Unsupported1();
		Combined1();
	}

	public static void Index1() {
		List<String> list = new ArrayList<>();
		list.add("Element");
		if (list.size() >= 2) {
			list.get(1);
		}
	}
	
	public static void Index2() {
		List<String> list = new Stack<>();
		if (!list.isEmpty()) {
			list.get(0);
		}
	}
	
	public static void Index3() {
		int index = -3;
		List<String> list = new ArrayList<>();
		list.add("Element");
		if (index >= 0 && index < list.size()) {
			list.remove(index);
		}
	}
	
	public static void Index4() {
		int index = returnPositive(1);
		List<String> list = new LinkedList<>();
		list.add(returnPositive(6), "First");
		list.add(0, "String");
		list.add(index, "Element");
	}
	
	private static int returnPositive(int i) {
		int result = 1;
		if (i > 5) {
			return 0;
		}
		else return result;
	}
	
	public static void Index5() {
		int index = Integer.parseInt("-2");
		List<String> list = new LinkedList<>();
		try {
			list.get(index); //Error: Negative index
		}
		catch (Exception e) {
			list.add("Exception");
		}
	}
	
	public static void Index6() {
		List<String> list = new Vector<>();
		list.add("Element");
		list.add("String");
		list.add("Third");
		for (int i = 0; i < list.size(); i++) {
			list.get(i);
		}
	}
	
	public static void Index7() {
		List<Object> list = new ArrayList<>();
		int number = ExternalCode.returnUnknownNumber();
		if (number >= 0 && number < list.size()) {
			list.get(number);
		}
	}
	
	public static void Index8() {
		List<Integer> list = new Stack<>();
		list.add(NUMBER, 3); //Error: Negative index
	}
	
	public static void Null1() {
		List<String> list = new CopyOnWriteArrayList<>();
		list.add(null);
	}
	
	public static void Null2() {
		Object o = null;
		List<Object> list = new LinkedList<>();
		list.add(o);
		List<Object> list2 = ExternalCode.returnUnknownList();
		if (list2 != null) {
			list.addAll(list2);
		}
	}
	
	public static void Null3() {
		Object o = null;
		List<Object> list2 = null;
		List<Object> list = new LinkedList<>();
		list.add(o);
		list2 = new ArrayList<>();
		list2.add(new Object());
		if (list.size() >= 1) {
			list.addAll(1, list2);
		}
	}
	
	public static void Null4() {
		List<Object> list = new Stack<>();
		list.addAll(returnNull("-1"));
	}
	
	private static List<Object> returnNull(String number) {
		List<Object> list = new ArrayList<>();
		if (Integer.parseInt(number) > 0) {
			list = null;
		}
		return list;
	}
	
	public static void Null5() {
		List<Object> list = new LinkedList<>();
		List<Object> list2 = ExternalCode.returnNotNullList();
		if (list.containsAll(list2)) {
			return;
		}
		
	}
	
	public static void Unsupported1() {
		List<Object> list = new LinkedList<>();
		list = Collections.unmodifiableList(list);
		try {
			list.add(new Object());
		}
		catch (UnsupportedOperationException e) {
			return;
		}
	}
	
	public static void Combined1() {
		List<Object> list = new LinkedList<>();
		int index = 0;
		List<Object> list2 = new ArrayList<>();
		list2.add(new Object());
		try {
			list = Collections.unmodifiableList(list);
			list.addAll(index, list2);
		}
		catch (UnsupportedOperationException e) {
			return;
		}
	}
	
}
