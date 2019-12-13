package us.msu.cse.repair.core.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;


import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import us.msu.cse.repair.core.parser.ExtendedModificationPoint;
import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.templates.ElementReplacer;
import us.msu.cse.repair.core.util.Helper;

public class TemplatePreparation {
	static String[] compareOperators = {">", ">=", "<", "<=", "==", "!="};
	static String[] conditionalOperators = {"&&", "||"};
	static String[] logicalOperators = {"^", "&", "|"};
	static String[] shiftOperators = {"<<", ">>", ">>>"};
	static String[] primitiveTypes = {"byte", "short", "int", "long", "float", "double"};
	
	public static ElementReplacer createReplacerForSimpleName(SimpleName sn, IVariableBinding vb,
			ExtendedModificationPoint mp, ASTNode component) {
		Map<String, VarInfo> localVars = mp.getLocalVars();
		Map<String, VarInfo> declaredFields = mp.getDeclaredFields();
		Map<String, VarInfo> inheritedFields = mp.getInheritedFields();
		Map<String, VarInfo> outerFields = mp.getOuterFields();
		Set<IVariableBinding> availableStaticFields = mp.getAvailableStaticFields();

		Map<String, MethodInfo> delcaredMethods = mp.getDeclaredMethods();
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
		Map<String, MethodInfo> outerMethods = mp.getOuterMethods();
		Set<IMethodBinding> availableStaticMethods = mp.getAvailableStaticMethods();

		VarInfo varInfo = Helper.getVarInfo(vb);
		String varName = sn.getIdentifier();
		List<ASTNode> replacements = new ArrayList<ASTNode>();

		lookVarMap(varName, varInfo, localVars, mp, replacements);
		lookVarMap(varName, varInfo, declaredFields, mp, replacements);
		lookVarMap(varName, varInfo, inheritedFields, mp, replacements);
		lookVarMap(varName, varInfo, outerFields, mp, replacements);
		lookStaticVarSet(varInfo, availableStaticFields, mp, replacements);

		if (component.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			lookMethodMap(vb, component, delcaredMethods, mp, replacements);
			lookMethodMap(vb, component, inheritedMethods, mp, replacements);
			lookMethodMap(vb, component, outerMethods, mp, replacements);
			lookStaticMethodSet(vb, component, availableStaticMethods, mp, replacements);
		}

		ElementReplacer er = new ElementReplacer(sn, replacements);
		return er;
	}

	// this.x  or this.x.y
	public static ElementReplacer createReplacerForFieldAccess_1(FieldAccess fa, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		ElementReplacer er = createReplacerForSimpleName(fa.getName(), vb, mp, fa);
		er.setTarget(fa);
		return er;
	}

	// get().x
	@SuppressWarnings("unchecked")
	public static ElementReplacer createReplacerForFieldAccess_2(FieldAccess fa, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		Expression exp = fa.getExpression();
		SimpleName sn = fa.getName();

		ITypeBinding expType = exp.resolveTypeBinding();
		ITypeBinding snType = sn.resolveTypeBinding();
			
		IMethodBinding[] declaredMethods = expType.getDeclaredMethods();
		IVariableBinding[] declaredFields = expType.getDeclaredFields();
		
		boolean isSamePackage = Helper.isInSamePackage(expType, mp.getStatement());
		boolean isOrgFinal = Modifier.isFinal(vb.getVariableDeclaration().getModifiers());
		
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		for (IVariableBinding field : declaredFields) {
			int mod = field.getModifiers();
			
			boolean isPrivate = Modifier.isPrivate(mod);
			boolean isProtected = Modifier.isProtected(mod);
			boolean isPackagePrivate = Helper.isPackagePrivate(mod);
			
			boolean isStatic = Modifier.isStatic(mod);
			boolean isEqual = sn.getIdentifier().equals(field.getName());
			boolean isFinal = Modifier.isFinal(mod);
			
			if (isPrivate || isStatic || isEqual)
				continue;
			if (isProtected && !isSamePackage)
				continue;
			if (isPackagePrivate && !isSamePackage)
				continue;
			if (!isOrgFinal && isFinal)
				continue;	
			
			ITypeBinding fieldType = field.getVariableDeclaration().getType();
			
			if (fieldType.isAssignmentCompatible(snType)) {
				SimpleName rep = mp.getStatement().getAST().newSimpleName(field.getName());
				replacements.add(rep);
			}	
		}
		
		if (fa.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			for (IMethodBinding method : declaredMethods) {
				int mod = method.getModifiers();
				boolean isStatic = Modifier.isStatic(mod);	
				
				boolean isPrivate = Modifier.isPrivate(mod);
				boolean isProtected = Modifier.isProtected(mod);
				boolean isPackagePrivate = Helper.isPackagePrivate(mod);
				boolean isFinal = Modifier.isFinal(mod);
				
				if (isPrivate || isStatic)
					continue;
				if (isProtected && !isSamePackage)
					continue;
				if (isPackagePrivate && !isSamePackage)
					continue;
				if (method.getParameterTypes().length > 1)
					continue;
				if (!isOrgFinal && isFinal)
					continue;	
		
				ITypeBinding returnType = method.getReturnType();				
				if (!returnType.isAssignmentCompatible(snType))
					continue;
				
				MethodInvocation mi = mp.getStatement().getAST().newMethodInvocation();
				SimpleName mname = mp.getStatement().getAST().newSimpleName(method.getName());
				mi.setName(mname);
				if (method.getParameterTypes().length == 0)
					replacements.add(mi);
				else {
					ITypeBinding paramType = method.getParameterTypes()[0];
					if (snType.isAssignmentCompatible(paramType)) {
						SimpleName arg = mp.getStatement().getAST().newSimpleName(sn.getIdentifier());
						mi.arguments().add(arg);
						replacements.add(mi);
					}
				}
					
			}
		}

		ElementReplacer er = new ElementReplacer(fa.getName(), replacements);
		return er;
	}

	public static ElementReplacer createReplacerForSuperFieldAccess(SuperFieldAccess sfa, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		ElementReplacer er = createReplacerForSimpleName(sfa.getName(), vb, mp, sfa);
		er.setTarget(sfa);
		return er;
	}

	// a.b for a
	public static ElementReplacer createReplacerForQualifiedName_1(QualifiedName qn, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		SimpleName sn = (SimpleName) (qn.getQualifier());
		ElementReplacer er = createReplacerForSimpleName(sn, vb, mp, sn);
		return er;
	}

	// a.b for b
	@SuppressWarnings("unchecked")
	public static ElementReplacer createReplacerForQualifiedName_2(QualifiedName qn, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		Name qualifierNode = qn.getQualifier();
		
		ITypeBinding qualifierType = qualifierNode.resolveTypeBinding();
		ITypeBinding nameType = vb.getVariableDeclaration().getType();
		
		boolean isClassName = Helper.isClassName(qualifierNode);
		boolean isOrgFinal = Modifier.isFinal(vb.getVariableDeclaration().getModifiers());		
		
		IMethodBinding[] declaredMethods = qualifierType.getDeclaredMethods();
		IVariableBinding[] declaredFields = qualifierType.getDeclaredFields();
		
		List<ASTNode> replacements = new ArrayList<ASTNode>();

		boolean isSamePackage = Helper.isInSamePackage(qualifierType, mp.getStatement());
		
		for (IVariableBinding field : declaredFields) {
			int mod = field.getModifiers();
			boolean isStatic = Modifier.isStatic(mod);
			
			boolean isPrivate = Modifier.isPrivate(mod);
			boolean isProtected = Modifier.isProtected(mod);
			boolean isPackagePrivate = Helper.isPackagePrivate(mod);
			
			boolean isEqual = vb.getName().equals(field.getName());
			boolean isFinal = Modifier.isFinal(mod);
			
			if (isPrivate || isEqual)
				continue;
			
			if (isProtected && !isSamePackage)
				continue;
			if (isPackagePrivate && !isSamePackage)
				continue;
			
			if (isClassName && !isStatic)
				continue;
			
			if (!isClassName && isStatic)
				continue;
			
			if (!isOrgFinal && isFinal)
				continue;
			
			ITypeBinding fieldType = field.getVariableDeclaration().getType();
			
			if (fieldType.isAssignmentCompatible(nameType)) {
				SimpleName rep = mp.getStatement().getAST().newSimpleName(field.getName());
				replacements.add(rep);
			}	
		}
		
		if (qn.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			for (IMethodBinding method : declaredMethods) {
				int mod = method.getModifiers();
				boolean isPrivate = Modifier.isPrivate(mod);
				boolean isProtected = Modifier.isProtected(mod);
				boolean isPackagePrivate = Helper.isPackagePrivate(mod);
				
				boolean isStatic = Modifier.isStatic(mod);	
				boolean isFinal = Modifier.isFinal(mod);
				
				if (method.getParameterTypes().length > 1)
					continue;
				if (isClassName && method.getParameterTypes().length == 1)
					continue;
				if (isPrivate)
					continue;
				if (isProtected && !isSamePackage)
					continue;
				if (isPackagePrivate && !isSamePackage)
					continue;
				if (isClassName && !isStatic)
					continue;
				if (!isClassName && isStatic)
					continue;
				
				if (!isOrgFinal && isFinal)
					continue;
				
				ITypeBinding returnType = method.getReturnType();
				
				if (!returnType.isAssignmentCompatible(nameType))
					continue;
				
				MethodInvocation mi = mp.getStatement().getAST().newMethodInvocation();
				SimpleName mname = mp.getStatement().getAST().newSimpleName(method.getMethodDeclaration().getName());
				mi.setName(mname);
				if (method.getParameterTypes().length == 0)
					replacements.add(mi);
				else {
					ITypeBinding paramType = method.getParameterTypes()[0];
					if (nameType.isAssignmentCompatible(paramType)) {
						SimpleName arg = mp.getStatement().getAST().newSimpleName(vb.getName());
						mi.arguments().add(arg);
						replacements.add(mi);
					}
				}
					
			}
		}

		ElementReplacer er = new ElementReplacer(qn.getName(), replacements);
		return er;
	}

	// a.b whole
	public static ElementReplacer createReplacerForQualifiedName_3(QualifiedName qn, IVariableBinding vb,
			ExtendedModificationPoint mp) {
		SimpleName sn = qn.getName();	
		ElementReplacer er = createReplacerForSimpleName(sn, vb, mp, qn);
		er.setTarget(qn);
		return er;
	}

	public static ElementReplacer createReplacerForMethodInvocation(MethodInvocation mi, IMethodBinding mb,
			ExtendedModificationPoint mp) {
		Expression exp = mi.getExpression();
		String methodName = mi.getName().getIdentifier();

		Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
		Map<String, MethodInfo> outerMethods = mp.getOuterMethods();
		
		boolean isConstructor = mb.isConstructor();

		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		ITypeBinding curReturn = mb.getReturnType();
		ITypeBinding[] curParams = new ITypeBinding[mi.arguments().size()];	
		for (int i = 0; i < curParams.length; i++) {
			Expression arg = (Expression) mi.arguments().get(i);
			curParams[i] = arg.resolveTypeBinding();
		}	

		if (exp == null) {
			findInMethodMap(methodName, curParams, curReturn, isConstructor, declaredMethods, mp, replacements);
			findInMethodMap(methodName, curParams, curReturn, isConstructor, inheritedMethods, mp, replacements);
			findInMethodMap(methodName, curParams, curReturn, isConstructor, outerMethods, mp, replacements);
		} else if (exp instanceof ThisExpression) {
			findInMethodMap(methodName, curParams, curReturn, isConstructor, declaredMethods, mp, replacements);
			findInMethodMap(methodName, curParams, curReturn, isConstructor, inheritedMethods, mp, replacements);
		} else {
			ITypeBinding tb = exp.resolveTypeBinding();
			IMethodBinding[] methodBindings = tb.getDeclaredMethods();
			
			boolean isClassName = Helper.isClassName(mi.getExpression());
			boolean isSamePackage = Helper.isInSamePackage(tb, mp.getStatement());

			for (IMethodBinding methodBinding : methodBindings) {
				int mod = methodBinding.getModifiers();
				boolean isStatic = Modifier.isStatic(mod);
				boolean isPrivate = Modifier.isPrivate(mod);
				boolean isProtected = Modifier.isProtected(mod);
				boolean isPackagePrivate = Helper.isPackagePrivate(mod);
				if (isPrivate)
					continue;
				
				if (isProtected && !isSamePackage)
					continue;
				
				if (isPackagePrivate && !isSamePackage)
					continue;
				
				if (isClassName && !isStatic)
					continue;

				if (!isClassName && isStatic)
					continue;
				

				if (methodBinding.getName().equals(methodName))
					continue;

				if (Helper.isReplaceableMethod(curParams, curReturn, isConstructor, methodBinding)) {
					SimpleName newName = mp.getStatement().getAST()
							.newSimpleName(methodBinding.getMethodDeclaration().getName());
					replacements.add(newName);
				}
			}

		}

		ElementReplacer er = new ElementReplacer(mi.getName(), replacements);
		return er;
	}

	public static ElementReplacer createReplacerForSuperMethodInvocation(SuperMethodInvocation smi, IMethodBinding mb,
			ExtendedModificationPoint mp) {
		String methodName = smi.getName().getIdentifier();

		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		ITypeBinding curReturn = mb.getReturnType();
		ITypeBinding[] curParams = new ITypeBinding[smi.arguments().size()];	
		
		boolean isConstructor = mb.isConstructor();
		
		for (int i = 0; i < curParams.length; i++) {
			Expression arg = (Expression) smi.arguments().get(i);
			curParams[i] = arg.resolveTypeBinding();
		}	

		findInMethodMap(methodName, curParams, curReturn, isConstructor, inheritedMethods, mp, replacements);

		ElementReplacer er = new ElementReplacer(smi.getName(), replacements);
		return er;
	}

	static void findInMethodMap(String methodName, ITypeBinding[] curParams, ITypeBinding curReturn, boolean isConstructor,
			Map<String, MethodInfo> map,
			ExtendedModificationPoint mp, List<ASTNode> replacements) {
		
		
		for (Map.Entry<String, MethodInfo> entry : map.entrySet()) {
			String strs[] = entry.getKey().split(":");
			String name = strs[0];
			int paramNum = Integer.parseInt(strs[1]);
			MethodInfo mi = entry.getValue();

			if (name.equals(methodName))
				continue;
			if (paramNum != curParams.length)
				continue;

			for (int i = 0; i < mi.getSize(); i++) {
				IMethodBinding rep = mi.getMethodBinding(i);
				
				if (rep == null)
					continue;

				if (Helper.isReplaceableMethod(curParams, curReturn, isConstructor, rep)) {
					SimpleName newName = mp.getStatement().getAST().newSimpleName(rep.getName());
					replacements.add(newName);
					break;
				}
			}
		}
	}

	public static ElementReplacer createReplacerForBooleanLiteral(BooleanLiteral bl, ExtendedModificationPoint mp) {
		boolean flag = bl.booleanValue();
		BooleanLiteral nbl = mp.getStatement().getAST().newBooleanLiteral(!flag);
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		replacements.add(nbl);

		ElementReplacer er = new ElementReplacer(bl, replacements);
		return er;
	}

	public static ElementReplacer createReplacerForNumberLiteral(NumberLiteral nl, ExtendedModificationPoint mp) {
		List<NumberLiteral> nearbyNumberLiterals = mp.getNearbyNumberLiterals();
		ITypeBinding tb = nl.resolveTypeBinding();

		Set<String> existingTokens = new HashSet<String>();
		existingTokens.add(nl.getToken());

		List<ASTNode> replacements = new ArrayList<ASTNode>();
	
		for (NumberLiteral rep : nearbyNumberLiterals) {
			String token = rep.getToken();
			if (existingTokens.contains(token))
				continue;

			ITypeBinding repType = rep.resolveTypeBinding();

			if (repType.isAssignmentCompatible(tb)) {
				existingTokens.add(token);
				NumberLiteral nnl = mp.getStatement().getAST().newNumberLiteral(token);
				replacements.add(nnl);
			}
		}

		StructuralPropertyDescriptor spd = nl.getLocationInParent();

		if (!tb.getName().equals("double")) {
			String token = null;
			if (spd == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
				Assignment as = (Assignment) (nl.getParent());
				if (as.getOperator() == Assignment.Operator.DIVIDE_ASSIGN)
					token = Helper.convert2Double(nl.getToken());

			} else if (spd == InfixExpression.RIGHT_OPERAND_PROPERTY) {
				InfixExpression ie = (InfixExpression) (nl.getParent());
				if (ie.getOperator() == Operator.DIVIDE)
					token = Helper.convert2Double(nl.getToken());
			} else if (spd == MethodInvocation.ARGUMENTS_PROPERTY) {
				MethodInvocation mi = (MethodInvocation) (nl.getParent());
				int index = mi.arguments().indexOf(nl);
				ITypeBinding pt = mi.resolveMethodBinding().getParameterTypes()[index];
				String qn = pt.getQualifiedName();
				if (qn.equals("java.lang.Object") || qn.equals("double"))
					token = Helper.convert2Double(nl.getToken());
			}
			
			if (token != null && !existingTokens.contains(token)) {
				NumberLiteral nnl = mp.getStatement().getAST().newNumberLiteral(token);
				replacements.add(nnl);
			}
		}

		ElementReplacer er = new ElementReplacer(nl, replacements);
		return er;
	}
	
	public static ElementReplacer createReplacerForInfixExpression(InfixExpression ie) {	
		if (Helper.isInForExpression(ie)) {
			List<ASTNode> replacements = new ArrayList<ASTNode>();
			
			if (ie.getOperator() == InfixExpression.Operator.GREATER) {
				InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
				ieCopy.setOperator(InfixExpression.Operator.GREATER_EQUALS);
				replacements.add(ieCopy);
			}
			else if (ie.getOperator() == InfixExpression.Operator.GREATER_EQUALS) {
				InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
				ieCopy.setOperator(InfixExpression.Operator.GREATER);
				replacements.add(ieCopy);
			}
			else if (ie.getOperator() == InfixExpression.Operator.LESS) {
				InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
				ieCopy.setOperator(InfixExpression.Operator.LESS_EQUALS);
				replacements.add(ieCopy);
			}
			else if (ie.getOperator() == InfixExpression.Operator.LESS_EQUALS) {
				InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
				ieCopy.setOperator(InfixExpression.Operator.LESS);
				replacements.add(ieCopy);
			}
			ElementReplacer er = new ElementReplacer(ie, replacements);
			return er;
		}
		
		String opStr = ie.getOperator().toString();
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		ITypeBinding tb = ie.getLeftOperand().resolveTypeBinding();
		ITypeBinding tbr = ie.getRightOperand().resolveTypeBinding();
		boolean flag1 = Arrays.asList(primitiveTypes).contains(tb.getQualifiedName());
		boolean flag2 = (tb.getSuperclass() != null)
				&& (tb.getSuperclass().getQualifiedName().equals("java.lang.Number"));
		boolean flag1r = Arrays.asList(primitiveTypes).contains(tbr.getQualifiedName());
		boolean flag2r = (tbr.getSuperclass() != null)
				&& (tbr.getSuperclass().getQualifiedName().equals("java.lang.Number"));
		boolean flag3 = (flag1 || flag2);
		boolean flag3r = (flag1r || flag2r);
		
		boolean isStr = tb.getQualifiedName().equals("java.lang.String");

		if (Arrays.asList(compareOperators).contains(opStr)) {
			if (flag3 && flag3r)
				lookOperatorList(ie, opStr, compareOperators, replacements);
			else {
				if (opStr.equals("==")) {
					InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
					ieCopy.setOperator(InfixExpression.Operator.NOT_EQUALS);
					replacements.add(ieCopy);
				}
				else if (opStr.equals("!=")) {
					InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
					ieCopy.setOperator(InfixExpression.Operator.EQUALS);
					replacements.add(ieCopy);
				}
			}
		}
		else if (Arrays.asList(conditionalOperators).contains(opStr)) 
			lookOperatorList(ie, opStr, conditionalOperators, replacements);
		else if (Arrays.asList(logicalOperators).contains(opStr)) 
			lookOperatorList(ie, opStr, logicalOperators, replacements);
		else if (Arrays.asList(shiftOperators).contains(opStr)) 
			lookOperatorList(ie, opStr, shiftOperators, replacements);
		else if (opStr.equals("*")) {
			InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
			ieCopy.setOperator(InfixExpression.Operator.DIVIDE);
			replacements.add(ieCopy);
		}
		else if (opStr.equals("/")) {
			InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
			ieCopy.setOperator(InfixExpression.Operator.TIMES);
			replacements.add(ieCopy);
		}
		else if (opStr.equals("+") && !isStr) {
			InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
			ieCopy.setOperator(InfixExpression.Operator.MINUS);
			replacements.add(ieCopy);
		}
		else if (opStr.equals("-")) {
			InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
			ieCopy.setOperator(InfixExpression.Operator.PLUS);
			replacements.add(ieCopy);
		}
		
		ElementReplacer er = new ElementReplacer(ie, replacements);
		return er;
	}
	
	
	
	
	public static ElementReplacer createReplacerForAssignment(Assignment as) {
		String opStr = as.getOperator().toString();
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		if (opStr.equals("+=")) {
			Assignment asCopy1 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy1.setOperator(Assignment.Operator.MINUS_ASSIGN);
			replacements.add(asCopy1);
			
			Assignment asCopy2 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy2.setOperator(Assignment.Operator.ASSIGN);
			replacements.add(asCopy2);
			
		} else if (opStr.equals("-=")) {
			Assignment asCopy1 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy1.setOperator(Assignment.Operator.PLUS_ASSIGN);
			replacements.add(asCopy1);
			
			Assignment asCopy2 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy2.setOperator(Assignment.Operator.ASSIGN);
			replacements.add(asCopy2);
		} else if (opStr.equals("*=")) {
			Assignment asCopy1 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy1.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
			replacements.add(asCopy1);
			
			Assignment asCopy2 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy2.setOperator(Assignment.Operator.ASSIGN);
			replacements.add(asCopy2);
		} else if (opStr.equals("/=")) {
			Assignment asCopy1 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy1.setOperator(Assignment.Operator.TIMES_ASSIGN);
			replacements.add(asCopy1);
			
			Assignment asCopy2 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy2.setOperator(Assignment.Operator.ASSIGN);
			replacements.add(asCopy2);
			
			Assignment asCopy3 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy3.setOperator(Assignment.Operator.REMAINDER_ASSIGN);
			replacements.add(asCopy3);		
		}
		else if (opStr.equals("%=")) {
			Assignment asCopy1 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy1.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
			replacements.add(asCopy1);
			
			Assignment asCopy2 = (Assignment) ASTNode.copySubtree(as.getAST(), as);
			asCopy2.setOperator(Assignment.Operator.ASSIGN);
			replacements.add(asCopy2);
		}

		ElementReplacer er = new ElementReplacer(as, replacements);
		return er;
	}

	public static ElementReplacer createReplacerForPrimitiveType(PrimitiveType pt) {
		String typeStr = pt.getPrimitiveTypeCode().toString();
		List<String> ptTypes = Arrays.asList(primitiveTypes);
		int index = ptTypes.indexOf(typeStr);
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		if (index != -1) {
			for (int i = index + 1; i < ptTypes.size(); i++) {
				String type = ptTypes.get(i);
				PrimitiveType npt = pt.getAST().newPrimitiveType(PrimitiveType.toCode(type));
				replacements.add(npt);
			}
		}
		
		ElementReplacer er = new ElementReplacer(pt, replacements);
		return er;
	}
	
	public static ElementReplacer createReplacerForPrefixExpression(PrefixExpression pre)  {
		PrefixExpression.Operator op = pre.getOperator();
		
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		if (op == PrefixExpression.Operator.DECREMENT) {
			PrefixExpression preCopy = (PrefixExpression) ASTNode.copySubtree(pre.getAST(), pre);
			preCopy.setOperator(PrefixExpression.Operator.INCREMENT);
			replacements.add(preCopy);
		}
		else if (op == PrefixExpression.Operator.INCREMENT) {
			PrefixExpression preCopy = (PrefixExpression) ASTNode.copySubtree(pre.getAST(), pre);
			preCopy.setOperator(PrefixExpression.Operator.DECREMENT);
			replacements.add(preCopy);
		}
		else if (op == PrefixExpression.Operator.PLUS) {
			PrefixExpression preCopy = (PrefixExpression) ASTNode.copySubtree(pre.getAST(), pre);
			preCopy.setOperator(PrefixExpression.Operator.MINUS);
			replacements.add(preCopy);
		}
		else if (op == PrefixExpression.Operator.MINUS) {
			PrefixExpression preCopy = (PrefixExpression) ASTNode.copySubtree(pre.getAST(), pre);
			preCopy.setOperator(PrefixExpression.Operator.PLUS);
			replacements.add(preCopy);
		}
		else if (op == PrefixExpression.Operator.NOT) {
			Expression expCopy = (Expression) ASTNode.copySubtree(pre.getAST(), pre.getOperand());
			replacements.add(expCopy);
		}
		ElementReplacer er = new ElementReplacer(pre, replacements);
		return er;
	}
	
	public static ElementReplacer createReplacerForPostfixExpression(PostfixExpression poe)  {
		PostfixExpression.Operator op = poe.getOperator();
		
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		if (op == PostfixExpression.Operator.DECREMENT) {
			PostfixExpression poeCopy = (PostfixExpression) ASTNode.copySubtree(poe.getAST(), poe);
			poeCopy.setOperator(PostfixExpression.Operator.INCREMENT);
			replacements.add(poeCopy);
		}
		else if (op == PostfixExpression.Operator.INCREMENT) {
			PostfixExpression poeCopy = (PostfixExpression) ASTNode.copySubtree(poe.getAST(), poe);
			poeCopy.setOperator(PostfixExpression.Operator.DECREMENT);
			replacements.add(poeCopy);
		}
		
		ElementReplacer er = new ElementReplacer(poe, replacements);
		return er;
	}
	
	public static ElementReplacer createReplacerForConditionalExpression(ConditionalExpression ce) {		
		Expression thenExp = ce.getThenExpression();
		Expression elseExp = ce.getElseExpression();
		
		List<ASTNode> replacements = new ArrayList<ASTNode>();
		
		Expression thenExpCopy = (Expression) ASTNode.copySubtree(ce.getAST(), thenExp);
		Expression elseCopy = (Expression) ASTNode.copySubtree(ce.getAST(), elseExp);
		
		replacements.add(thenExpCopy);
		replacements.add(elseCopy);
		
		ElementReplacer er = new ElementReplacer(ce, replacements);
		return er;
	}
	
	public static OverloadedMethodDetector detectOverloadedMethods(ClassInstanceCreation currentMethod,
			ExtendedModificationPoint mp) {
		List<IMethodBinding> overloadedMethods = new ArrayList<IMethodBinding>();
		
		ITypeBinding tb = currentMethod.resolveTypeBinding();	
		boolean isSamePackage = Helper.isInSamePackage(tb, mp.getStatement());
		IMethodBinding curMethodBinding = currentMethod.resolveConstructorBinding();
		
		if (tb != null) {
			IMethodBinding[] methodBindings = tb.getDeclaredMethods();
			for (IMethodBinding mb : methodBindings) {
				if (!(mb.isConstructor()))
					continue;
				if (mb.getMethodDeclaration() == curMethodBinding.getMethodDeclaration())
					continue;
				
				int mod = mb.getModifiers();
				
				boolean isPrivate = Modifier.isPrivate(mod);
				boolean isProtected = Modifier.isProtected(mod);
				boolean isPackagePrivate = Helper.isPackagePrivate(mod);

				if (isPrivate)
					continue;
				if (isProtected && !isSamePackage)
					continue;
				if (isPackagePrivate && !isSamePackage) 
					continue;
				
				overloadedMethods.add(mb);
			}
		}
		
		OverloadedMethodDetector omd = new OverloadedMethodDetector(currentMethod, overloadedMethods);
		return omd;
	}
	
	public static OverloadedMethodDetector detectOverloadedMethods(SuperMethodInvocation currentMethod,
			ExtendedModificationPoint mp) {
		List<IMethodBinding> overloadedMethods = new ArrayList<IMethodBinding>();
		IMethodBinding curMethodBinding = currentMethod.resolveMethodBinding();
		ITypeBinding typeDecl = curMethodBinding.getDeclaringClass();
		int curArgs = currentMethod.arguments().size();
		boolean isSamePackage = Helper.isInSamePackage(typeDecl, mp.getStatement());
		String methodName = currentMethod.getName().getIdentifier();
		
		if (typeDecl != null) {
			IMethodBinding[] methods = typeDecl.getDeclaredMethods();
			for (IMethodBinding mb : methods) {
				int nArgs = mb.getParameterTypes().length;
				
				if (!mb.getMethodDeclaration().getName().equals(methodName))
					continue;
				if (mb.getMethodDeclaration() == curMethodBinding.getMethodDeclaration())
					continue;
				
				if (Math.abs(curArgs - nArgs) > 1)
					continue;
				
				int mod = mb.getModifiers();	
				boolean isPrivate = Modifier.isPrivate(mod);
				boolean isProtected = Modifier.isProtected(mod);
				boolean isPackagePrivate = Helper.isPackagePrivate(mod);
				
				if (isPrivate)
					continue;
				if (isProtected && !isSamePackage)
					continue;
				if (isPackagePrivate && !isSamePackage) 
					continue;
				
				overloadedMethods.add(mb);
			}
		}
	
		
		OverloadedMethodDetector omd = new OverloadedMethodDetector(currentMethod, overloadedMethods);
		return omd;
	}
	
	public static OverloadedMethodDetector detectOverloadedMethods(MethodInvocation currentMethod,
			ExtendedModificationPoint mp) {

		List<IMethodBinding> overloadedMethods = new ArrayList<IMethodBinding>();
		
		Expression exp = currentMethod.getExpression();
		String methodName = currentMethod.getName().getIdentifier();
		int nargs = currentMethod.arguments().size();
		IMethodBinding currentMethodBinding = currentMethod.resolveMethodBinding();
		ITypeBinding returnType = currentMethodBinding.getReturnType();
		
		if (exp == null) {
			Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
			Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
			Map<String, MethodInfo> outerMethods = mp.getOuterMethods();
			List<MethodInfo> allMethodInfos = new ArrayList<MethodInfo>();	
			allMethodInfos.addAll(declaredMethods.values());
			allMethodInfos.addAll(inheritedMethods.values());
			allMethodInfos.addAll(outerMethods.values());
			
			for (MethodInfo mi : allMethodInfos) {
				int params = mi.getNumberOfParameters();
				if (!mi.getName().equals(methodName))
					continue;
				if (Math.abs(nargs - params) > 1)
					continue;
				
				for (int i = 0; i < mi.getSize(); i++) {
					IMethodBinding mb = mi.getMethodBinding(i);
					if (mb == null)
						continue;
					
					ITypeBinding rt = mb.getReturnType();
					
					if (mb.getMethodDeclaration() == currentMethodBinding.getMethodDeclaration())
						continue;
					
					if (!rt.isAssignmentCompatible(returnType))
						continue;
					
					overloadedMethods.add(mb);
				}
				
			}
		}
		else {
			ITypeBinding expType = exp.resolveTypeBinding();
			if (expType != null) {
				IMethodBinding[] declaredMethods = expType.getDeclaredMethods();
				boolean isClassName = Helper.isClassName(exp);
				boolean isSamePackage = Helper.isInSamePackage(expType, mp.getStatement());
				for (IMethodBinding mb : declaredMethods) {
					int mod = mb.getModifiers();
					boolean isStatic = Modifier.isStatic(mod);
					boolean isPrivate = Modifier.isPrivate(mod);
					boolean isProtected = Modifier.isProtected(mod);
					boolean isPackagePrivate = Helper.isPackagePrivate(mod);
					
					int params = mb.getParameterTypes().length;
					ITypeBinding rt = mb.getReturnType();
					
					if (!mb.getMethodDeclaration().getName().equals(methodName))
						continue;
					if (mb.getMethodDeclaration() == currentMethodBinding.getMethodDeclaration())
						continue;
					if (Math.abs(nargs - params) > 1)
						continue;
					if (isPrivate)
						continue;
					if (isProtected && !isSamePackage)
						continue;
					if (isPackagePrivate && !isSamePackage) 
						continue;
					if (isClassName && !isStatic)
						continue;
					if (!isClassName && isStatic)
						continue;
					if (!rt.isAssignmentCompatible(returnType))
						continue;
					
					overloadedMethods.add(mb);
				}
			}
		}

		OverloadedMethodDetector omd = new OverloadedMethodDetector(currentMethod, overloadedMethods);
		return omd;
	}

	static void lookOperatorList(InfixExpression ie, String opStr, String[] operators, List<ASTNode> replacements) {
		for (String op : operators) {
			if (op.equals(opStr))
				continue;
			
			InfixExpression ieCopy = (InfixExpression) ASTNode.copySubtree(ie.getAST(), ie);
			ieCopy.setOperator(InfixExpression.Operator.toOperator(op));
			replacements.add(ieCopy);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	static void lookStaticMethodSet(IVariableBinding vb, ASTNode component, Set<IMethodBinding> availableStaticMethods,
			ExtendedModificationPoint mp, List<ASTNode> replacements) {
		Statement statement = mp.getStatement();
		ITypeBinding varType = vb.getVariableDeclaration().getType();

		for (IMethodBinding mb : availableStaticMethods) {
			if (mb.getParameterTypes().length > 1)
				continue;

			ITypeBinding returnType = mb.getReturnType();

			if (!returnType.isAssignmentCompatible(varType))
				continue;

			String expStr = mb.getDeclaringClass().getName();
			String nameStr = mb.getName();

			MethodInvocation mi = statement.getAST().newMethodInvocation();
			SimpleName expression = statement.getAST().newSimpleName(expStr);
			SimpleName name = statement.getAST().newSimpleName(nameStr);
			mi.setExpression(expression);
			mi.setName(name);

			if (mb.getParameterTypes().length == 0)
				replacements.add(mi);
			else {
				ITypeBinding parameterType = mb.getParameterTypes()[0];
				if (varType.isAssignmentCompatible(parameterType)) {
					ASTNode arg = ASTNode.copySubtree(statement.getAST(), component);
					mi.arguments().add(arg);
					replacements.add(mi);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	static void lookMethodMap(IVariableBinding vb, ASTNode component, Map<String, MethodInfo> map, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		Statement statement = mp.getStatement();
		ITypeBinding varType = vb.getVariableDeclaration().getType();

		for (Map.Entry<String, MethodInfo> entry : map.entrySet()) {
			String strs[] = entry.getKey().split(":");
			String methodName = strs[0];

			int num = Integer.parseInt(strs[1]);
			MethodInfo mi = entry.getValue();

			if (num == 0) {
				for (int i = 0; i < mi.getSize(); i++) {
					ITypeBinding returnType = mi.getReturnTypeBinding(i);
					if (returnType != null && returnType.isAssignmentCompatible(varType)) {
						MethodInvocation mdinv = statement.getAST().newMethodInvocation();
						SimpleName sn = statement.getAST().newSimpleName(methodName);
						mdinv.setName(sn);

						replacements.add(mdinv);
						break;
					}
				}
			} else if (num == 1) {
				for (int i = 0; i < mi.getSize(); i++) {
					ITypeBinding returnType = mi.getReturnTypeBinding(i);
					ITypeBinding parameterTypes[] = mi.getParameterTypeBindings(i);
					if (returnType != null && returnType.isAssignmentCompatible(varType)
							&& varType.isAssignmentCompatible(parameterTypes[0])) {
						MethodInvocation mdinv = statement.getAST().newMethodInvocation();
						SimpleName sn = statement.getAST().newSimpleName(methodName);
						mdinv.setName(sn);

						ASTNode arg = ASTNode.copySubtree(statement.getAST(), component);
						mdinv.arguments().add(arg);
						replacements.add(mdinv);
						break;
					}
				}
			}
		}

	}

	static void lookVarMap(String varName, VarInfo varInfo, Map<String, VarInfo> map, ExtendedModificationPoint mp,
			List<ASTNode> replacements) {
		Statement statement = mp.getStatement();
		boolean isOrgFinal = Modifier.isFinal(varInfo.getVariableBinding().getVariableDeclaration().getModifiers());
		for (Map.Entry<String, VarInfo> entry : map.entrySet()) {
			String name = entry.getKey();
			VarInfo vi = entry.getValue();
			
			boolean isFinal = Modifier.isFinal(vi.getModifiers());
			
			if (!isOrgFinal && isFinal)
				continue;

			if (name.equals(varName))
				continue;

			if (varInfo.isStronglyTypeMatched(vi) || varInfo.isWeaklyTypeMatched(vi)) {
				SimpleName sn = statement.getAST().newSimpleName(name);
				replacements.add(sn);
			}
		}
	}

	static void lookStaticVarSet(VarInfo varInfo, Set<IVariableBinding> availableStaticFields,
			ExtendedModificationPoint mp, List<ASTNode> replacements) {
		Statement statement = mp.getStatement();
		
		IVariableBinding orgVB = varInfo.getVariableBinding().getVariableDeclaration();
		boolean isOrgFinal = Modifier.isFinal(orgVB.getModifiers());	
		boolean isParameter = orgVB.isParameter();
		
		for (IVariableBinding vb : availableStaticFields) {
			boolean isFinal = Modifier.isFinal(vb.getVariableDeclaration().getModifiers());
			if ((!isOrgFinal || isParameter) && isFinal)
				continue;
			
			VarInfo vi = Helper.getVarInfo(vb);
			if (varInfo.isStronglyTypeMatched(vi) || varInfo.isWeaklyTypeMatched(vi)) {
				String qualifierStr = vb.getDeclaringClass().getName();
				String nameStr = vb.getName();

				SimpleName qualifier = statement.getAST().newSimpleName(qualifierStr);
				SimpleName name = statement.getAST().newSimpleName(nameStr);
				QualifiedName qn = statement.getAST().newQualifiedName(qualifier, name);
				replacements.add(qn);
			}
		}
	}
}
