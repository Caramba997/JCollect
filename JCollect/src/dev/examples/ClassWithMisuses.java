package dev.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class ClassWithMisuses {
	
	public ClassWithMisuses() {
		String[] strings = {"LessThanFive", "Five", "BiggerThanFive"};
		List<String> stringList= Arrays.asList(strings);
		someMethod(3, stringList);
		
		System.out.println("Started");
		int index = -3;
		int anotherIndex = index;
		List<String> list = new ArrayList<String>();
		list.add("ONE");
		list.add("TWO");
		list.add("THREE");
		list.add("FOUR");
		List<String> sList = Collections.synchronizedList(list);
		list.addAll(list);
		list.hashCode();
	    new Thread(() -> {
	    	Iterator<String> iter = sList.iterator(); // Must be in the synchronized block
	        while (true) {
	        	sList.add("X");
			    iter.next();
	        }
	    }).start();
	    new Thread(() -> {
	        while (true) {
	        	sList.add("Y");
	        }
	    }).start();
		list.add("FIVE");
		Vector<String> q = new Stack<>();
		List<String> vector = Collections.unmodifiableList(q);
		vector.add("ABS");
		q.add("HAHA");
		Stack<String> stack = new Stack<>();
		stack.add("HEHE");
		anotherIndex = Integer.valueOf("-3");
		String str = returnAnything(1, 2);
		//Iterator iterator = list.iterator();
		//list.remove(10);
		if (list.size() > index) {
			list.get(index);
		}
		list.get(Integer.valueOf("-3"));
		if (list.size() > 2) {
			list.remove(2);
		}
		while (list.size() > 0) {
			if (true) {
				list.remove(0);
			}
		}
		if (list.size() > 2) {
			if (true) {
				list.get(2);
			}
		}
		if (str != null) {
			list.remove(str);
		}
		list.get(3);
		for (int i = 0; i < list.size(); i++) {
			list.get(i);
		}
		list.remove(returnAnything(2, 5));
		list.get(-2);
		//iterator.next();
		//iterator.next();
		//iterator.next();
		list.remove(null);
		list.get(anotherIndex);
		setAnything(list);
		list.add(returnAnything(23, 2));
		System.out.println("Finished");
		String str2 = null;
		if (str != null) {
			str2 = str;
		}
		list.add(str2);
	}
	
	private List<String> someMethod(int i, List<String> oldList) {
		String s = null;
		List<String> list = new ArrayList<>(oldList);
		if (i > 5) {
			s = "LessThanFive";
		}
		list.remove(s);
		return list;
	}

	public static void main(String[] args) {
		new ClassWithMisuses();
	}
	
	private static void setAnything(List<String> list) {
		list.add("Anything");
	}
	
	public static String returnAnything(int a, int b) {
		int x = a + b;
		if (x < 10) {
			return null;
		}
		return String.valueOf(x - 10);
	}
	
}
