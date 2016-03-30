package com.cwctravel.tools.build.javascript;

import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.IProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;

class DoNothingProblemReporter extends ProblemReporter {

	public DoNothingProblemReporter(CompilerOptions options, IProblemFactory problemFactory) {
		super(DefaultErrorHandlingPolicies.exitAfterAllProblems(), options, problemFactory);
	}

	@Override
	public void record(CategorizedProblem problem, CompilationResult unitResult, ReferenceContext context) {

	}
}