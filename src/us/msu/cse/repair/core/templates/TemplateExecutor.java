package us.msu.cse.repair.core.templates;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;


import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;

import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import us.msu.cse.repair.core.parser.ExtendedModificationPoint;

import us.msu.cse.repair.core.parser.VarMethodInfoExtractor;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.parser.ingredient.IngredientUtil;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.TemplateCreationASTVisitor;

public class TemplateExecutor {
	Map<String, String> sourceContents;
    Map<String, ITypeBinding> declaredClasses;
    List<ExtendedModificationPoint> modificationPoints;
    
    Map<IMethodBinding, MethodDeclaration> methodDeclarations;
    
	public TemplateExecutor(List<ExtendedModificationPoint> modificationPoints, Map<String, String> sourceContents,
			Map<String, ITypeBinding> declaredClasses,
			Map<IMethodBinding, MethodDeclaration> methodDeclarations) throws MalformedTreeException, BadLocationException {
		this.sourceContents = sourceContents;
		this.declaredClasses = declaredClasses;
		this.modificationPoints = modificationPoints;
		this.methodDeclarations = methodDeclarations;
	}
	
	public void execute() throws MalformedTreeException, BadLocationException {
		for (ExtendedModificationPoint mp : modificationPoints) {
			handleOneModificationPoint(mp);
		}
	}
	
	
	
	void handleOneModificationPoint(ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		Statement statement = mp.getStatement();
		
		boolean isIf = (statement instanceof IfStatement);
		boolean isWhile = (statement instanceof WhileStatement);
		boolean isDo = (statement instanceof DoStatement);

		if (isIf || isWhile || isDo) {
			Expression exp;
			if (isIf)
				exp = ((IfStatement) statement).getExpression();
			else if (isWhile) 
				exp = ((WhileStatement) statement).getExpression();
			else
				exp = ((DoStatement) statement).getExpression();
			
			removeAndAddBooleanCondition(exp, mp);
		}
		
		TemplateCreationASTVisitor tcVisitor = new TemplateCreationASTVisitor(mp, methodDeclarations);	
		ASTNode node = getVisitedNode(statement);	
		if (node == null)
			return;
		
		node.accept(tcVisitor);
		
		handleElementReplacers(tcVisitor.getElementReplacers(), mp);
		handleNullPointerChecker(tcVisitor.getNullPointerChecker(), mp);
		handleCastChecker(tcVisitor.getCastChecker(), mp);
	    handleDivideZeroChecker(tcVisitor.getDivideZeroChecker(), mp);
	    handleRangeChecker(tcVisitor.getRangeChecker(), mp);
	    handleOverloadedMethodDetectors(tcVisitor.getOverloadedMethodDetectors(), mp);
	}
	
	void removeAndAddBooleanCondition(Expression exp, ExtendedModificationPoint mp)
			throws MalformedTreeException, BadLocationException {
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		List<Expression> expressions = new ArrayList<Expression>();
		List<String> operators = new ArrayList<String>();
		
		decomposeBooleanCondition(exp, expressions, operators);
		
		removeBooleanCondition(exp, expressions, operators, mp, replacements);
		addBooleanCondition(exp, expressions, mp, replacements);

		ElementReplacer er = new ElementReplacer(exp, replacements);
		handleElementReplacer(er, mp);
	}
	
	void decomposeBooleanCondition(Expression exp, List<Expression> expressions,
			List<String> operators) {
		while (Helper.isAndOrOrInfixExpression(exp)) {
			InfixExpression nt = (InfixExpression) exp;

			Expression right = nt.getRightOperand();
			String op = nt.getOperator().toString();
			expressions.add(right);
			operators.add(op);
			exp = nt.getLeftOperand();
		}
		
		expressions.add(exp);
		operators.add(null);
	}
	
	void removeBooleanCondition(Expression exp, List<Expression> expressions, List<String> operators,
			ExtendedModificationPoint mp, List<ASTNode> replacements) {
		if (expressions.size() <= 1)
			return;

		for (int i = 0; i < expressions.size(); i++)
			removeBooleanCondition(expressions, operators, i, exp, replacements);
	}
	

	
	void removeBooleanCondition(List<Expression> expressions, List<String> operators, int index, Expression exp,
			List<ASTNode> replacements) {
		int size = expressions.size();
		if (size == 2) {
			Expression resEx = (Expression) ASTNode.copySubtree(exp.getAST(), expressions.get(1 - index));
			replacements.add(resEx);
			return;
		}
		
		Expression copyEx;
		int startIndex;
		if (index < size - 1) {
			copyEx = (Expression) ASTNode.copySubtree(exp.getAST(), expressions.get(size - 1));
			startIndex = size - 2;
		}
		else {
			copyEx = (Expression) ASTNode.copySubtree(exp.getAST(), expressions.get(size - 2));
			startIndex = size - 3;
		}

		for (int i = startIndex; i >= 0; i--) {
			if (i == index)
				continue;
			
			InfixExpression ie = exp.getAST().newInfixExpression();
			ie.setLeftOperand(copyEx);
			
			InfixExpression.Operator op = InfixExpression.Operator.toOperator(operators.get(i));
			ie.setOperator(op);
			
			Expression right = (Expression) ASTNode.copySubtree(exp.getAST(), expressions.get(i));
			ie.setRightOperand(right);
			
			copyEx = ie;
		}
		replacements.add(copyEx);
	}
	
	void addBooleanCondition(Expression exp, List<Expression> expressions, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		List<Expression> booleanExps = mp.getAvailableBooleanExpressions();
		
		for (Expression booleanExp : booleanExps) {
			if (isInExpressionList(booleanExp, expressions))
				continue;
			addBoolean_And(exp, booleanExp, mp, replacements);
			addBoolean_Or(exp, booleanExp, mp, replacements);
			
			boolean flag = (booleanExp instanceof InfixExpression)
					&& (((InfixExpression) booleanExp).getOperator() == InfixExpression.Operator.GREATER
					|| ((InfixExpression) booleanExp).getOperator() == InfixExpression.Operator.GREATER_EQUALS
					|| ((InfixExpression) booleanExp).getOperator() == InfixExpression.Operator.LESS
					|| ((InfixExpression) booleanExp).getOperator() == InfixExpression.Operator.LESS_EQUALS);
			
			if (flag)
				continue;
			
			addBoolean_AndNot(exp, booleanExp, mp, replacements);
			addBoolean_OrNot(exp, booleanExp, mp, replacements);
		}
	}
	
	boolean isInExpressionList(Expression exp, List<Expression> expressions) {
		for (Expression expression : expressions) {
			if (expression.subtreeMatch(new ASTMatcher(true), exp))
				return true;
		}
		return false;
	}
	
	void addBoolean_And(Expression exp, Expression booleanExp, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		InfixExpression ie = mp.getStatement().getAST().newInfixExpression();

		Expression left = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
		Expression right = (Expression) ASTNode.copySubtree(exp.getAST(), booleanExp);

		ie.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		ie.setLeftOperand(left);
		ie.setRightOperand(right);
		replacements.add(ie);
	}

	void addBoolean_Or(Expression exp, Expression booleanExp, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		InfixExpression ie = mp.getStatement().getAST().newInfixExpression();

		Expression left = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
		Expression right = (Expression) ASTNode.copySubtree(exp.getAST(), booleanExp);

		ie.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		ie.setLeftOperand(left);
		ie.setRightOperand(right);
		replacements.add(ie);
	}

	void addBoolean_AndNot(Expression exp, Expression booleanExp, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		InfixExpression ie = mp.getStatement().getAST().newInfixExpression();

		Expression left = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
		
		Expression temp = (Expression) ASTNode.copySubtree(exp.getAST(), booleanExp);
		
		PrefixExpression right = mp.getStatement().getAST().newPrefixExpression(); 
		right.setOperator(PrefixExpression.Operator.NOT);
		
		ParenthesizedExpression pe = mp.getStatement().getAST().newParenthesizedExpression();
		pe.setExpression(temp);	
		
		right.setOperand(pe);

		ie.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		ie.setLeftOperand(left);
		ie.setRightOperand(right);
		replacements.add(ie);
	}

	void addBoolean_OrNot(Expression exp, Expression booleanExp, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		InfixExpression ie = mp.getStatement().getAST().newInfixExpression();

		Expression left = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
		
		Expression temp = (Expression) ASTNode.copySubtree(exp.getAST(), booleanExp);
		
		PrefixExpression right = mp.getStatement().getAST().newPrefixExpression(); 
		right.setOperator(PrefixExpression.Operator.NOT);
		
		ParenthesizedExpression pe = mp.getStatement().getAST().newParenthesizedExpression();
		pe.setExpression(temp);	
		
		right.setOperand(pe);

		ie.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		ie.setLeftOperand(left);
		ie.setRightOperand(right);
		replacements.add(ie);
	}
	
	
	void handleOverloadedMethodDetectors(List<OverloadedMethodDetector> overloadedMethodDetectors,
			ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		for (OverloadedMethodDetector omd : overloadedMethodDetectors) 
			handleOverloadedMethodDetector(omd, mp);
	}
	
	void handleOverloadedMethodDetector(OverloadedMethodDetector overloadedMethodDetector, ExtendedModificationPoint mp)
			throws MalformedTreeException, BadLocationException {
		List<IMethodBinding> overloadedMethods = overloadedMethodDetector.getOverloadedMethods();
		Expression currentMethod = overloadedMethodDetector.getCurrentMethod();

		for (IMethodBinding overloadedMethod : overloadedMethods)
			handleOneOverloadedMethod(currentMethod, overloadedMethod, mp);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void handleOneOverloadedMethod(Expression currentMethod, IMethodBinding overloadedMethod,
			ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		int olNumberOfArgs = overloadedMethod.getParameterTypes().length;
		List curArguments = Helper.getArguments(currentMethod);
		int curNumberOfArgs = curArguments.size();
		
		if (curNumberOfArgs == 1 && olNumberOfArgs == 1)
			return;

		ITypeBinding olParameterTypes[] = overloadedMethod.getParameterTypes();

		if (curNumberOfArgs == olNumberOfArgs || curNumberOfArgs - 1 == olNumberOfArgs) {			
			if (olNumberOfArgs == 0) {
				Expression repMethod = (Expression) ASTNode.copySubtree(currentMethod.getAST(),
						currentMethod);
				List repArguments = Helper.getArguments(repMethod);
				repArguments.clear();
				handleOneReplacement(currentMethod, repMethod, mp);
				return;
			}		
			
			int[][] perms = Helper.getPermutations(curNumberOfArgs);
			for (int i = 0; i < perms.length; i++) {
				int j = 0;
				for (j = 0; j < olNumberOfArgs; j++) {
					int k = perms[i][j];
					Expression exp = (Expression) curArguments.get(k);
					ITypeBinding argType = exp.resolveTypeBinding();

					if (argType == null || !argType.isAssignmentCompatible(olParameterTypes[j]))
						break;
				}
				if (j == olNumberOfArgs) {
					Expression repMethod = (Expression) ASTNode.copySubtree(currentMethod.getAST(), currentMethod);
					List repArguments = Helper.getArguments(repMethod);
					repArguments.clear();

					for (int l = 0; l < olNumberOfArgs; l++) {
						int k = perms[i][l];
						ASTNode arg = (ASTNode) curArguments.get(k);
						ASTNode argCopy = ASTNode.copySubtree(arg.getAST(), arg);
						repArguments.add(argCopy);
					}

					handleOneReplacement(currentMethod, repMethod, mp);
				}
			}
		} else if (curNumberOfArgs + 1 == olNumberOfArgs) {
			int[][] perms = Helper.getPermutations(olNumberOfArgs);

			for (int i = 0; i < perms.length; i++) {
				int j = 0;
				int index = -1;
				for (j = 0; j < olNumberOfArgs; j++) {
					int k = perms[i][j];
					if (k == olNumberOfArgs - 1) {
						index = j;
						continue;
					}
					Expression exp = (Expression) curArguments.get(k);
					ITypeBinding argType = exp.resolveTypeBinding();

					if (argType == null || !argType.isAssignmentCompatible(olParameterTypes[j]))
						break;
				}

				if (j == olNumberOfArgs) {
					ITypeBinding addedType = olParameterTypes[index];
					List<ASTNode> alternatives = new ArrayList<ASTNode>();

					if (Helper.isBooleanType(addedType)) {
						BooleanLiteral bl1 = mp.getStatement().getAST().newBooleanLiteral(true);
						BooleanLiteral bl2 = mp.getStatement().getAST().newBooleanLiteral(false);
						alternatives.add(bl1);
						alternatives.add(bl2);
					} else {
						Map<String, VarInfo> localVars = mp.getLocalVars();
						Map<String, VarInfo> declaredFields = mp.getDeclaredFields();
						Map<String, VarInfo> inheritedFields = mp.getInheritedFields();
						Map<String, VarInfo> outerFields = mp.getOuterFields();

						findCompatibleVarsAsArgument(addedType, localVars, mp, alternatives);
						findCompatibleVarsAsArgument(addedType, declaredFields, mp, alternatives);
						findCompatibleVarsAsArgument(addedType, inheritedFields, mp, alternatives);
						findCompatibleVarsAsArgument(addedType, outerFields, mp, alternatives);
						
						if (alternatives.size() == 0)
							continue;

						for (int l = 0; l < alternatives.size(); l++) {
							Expression repMethod = (Expression) ASTNode.copySubtree(currentMethod.getAST(),
									currentMethod);
							List repArguments = Helper.getArguments(repMethod);
							repArguments.clear();
							for (int u = 0; u < olNumberOfArgs; u++) {
								int k = perms[i][u];
								if (k == olNumberOfArgs - 1)
									repArguments.add(alternatives.get(l));
								else {
									ASTNode arg = (ASTNode) curArguments.get(k);
									ASTNode argCopy = ASTNode.copySubtree(arg.getAST(), arg);
									repArguments.add(argCopy);
								}
							}
							handleOneReplacement(currentMethod, repMethod, mp);
						}

					}

				}

			}
		}

	}
	
	void findCompatibleVarsAsArgument(ITypeBinding addedType, Map<String, VarInfo> vars, ExtendedModificationPoint mp,
			List<ASTNode> alternatives) {
		for (Map.Entry<String, VarInfo> entry : vars.entrySet()) {
			String key = entry.getKey();
			ITypeBinding tb = entry.getValue().getTypeBinding();
			if (tb != null && tb.isAssignmentCompatible(addedType)) {
				SimpleName name = mp.getStatement().getAST().newSimpleName(key);
				alternatives.add(name);
			}
		}
	}
	
	
	void handleChecker(Expression ifExp, ExtendedModificationPoint mp,
			boolean isInParams) {
		Statement statement = mp.getStatement();
		
		Expression notIfExp = Helper.getNotBooleanExpression(statement.getAST(), ifExp);
		
		boolean isVarDecl = statement instanceof VariableDeclarationStatement;
		boolean isLastRT = Helper.isLastStatementInMethod(statement) && (statement instanceof ReturnStatement ||
				statement instanceof ThrowStatement);
		if (!isVarDecl && !isLastRT) {
			IfStatement ifs = statement.getAST().newIfStatement();
			Statement stCopy = (Statement) ASTNode.copySubtree(statement.getAST(), statement);
			Expression ifExpCopy = (Expression) ASTNode.copySubtree(statement.getAST(), ifExp);
			ifs.setExpression(ifExpCopy);
			ifs.setThenStatement(stCopy);
			mp.addReplaceIngredient(ifs);
		}
		
		
		List<Statement> thenStatements = new ArrayList<Statement>();
		boolean isInLoop = Helper.isInLoop(statement);
		if (isInLoop) {
			BreakStatement bs = statement.getAST().newBreakStatement();
			ContinueStatement cs = statement.getAST().newContinueStatement();
			thenStatements.add(bs);
			thenStatements.add(cs);
		}
		
	
		MethodDeclaration md = Helper.getMethodDeclaration(statement);
		if (md == null) {
			addInsertChecker(notIfExp, thenStatements, mp);
			return;
		}
		
		ITypeBinding tb = md.getReturnType2() == null ? null : md.getReturnType2().resolveBinding();
		if (tb != null && tb.getQualifiedName().equals("boolean")) {
			ReturnStatement rs1 = statement.getAST().newReturnStatement();
			BooleanLiteral bl1 = statement.getAST().newBooleanLiteral(true);
			rs1.setExpression(bl1);
			
			ReturnStatement rs2 = statement.getAST().newReturnStatement();
			BooleanLiteral bl2 = statement.getAST().newBooleanLiteral(false);
			rs2.setExpression(bl2);
			
			thenStatements.add(rs1);
			thenStatements.add(rs2);
		} else if (tb != null && tb.getQualifiedName().equals("void")) {
			ReturnStatement rs = statement.getAST().newReturnStatement();
			thenStatements.add(rs);
		} else if (tb != null && tb.isPrimitive()) {
			ReturnStatement rs = statement.getAST().newReturnStatement();
			NumberLiteral bl = statement.getAST().newNumberLiteral("0");
			rs.setExpression(bl);
			thenStatements.add(rs);
		} else if (tb != null && (tb.isClass() || tb.isInterface() || tb.isArray())) {
			ReturnStatement rs = statement.getAST().newReturnStatement();
			NullLiteral nl = statement.getAST().newNullLiteral();
			rs.setExpression(nl);
			thenStatements.add(rs);
		}
		
		int numberOfStatements = md.getBody().statements().size();
		Statement lastStatement = (Statement) md.getBody().statements().get(numberOfStatements - 1);
		if (lastStatement instanceof ReturnStatement) {
			VarMethodInfoExtractor sie = new VarMethodInfoExtractor(lastStatement);
			sie.extract();
			if (IngredientUtil.isInMethodScope(mp, sie)
					&& IngredientUtil.isInVarScope(mp, sie)) {
				thenStatements.add(lastStatement);
			}
		}
		
		CompilationUnit cu = (CompilationUnit) statement.getRoot();
		List<Statement> throwStsFromMethod = Helper.getThrowStatementsFromMethodThrow(md);
		List<Statement> throwStsFromImport = Helper.getThrowStatementsFromImportThrow(cu);
		thenStatements.addAll(throwStsFromMethod);
		thenStatements.addAll(throwStsFromImport);
		
		if (isInParams) {
			ThrowStatement argThr = Helper.getThrowStatement(statement.getAST(), "java.lang.IllegalArgumentException");
			thenStatements.add(argThr);
		}
		
		addInsertChecker(notIfExp, thenStatements, mp);
	}
	
	
	void handleNullPointerChecker(NullPointerChecker nullPointerChecker, ExtendedModificationPoint mp) {
		if (nullPointerChecker.getSize() == 0)
			return;
		
		Statement statement = mp.getStatement();
		
		boolean isInParams = false;
		for (Expression exp : nullPointerChecker.getReferences()) {
			if (Helper.isInParameter(exp)) {
				isInParams = true;
				break;
			}
		}
		
		Expression exp =  nullPointerChecker.getReferences().get(0);
		InfixExpression ifExp = Helper.getIfNotNullExpression(exp);
		
		for (int i = 1; i < nullPointerChecker.getSize(); i++) {
			Expression exp2 =  nullPointerChecker.getReferences().get(i);
			InfixExpression ifExp2 = Helper.getIfNotNullExpression(exp2);
			
			InfixExpression temp = statement.getAST().newInfixExpression();
			temp.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			temp.setLeftOperand(ifExp);
			temp.setRightOperand(ifExp2);
			ifExp = temp;
		}
		
		handleChecker(ifExp, mp, isInParams);
		addInsertInitialization(nullPointerChecker, mp);
	}
	
	void handleRangeChecker(RangeChecker rangeChecker, ExtendedModificationPoint mp) {
		if (rangeChecker.getSize() == 0)
			return;
		
		Statement statement = mp.getStatement();
		
		boolean isInParams = false;
		for (int i = 0; i < rangeChecker.getSize(); i++) {
			Expression exp = rangeChecker.getExpression(i);
			if (Helper.isInParameter(exp)) {
				isInParams = true;
				break;
			}
		}
		
		Expression ifExp = Helper.getRangeCheckExpression(rangeChecker.getExpression(0), rangeChecker.getIndex(0),
				rangeChecker.getType(0));

		for (int i = 1; i < rangeChecker.getSize(); i++) {
			Expression node = rangeChecker.getExpression(i);
			Expression index = rangeChecker.getIndex(i);
			String type = rangeChecker.getType(i);
			
			InfixExpression ie = statement.getAST().newInfixExpression();
			
			ie.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			ie.setLeftOperand(ifExp);
			
			Expression right = Helper.getRangeCheckExpression(node, index, type);
			ie.setRightOperand(right);
			ifExp = ie;
		}
		
		handleChecker(ifExp, mp, isInParams);
	}
	
	@SuppressWarnings("unchecked")
	void handleCastChecker(CastChecker castChecker, ExtendedModificationPoint mp) {
		if (castChecker.getSize() == 0)
			return;
		
		Statement statement = mp.getStatement();
		
		boolean isInParams = false;
		for (CastExpression ce : castChecker.getCastExpressions()) {
			if (Helper.isInParameter(ce)) {
				isInParams = true;
				break;
			}
		}

		Expression ifExp = null;
		if (castChecker.getSize() == 1) {
			CastExpression ce = castChecker.getCastExpressions().get(0);
			InstanceofExpression ioe = Helper.getInstanceofExpression(ce);
			ifExp = ioe;
		} else {
			InfixExpression ie = statement.getAST().newInfixExpression();
			CastExpression ce1 = castChecker.getCastExpressions().get(0);
			CastExpression ce2 = castChecker.getCastExpressions().get(1);
			ie.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			ie.setLeftOperand(Helper.getInstanceofExpression(ce1));
			ie.setRightOperand(Helper.getInstanceofExpression(ce2));

			for (int i = 2; i < castChecker.getSize(); i++) {
				CastExpression ce = castChecker.getCastExpressions().get(i);
				ie.extendedOperands().add(Helper.getInstanceofExpression(ce));
			}
			ifExp = ie;
		}

		handleChecker(ifExp, mp, isInParams);
		
	}
	
	void handleDivideZeroChecker(DivideZeroChecker divideZeroChecker, ExtendedModificationPoint mp) {
		if (divideZeroChecker.getSize() == 0)
			return;
		
		Statement statement = mp.getStatement();
		
		boolean isInParams = false;
		for (ASTNode node : divideZeroChecker.getDivisors()) {
			if (Helper.isInParameter(node)) {
				isInParams = true;
				break;
			}
		}
		
		Expression exp = divideZeroChecker.getDivisors().get(0);
		InfixExpression ifExp = Helper.getIfNotZeroExpression(exp);
		
		for (int i = 1; i < divideZeroChecker.getSize(); i++) {
			Expression exp2 = divideZeroChecker.getDivisors().get(i);
			InfixExpression ifExp2 = Helper.getIfNotZeroExpression(exp2);
			
			InfixExpression temp = statement.getAST().newInfixExpression();
			temp.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			temp.setLeftOperand(ifExp);
			temp.setRightOperand(ifExp2);
			ifExp = temp;
		}
		
		handleChecker(ifExp, mp, isInParams);
	}
	

	
	void handleElementReplacers(List<ElementReplacer> elementReplacers, ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		for (ElementReplacer er : elementReplacers)
			handleElementReplacer(er, mp);
	}
	
	void handleElementReplacer(ElementReplacer elementReplacer, ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		List<ASTNode> replacements = elementReplacer.getReplacements();
		ASTNode target = elementReplacer.getTarget();
		
		for (ASTNode replacement : replacements) 
			handleOneReplacement(target, replacement, mp);
	}
	
	void handleOneReplacement(ASTNode target, ASTNode replacement, ExtendedModificationPoint mp) throws MalformedTreeException, BadLocationException {
		String sourceFilePath = mp.getSourceFilePath();
		Document doc = new Document(sourceContents.get(sourceFilePath));
		
		Statement statement = mp.getStatement();
		
		ASTRewrite rewriter = ASTRewrite.create(statement.getAST());
		ITrackedNodePosition position = rewriter.track(statement);
		
		rewriter.replace(target, replacement, null);
		
		TextEdit edits = rewriter.rewriteAST(doc, null);
		edits.apply(doc);
		
		int start = position.getStartPosition();
		int len = position.getLength();
		
		String repStr = doc.get().substring(start, start + len);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(repStr.toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		Block block = (Block) parser.createAST(null);
		
		if (block.statements().size() > 0) {
			Statement repStatement = (Statement) block.statements().get(0);
			mp.addReplaceIngredient(repStatement);
		}
	}
	
	public ASTNode getVisitedNode(Statement statement) {
		ASTNode node = statement;
		if (statement instanceof IfStatement) 
			node = ((IfStatement) statement).getExpression();
		else if (statement instanceof ForStatement) 
			node = ((ForStatement) statement).getExpression();
		else if (statement instanceof EnhancedForStatement) 
			node = ((EnhancedForStatement) statement).getExpression();
		else if (statement instanceof WhileStatement)
			node =  ((WhileStatement) statement).getExpression();
		else if (statement instanceof DoStatement) 
			node =  ((DoStatement) statement).getExpression();
		else if (statement instanceof TryStatement)
			node = null;
		
		return node;
	}
	
	
	void addInsertChecker(Expression notIfExp, List<Statement> thenStatements, 
			ExtendedModificationPoint mp) {
		for (int j = 0; j < thenStatements.size(); j++) {
			IfStatement ifs = Helper.getIfStatementForNullChecker(notIfExp, thenStatements.get(j));
			mp.addInsertIngredient(ifs);
		}
	}
	
	@SuppressWarnings("unchecked")
	void addInsertInitialization(NullPointerChecker nullPointerChecker, ExtendedModificationPoint mp) {
		AST ast = mp.getStatement().getAST();
		
		Block block = ast.newBlock();
		for (Map.Entry<String, Expression> entry : nullPointerChecker.references.entrySet()) {
			String refStr = entry.getKey();
			Expression ref = entry.getValue();
			
			ITypeBinding tb = ref.resolveTypeBinding();
			
			if (!tb.isClass() || (ref instanceof MethodInvocation))
				continue;

			ASTParser parser = ASTParser.newParser(AST.JLS8);
	    	String stStr = (refStr + "= new " + tb.getName() + "();");
	    	parser.setSource(stStr.toCharArray());
	    	parser.setKind(ASTParser.K_STATEMENTS);
	    	Block bl = (Block) parser.createAST(null);
	    	
			if (!bl.statements().isEmpty()) {
				Statement stCopy = (Statement) ASTNode.copySubtree(ref.getAST(), (Statement) (bl.statements().get(0)));
				block.statements().add(stCopy);
			}
		}
		
		if (!block.statements().isEmpty())
			mp.addInsertIngredient(block);
	}

}
