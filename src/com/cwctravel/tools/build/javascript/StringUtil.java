package com.cwctravel.tools.build.javascript;

public class StringUtil {
	public static int compare(String s1, String s2) {
		if(s1 == s2) {
			return 0;
		}
		else if(s1 == null) {
			return -1;
		}
		else if(s2 == null) {
			return 1;
		}
		return s1.compareTo(s2);
	}
}
