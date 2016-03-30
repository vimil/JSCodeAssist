package com.cwctravel.tools.build.javascript;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;

public class JSCodeAssist {

	private String source;
	private int cursorLocation;

	private CompletionParser parser;
	private CompilationUnitDeclaration parsedUnit;
	private CompilationResult compilationResult;

	private List<String> suggestions;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getCursorLocation() {
		return cursorLocation;
	}

	public void setCursorLocation(int cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

	public void compile() {
		Map<String, String> optionsMap = new HashMap<String, String>();
		optionsMap.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
		optionsMap.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
		optionsMap.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportWrongNumberOfArguments, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_Unresolved_Field, CompilerOptions.ERROR);
		optionsMap.put(CompilerOptions.OPTION_Unresolved_Method, CompilerOptions.ERROR);
		optionsMap.put(CompilerOptions.OPTION_Unresolved_Type, CompilerOptions.ERROR);
		optionsMap.put(CompilerOptions.OPTION_ReportUninitializedLocalVariable, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportUninitializedGlobalVariable, CompilerOptions.IGNORE);
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_SemanticValidation, CompilerOptions.ENABLED);
		optionsMap.put(CompilerOptions.OPTION_StrictOnKeywordUsage, CompilerOptions.ENABLED);
		CompilerOptions options = new CompilerOptions(optionsMap);

		parser = new CompletionParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options, new DefaultProblemFactory(Locale.getDefault())));
		parser.initialize();

		String snippet = getSource();
		ICompilationUnit sourceUnit = new CompilationUnit(snippet.toCharArray(), "snippet.js", null);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, options.maxProblemsPerUnit);
		parsedUnit = parser.dietParse(sourceUnit, compilationResult, getCursorLocation());
		parser.inferTypes(parsedUnit, options);

		if (parser.assistNode instanceof CompletionOnMemberAccess) {
			int completionIndex = -1;
			for (int i = 0; i < parsedUnit.statements.length; i++) {
				if (parsedUnit.statements[i] instanceof CompletionOnMemberAccess) {
					completionIndex = i;
					break;
				}
			}
			CompletionOnMemberAccess cOM = (CompletionOnMemberAccess) parser.assistNode;
			System.out.println(cOM.receiver);
			// parsedUnit.statements
		}
	}

	public static void main(String[] args) {
		JSCodeAssist codeAssist = new JSCodeAssist();
		// String snippet1 = "var f = function(){var a=1; this.a= 'hello';
		// b.x(}";
		String snippet2 = "var g= function() {\n" + "var x ={d:1,e:2}\n" + "var f = function() {\n"
				+ "var x= {a:1,b:2};\n" + "x.\nb=1;";
		int cursorLocation = snippet2.length() - 1;
		codeAssist.setSource(snippet2);
		System.out.println(cursorLocation);
		codeAssist.setCursorLocation(77);
		codeAssist.compile();

		System.out.println(codeAssist.parsedUnit);
	}

}
