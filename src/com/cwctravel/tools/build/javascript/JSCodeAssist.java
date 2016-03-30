package com.cwctravel.tools.build.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ast.Assignment;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.Expression;
import org.eclipse.wst.jsdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.wst.jsdt.internal.compiler.ast.FieldReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.FunctionExpression;
import org.eclipse.wst.jsdt.internal.compiler.ast.IntLiteral;
import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.ObjectLiteral;
import org.eclipse.wst.jsdt.internal.compiler.ast.ObjectLiteralField;
import org.eclipse.wst.jsdt.internal.compiler.ast.ProgramElement;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.Statement;
import org.eclipse.wst.jsdt.internal.compiler.ast.StringLiteral;
import org.eclipse.wst.jsdt.internal.compiler.ast.ThisReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.TrueLiteral;
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

	private Map<String, Class<?>> globalMappings;

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

		parser = new CompletionParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options, new DefaultProblemFactory(Locale.getDefault())));
		parser.initialize();

		String snippet = getSource();
		ICompilationUnit sourceUnit = new CompilationUnit(snippet.toCharArray(), "snippet.js", null);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, options.maxProblemsPerUnit);
		parsedUnit = parser.dietParse(sourceUnit, compilationResult, getCursorLocation());
		parser.inferTypes(parsedUnit, options);

		if(parser.assistNode instanceof CompletionOnMemberAccess) {
			int completionIndex = -1;
			for(int i = 0; i < parsedUnit.statements.length; i++) {
				ProgramElement programElement = parsedUnit.statements[i];
				if(programElement instanceof CompletionOnMemberAccess) {
					completionIndex = i;
					break;
				}
				else if(programElement instanceof LocalDeclaration) {
					LocalDeclaration lD = (LocalDeclaration)programElement;
					if(lD.initialization instanceof CompletionOnMemberAccess) {
						completionIndex = i;
						break;
					}
				}
			}

			CompletionOnMemberAccess cOM = (CompletionOnMemberAccess)parser.assistNode;
			List<String> receiverNamePath = computeReceiverNamePath(cOM.receiver);

			List<Suggestion> suggestions = new ArrayList<Suggestion>();
			for(int i = completionIndex - 1; i >= 0; i--) {
				ProgramElement programElement = parsedUnit.statements[i];
				if(programElement instanceof LocalDeclaration) {
					LocalDeclaration lD = (LocalDeclaration)programElement;
					computeSuggestions(receiverNamePath, lD, suggestions);
				}
			}

			System.out.println(suggestions);
			// parsedUnit.statements
		}
	}

	private List<String> computeReceiverNamePath(Expression receiver) {
		List<String> result = new ArrayList<String>();
		while(receiver != null) {
			if(receiver instanceof FieldReference) {
				FieldReference fieldReference = (FieldReference)receiver;
				result.add(0, new String(fieldReference.token));
				receiver = fieldReference.receiver;
			}
			else if(receiver instanceof SingleNameReference) {
				SingleNameReference singleNameReference = (SingleNameReference)receiver;
				result.add(0, new String(singleNameReference.token));
				break;
			}
			else {
				break;
			}
		}

		return result;
	}

	private void computeSuggestions(List<String> receiverNamePath, LocalDeclaration lD, List<Suggestion> suggestions) {
		computeSuggestions(receiverNamePath, 0, lD, suggestions);
	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, LocalDeclaration lD, List<Suggestion> suggestions) {
		if(receiverNamePath != null && !receiverNamePath.isEmpty()) {
			String variableName = new String(lD.name);
			if(StringUtil.compare(receiverNamePath.get(pathIndex), variableName) == 0) {
				Expression initializer = lD.initialization;
				computeSuggestions(receiverNamePath, pathIndex, initializer, suggestions);
			}
		}
	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, FunctionExpression functionExpression, List<Suggestion> suggestions) {
		if(pathIndex == receiverNamePath.size()) {
			MethodDeclaration methodDeclaration = functionExpression.methodDeclaration;
			if(methodDeclaration != null) {
				Statement[] statements = methodDeclaration.statements;
				if(statements != null) {
					Suggestion suggestion = new Suggestion();
					suggestion.setType(Suggestion.ST_FUNCTION);
					for(Statement statement: statements) {
						if(statement instanceof Assignment) {
							Assignment assignment = (Assignment)statement;
							if(assignment.lhs instanceof FieldReference) {
								FieldReference fieldReference = (FieldReference)assignment.lhs;
								if(fieldReference.receiver instanceof ThisReference) {
									suggestion.addValue(new String(fieldReference.token));
								}
							}
						}
					}
					suggestions.add(suggestion);
				}
			}
		}
		else {
			MethodDeclaration methodDeclaration = functionExpression.methodDeclaration;
			computeSuggestions(receiverNamePath, pathIndex, methodDeclaration.statements, suggestions);
		}
	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, Statement[] statements, List<Suggestion> suggestions) {
		if(statements != null) {
			for(Statement statement: statements) {
				if(statement instanceof Assignment) {
					Assignment assignment = (Assignment)statement;
					if(assignment.lhs instanceof FieldReference) {
						FieldReference fieldReference = (FieldReference)assignment.lhs;
						if(fieldReference.receiver instanceof ThisReference) {
							computeSuggestions(receiverNamePath, pathIndex, assignment.expression, suggestions);
						}
					}
				}
			}
		}
	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, ObjectLiteral objectLiteral, List<Suggestion> suggestions) {
		if(pathIndex == receiverNamePath.size()) {
			if(objectLiteral.fields != null) {
				Suggestion suggestion = new Suggestion();
				suggestion.setType(Suggestion.ST_OBJECT);
				for(ObjectLiteralField field: objectLiteral.fields) {
					SingleNameReference singleNameReference = (SingleNameReference)field.fieldName;
					suggestion.addValue(new String(singleNameReference.token));
				}
				suggestions.add(suggestion);
			}
		}
		else {
			computeSuggestions(receiverNamePath, pathIndex, objectLiteral.fields, suggestions);
		}

	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, ObjectLiteralField[] fields, List<Suggestion> suggestions) {
		if(fields != null) {
			for(ObjectLiteralField field: fields) {
				SingleNameReference singleNameReference = (SingleNameReference)field.fieldName;
				String fieldName = new String(singleNameReference.token);
				if(StringUtil.compare(receiverNamePath.get(pathIndex), fieldName) == 0) {
					Expression initializer = field.initializer;
					computeSuggestions(receiverNamePath, pathIndex, initializer, suggestions);
				}
			}
		}
	}

	private void computeSuggestions(List<String> receiverNamePath, int pathIndex, Expression initializer, List<Suggestion> suggestions) {
		if(initializer instanceof ObjectLiteral) {
			ObjectLiteral objectLiteral = (ObjectLiteral)initializer;
			computeSuggestions(receiverNamePath, pathIndex + 1, objectLiteral, suggestions);
		}
		else if(initializer instanceof FunctionExpression) {
			FunctionExpression functionExpression = (FunctionExpression)initializer;
			computeSuggestions(receiverNamePath, pathIndex + 1, functionExpression, suggestions);
		}
		else if(initializer instanceof IntLiteral) {
			Suggestion suggestion = new Suggestion();
			suggestion.setType(Suggestion.ST_NUMBER);
			suggestions.add(suggestion);
		}
		else if(initializer instanceof StringLiteral) {
			Suggestion suggestion = new Suggestion();
			suggestion.setType(Suggestion.ST_STRING);
			suggestions.add(suggestion);
		}
		else if(initializer instanceof FalseLiteral || initializer instanceof TrueLiteral) {
			Suggestion suggestion = new Suggestion();
			suggestion.setType(Suggestion.ST_BOOLEAN);
			suggestions.add(suggestion);
		}
		else if(initializer instanceof SingleNameReference) {
			SingleNameReference singleNameReference = (SingleNameReference)initializer;
			receiverNamePath.set(pathIndex, new String(singleNameReference.getToken()));
		}
		else if(initializer instanceof FieldReference) {
			FieldReference fieldReference = (FieldReference)initializer;
			List<String> fieldReferencePath = computeReceiverNamePath(fieldReference);
			receiverNamePath.remove(pathIndex);
			receiverNamePath.addAll(pathIndex, fieldReferencePath);
		}
	}

}
