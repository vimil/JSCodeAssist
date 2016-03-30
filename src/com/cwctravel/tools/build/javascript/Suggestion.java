package com.cwctravel.tools.build.javascript;

import java.util.ArrayList;
import java.util.List;

public class Suggestion {
	public static final String ST_NUMBER = "number";
	public static final String ST_STRING = "string";
	public static final String ST_BOOLEAN = "boolean";
	public static final String ST_OBJECT = "object";
	public static final String ST_FUNCTION = "function";

	private String type;
	private List<String> values;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getValues() {
		if(values == null) {
			values = new ArrayList<String>();
		}
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void addValue(String value) {
		getValues().add(value);

	}
}
