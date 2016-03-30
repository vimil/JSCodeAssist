package com.cwctravel.tools.build.javascript;

public class JSCodeAssistTest {
	public static void main(String[] args) {
		JSCodeAssist codeAssist = new JSCodeAssist();
		// String snippet1 = "var f = function(){var a=1; this.a= 'hello';
		// b.x(}";
		// String snippet2 = "var g= function() {\n" + "var x ={d:1,e:2,y:{a:'hello',b:5}}\n" + "var f = function() {\n" + "var x= {a:1,b:2};\n" +
		// "x.y.a.";
		String snippet2 = "var g= function() {\n" + "var x = context.stream;\nvar y = x.a;\n" + "var f = function() {\n" + "x.";
		int cursorLocation = snippet2.length() - 1;
		codeAssist.setSource(snippet2);
		codeAssist.setCursorLocation(cursorLocation);
		codeAssist.compile();

	}
}
