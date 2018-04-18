package us.msu.cse.repair.core.parser.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;

import us.msu.cse.repair.core.parser.LCNode;
import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.parser.StatementInfoExtractor;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.ReturnStatementASTVisitor;
import us.msu.cse.repair.core.util.visitors.ClassInstanceAndArrayCreationVisitor;
import us.msu.cse.repair.core.util.visitors.ReturnOrThrowStatementASTVisitor;

public class IngredientUtil {

	public static boolean doSuperVarMatch(Map<String, String> superVarMatchMap, Map<String, VarInfo> superVars,
			Map<String, VarInfo> inheritedFields) {
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();
		varScopes.add(inheritedFields);
		return doMatch1(superVarMatchMap, superVars, varScopes);
	}

	public static boolean doThisVarMatch(Map<String, String> thisVarMatchMap, Map<String, VarInfo> thisVars,
			Map<String, VarInfo> declaredFields, Map<String, VarInfo> inheritedFields) {

		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();

		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);

		return doMatch1(thisVarMatchMap, thisVars, varScopes);
	}

	public static boolean doVarMatch(Map<String, String> varMatchMap, Map<String, VarInfo> vars,
			Map<String, VarInfo> localVars, Map<String, VarInfo> declaredFields, Map<String, VarInfo> inheritedFields,
			Map<String, VarInfo> outerFields) {
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();

		varScopes.add(localVars);
		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);
		varScopes.add(outerFields);

		return doMatch1(varMatchMap, vars, varScopes);
	}

	static boolean doMatch1(Map<String, String> matchMap, Map<String, VarInfo> vars,
			List<Map<String, VarInfo>> varScopes) {
		Iterator<Map.Entry<String, VarInfo>> iterator = vars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, VarInfo> varEntry = iterator.next();
			if (!doMatch1(matchMap, varEntry, varScopes))
				return false;
			else
				iterator.remove();
		}

		return true;

	}

	static boolean doMatch1(Map<String, String> matchMap, Map.Entry<String, VarInfo> varEntry,
			List<Map<String, VarInfo>> varScopes) {

		Map.Entry<String, VarInfo> maxEntry = null;
		Map<String, VarInfo> maxScope = null;
		int maxDegree = 0;

		for (Map<String, VarInfo> scope : varScopes) {
			for (Map.Entry<String, VarInfo> entry : scope.entrySet()) {
				int degree = getVarMatchDegree(varEntry, entry);
				if (degree > maxDegree) {
					maxDegree = degree;
					maxEntry = entry;
					maxScope = scope;
				}
			}
		}

		if (maxDegree > 0) {
			String name = varEntry.getKey();
			String newName = maxEntry.getKey();
			matchMap.put(name, newName);
			maxScope.remove(newName);
			return true;
		} else
			return false;
	}

	static int getVarMatchDegree(Map.Entry<String, VarInfo> entry1, Map.Entry<String, VarInfo> entry2) {
		String name1 = entry1.getKey();
		VarInfo vi1 = entry1.getValue();

		String name2 = entry2.getKey();
		VarInfo vi2 = entry2.getValue();

		if (!name1.equals(name2) && vi1.isStronglyTypeMatched(vi2))
			return 2;
		else if (!name1.equals(name2) && vi1.isWeaklyTypeMatched(vi2))
			return 1;
		else
			return 0;
	}

	public static void findSuperVars(Map<String, VarInfo> superVars, Map<String, VarInfo> inheritedFields) {
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();
		varScopes.add(inheritedFields);

		findInVarMaps(superVars, varScopes);
	}

	public static void findThisVars(Map<String, VarInfo> thisVars, Map<String, VarInfo> declaredFields,
			Map<String, VarInfo> inheritedFields) {
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();
		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);

		findInVarMaps(thisVars, varScopes);
	}

	public static void findVars(Map<String, VarInfo> vars, Map<String, VarInfo> localVars,
			Map<String, VarInfo> declaredFields, Map<String, VarInfo> inheritedFields,
			Map<String, VarInfo> outerFields) {
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();
		varScopes.add(localVars);
		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);
		varScopes.add(outerFields);

		findInVarMaps(vars, varScopes);
	}

	static void findInVarMaps(Map<String, VarInfo> vars, List<Map<String, VarInfo>> varScopes) {
		Iterator<Map.Entry<String, VarInfo>> iterator = vars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, VarInfo> entry = iterator.next();
			String name = entry.getKey();
			VarInfo vi1 = entry.getValue();

			for (int i = 0; i < varScopes.size(); i++) {
				Map<String, VarInfo> map = varScopes.get(i);
				if (map.containsKey(name)) {
					VarInfo vi2 = map.get(name);
					if (vi1.isStronglyTypeMatched(vi2) || vi1.isWeaklyTypeMatched(vi2)) {
						iterator.remove();
						map.remove(name);
						break;
					}
				}
			}
		}
	}

	public static boolean doSuperMethodMatch(Map<String, String> superMethodMatchMap,
			Map<String, MethodInfo> superMethods, Map<String, MethodInfo> inheritedMethods) {
		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(inheritedMethods);
		return doMatch2(superMethodMatchMap, superMethods, methodScopes);
	}

	public static boolean doThisMethodMatch(Map<String, String> thisMethodMatchMap, Map<String, MethodInfo> thisMethods,
			Map<String, MethodInfo> declaredMethods, Map<String, MethodInfo> inheritedMethods) {

		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();

		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);

		return doMatch2(thisMethodMatchMap, thisMethods, methodScopes);
	}

	public static boolean doMethodMatch(Map<String, String> methodMatchMap, Map<String, MethodInfo> methods,
			Map<String, MethodInfo> declaredMethods, Map<String, MethodInfo> inheritedMethods,
			Map<String, MethodInfo> outerMethods) {
		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();

		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);
		methodScopes.add(outerMethods);

		return doMatch2(methodMatchMap, methods, methodScopes);
	}

	static boolean doMatch2(Map<String, String> matchMap, Map<String, MethodInfo> methods,
			List<Map<String, MethodInfo>> methodScopes) {
		Iterator<Map.Entry<String, MethodInfo>> iterator = methods.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, MethodInfo> varEntry = iterator.next();
			if (!doMatch2(matchMap, varEntry, methodScopes))
				return false;
			else
				iterator.remove();
		}

		return true;

	}

	static boolean doMatch2(Map<String, String> matchMap, Map.Entry<String, MethodInfo> varEntry,
			List<Map<String, MethodInfo>> methodScopes) {

		Map.Entry<String, MethodInfo> maxEntry = null;
		Map<String, MethodInfo> maxScope = null;
		int maxDegree = 0;

		for (Map<String, MethodInfo> scope : methodScopes) {
			for (Map.Entry<String, MethodInfo> entry : scope.entrySet()) {
				int degree = getMethodMatchDegree(varEntry, entry);
				if (degree > maxDegree) {
					maxDegree = degree;
					maxEntry = entry;
					maxScope = scope;
				}
			}
		}

		if (maxDegree > 0) {
			String key = varEntry.getKey();
			String newKey = maxEntry.getKey();
			matchMap.put(key, Helper.getMethodName(newKey));
			maxScope.remove(newKey);
			return true;
		} else
			return false;

	}

	static int getMethodMatchDegree(Map.Entry<String, MethodInfo> entry1, Map.Entry<String, MethodInfo> entry2) {
		MethodInfo mi1 = entry1.getValue();
		String ps1 = mi1.getParameterTypeNames();

		MethodInfo mi2 = entry2.getValue();
		String ps2 = mi2.getParameterTypeNames();

		if (ps1.equals(ps2) && mi1.isStronglyReturnTypeMatched(mi2))
			return 2;
		else if (ps1.equals(ps2) && mi1.isWeaklyReturnTypeMatched(mi2))
			return 1;
		else
			return 0;
	}

	public static void findSuperMethods(Map<String, MethodInfo> superMethods,
			Map<String, MethodInfo> inheritedMethods) {
		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(inheritedMethods);

		findInMethodMaps(superMethods, methodScopes);
	}

	public static void findThisMethods(Map<String, MethodInfo> thisMethods, Map<String, MethodInfo> declaredMethods,
			Map<String, MethodInfo> inheritedMethods) {
		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);

		findInMethodMaps(thisMethods, methodScopes);
	}

	public static void findMethods(Map<String, MethodInfo> methods, Map<String, MethodInfo> declaredMethods,
			Map<String, MethodInfo> inheritedMethods, Map<String, MethodInfo> outerMethods) {
		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);
		methodScopes.add(outerMethods);

		findInMethodMaps(methods, methodScopes);
	}

	static void findInMethodMaps(Map<String, MethodInfo> methods, List<Map<String, MethodInfo>> methodScopes) {
		Iterator<Map.Entry<String, MethodInfo>> iterator = methods.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, MethodInfo> entry = iterator.next();
			String name = entry.getKey();
			MethodInfo mi1 = entry.getValue();

			for (int i = 0; i < methodScopes.size(); i++) {
				Map<String, MethodInfo> map = methodScopes.get(i);
				if (map.containsKey(name)) {
					MethodInfo mi2 = map.get(name);

					if (mi1.isStronglyReturnTypeMatched(mi2) || mi1.isWeaklyReturnTypeMatched(mi2)) {
						iterator.remove();
						map.remove(name);
					}
					break;
				}
			}
		}
	}

	public static boolean isInMethodScope(Statement seed, ModificationPoint mp, StatementInfoExtractor sie) {
		Map<String, MethodInfo> methods = sie.getMethods();
		Map<String, MethodInfo> thisMethods = sie.getThisMethods();
		Map<String, MethodInfo> superMethods = sie.getSuperMethods();

		if (mp.isInStaticMethod() && (thisMethods.size() > 0 || superMethods.size() > 0))
			return false;
		else if (!findThisMethods(thisMethods, mp))
			return false;
		else if (!findSuperMethods(superMethods, mp))
			return false;
		else if (!findMethods(methods, mp))
			return false;
		else
			return true;

	}

	public static boolean isInVarScope(Statement seed, ModificationPoint mp, StatementInfoExtractor sie) {
		Map<String, VarInfo> vars = sie.getVars();
		Map<String, VarInfo> thisVars = sie.getThisVars();
		Map<String, VarInfo> superVars = sie.getSuperVars();

		if (mp.isInStaticMethod() && (thisVars.size() > 0 || superVars.size() > 0))
			return false;
		else if (!findThisVars(thisVars, mp))
			return false;
		else if (!findSuperVars(superVars, mp))
			return false;
		else if (!findVars(vars, mp))
			return false;
		else
			return true;
	}

	static boolean findThisMethods(Map<String, MethodInfo> thisMethods, ModificationPoint mp) {
		Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();

		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();

		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);

		return findInMethodMaps2(thisMethods, methodScopes);
	}

	static boolean findSuperMethods(Map<String, MethodInfo> superMethods, ModificationPoint mp) {
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();

		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(inheritedMethods);

		return findInMethodMaps2(superMethods, methodScopes);
	}

	static boolean findMethods(Map<String, MethodInfo> methods, ModificationPoint mp) {
		Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
		Map<String, MethodInfo> outerMethods = mp.getOuterMethods();

		List<Map<String, MethodInfo>> methodScopes = new ArrayList<Map<String, MethodInfo>>();
		methodScopes.add(declaredMethods);
		methodScopes.add(inheritedMethods);
		methodScopes.add(outerMethods);

		return findInMethodMaps2(methods, methodScopes);
	}

	static boolean findInMethodMaps2(Map<String, MethodInfo> methods, List<Map<String, MethodInfo>> methodScopes) {
		for (Map.Entry<String, MethodInfo> entry : methods.entrySet()) {
			String name = entry.getKey();
			MethodInfo mi1 = entry.getValue();

			int i = 0;
			for (i = 0; i < methodScopes.size(); i++) {
				Map<String, MethodInfo> map = methodScopes.get(i);

				if (map.containsKey(name)) {
					MethodInfo mi2 = map.get(name);
					if (mi1.isStronglyReturnTypeMatched(mi2) || mi1.isWeaklyReturnTypeMatched(mi2))
						break;
					else
						return false;
				}
			}
			if (i == methodScopes.size())
				return false;
		}
		return true;
	}

	static boolean findThisVars(Map<String, VarInfo> thisVars, ModificationPoint mp) {
		Map<String, VarInfo> declaredFields = mp.getDeclaredFields();
		Map<String, VarInfo> inheritedFields = mp.getInheritedFields();

		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();

		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);

		return findInVarMaps2(thisVars, varScopes);

	}

	static boolean findSuperVars(Map<String, VarInfo> superVars, ModificationPoint mp) {
		Map<String, VarInfo> inheritedFields = mp.getInheritedFields();
		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();

		varScopes.add(inheritedFields);
		return findInVarMaps2(superVars, varScopes);
	}

	static boolean findVars(Map<String, VarInfo> vars, ModificationPoint mp) {
		Map<String, VarInfo> localVars = mp.getLocalVars();
		Map<String, VarInfo> declaredFields = mp.getDeclaredFields();
		Map<String, VarInfo> inheritedFields = mp.getInheritedFields();
		Map<String, VarInfo> outerFields = mp.getOuterFields();

		List<Map<String, VarInfo>> varScopes = new ArrayList<Map<String, VarInfo>>();

		varScopes.add(localVars);
		varScopes.add(declaredFields);
		varScopes.add(inheritedFields);
		varScopes.add(outerFields);

		return findInVarMaps2(vars, varScopes);
	}

	static boolean findInVarMaps2(Map<String, VarInfo> vars, List<Map<String, VarInfo>> varScopes) {
		for (Map.Entry<String, VarInfo> entry : vars.entrySet()) {
			String name = entry.getKey();
			VarInfo vi1 = entry.getValue();

			int i = 0;
			for (i = 0; i < varScopes.size(); i++) {
				Map<String, VarInfo> map = varScopes.get(i);
				if (map.containsKey(name)) {
					VarInfo vi2 = map.get(name);
					if (vi1.isStronglyTypeMatched(vi2) || vi1.isWeaklyTypeMatched(vi2))
						break;
				}
			}
			if (i == varScopes.size())
				return false;
		}

		return true;
	}

	public static boolean isInIngredientMode(SeedStatementInfo seedStatementInfo, ModificationPoint mp,
			IngredientMode ingredientMode) {
		if (ingredientMode == IngredientMode.File) {
			List<String> paths = seedStatementInfo.getSourceFilePaths();
			String sourceFilePath = mp.getSourceFilePath();

			for (String path : paths) {
				if (sourceFilePath.equals(path))
					return true;
			}
			return false;

		} else if (ingredientMode == IngredientMode.Package) {
			String className = mp.getLCNode().getClassName();
			List<LCNode> lcNodes = seedStatementInfo.getLCNodes();

			for (LCNode lcNode : lcNodes) {
				String name = lcNode.getClassName();

				if (Helper.isInSamePackage(name, className))
					return true;
			}
			return false;

		} else
			return true;
	}

	public static boolean isReturnThrowCompatible(Statement seed, ModificationPoint mp) {
		ReturnOrThrowStatementASTVisitor visitor = new ReturnOrThrowStatementASTVisitor();
		seed.accept(visitor);
		List<Statement> rsStatements = visitor.getReturnThrowStatements();

		if (rsStatements.isEmpty())
			return true;

		MethodDeclaration md = Helper.getMethodDeclaration(mp.getStatement());
		if (md == null)
			return false;

		Type returnType = md.getReturnType2();
		ITypeBinding methodReturnTypeBinding = (returnType != null ? returnType.resolveBinding() : null);

		List<ITypeBinding> methodThrowTypeBindings = new ArrayList<ITypeBinding>();
		for (Object type : md.thrownExceptionTypes())
			methodThrowTypeBindings.add(((Type) type).resolveBinding());

		for (Statement statement : rsStatements) {
			if (statement instanceof ReturnStatement) {

				if (methodReturnTypeBinding == null)
					return false;

				ReturnStatement rs = (ReturnStatement) statement;
				if (rs.getExpression() == null) {
					if (!methodReturnTypeBinding.getQualifiedName().equals("void"))
						return false;
				} else {
					ITypeBinding tb = rs.getExpression().resolveTypeBinding();
					if (!tb.isAssignmentCompatible(methodReturnTypeBinding))
						return false;
				}
			} else {
				ThrowStatement ts = (ThrowStatement) statement;
				ITypeBinding tb = ts.getExpression().resolveTypeBinding();

				if (Helper.isRuntimeException(tb))
					continue;

				boolean isMatched = false;
				for (ITypeBinding mehodThrowTypeBinding : methodThrowTypeBindings) {
					if (tb.isAssignmentCompatible(mehodThrowTypeBinding)) {
						isMatched = true;
						break;
					}
				}
				if (!isMatched)
					return false;
			}
		}

		return true;

	}

	public static boolean isReturnCompatible(Statement seed, ModificationPoint mp) {
		MethodDeclaration md = Helper.getMethodDeclaration(mp.getStatement());
		ITypeBinding methodReturnTypeBinding = md.getReturnType2().resolveBinding();

		ReturnStatementASTVisitor visitor = new ReturnStatementASTVisitor();
		seed.accept(visitor);

		List<ReturnStatement> returnStatements = visitor.getReturnStatements();

		for (ReturnStatement rs : returnStatements) {
			if (rs.getExpression() == null) {
				if (!methodReturnTypeBinding.getQualifiedName().equals("void"))
					return false;
			} else {
				ITypeBinding tb = rs.getExpression().resolveTypeBinding();
				if (!tb.isAssignmentCompatible(methodReturnTypeBinding))
					return false;
			}
		}

		return true;
	}

	public static boolean isSelfInIngredientMode(ModificationPoint mp,
			Map.Entry<SeedStatement, SeedStatementInfo> entry, IngredientMode ingredientMode) {
		Statement statement = mp.getStatement();
		Statement seed = entry.getKey().getStatement();

		if (!statement.subtreeMatch(new ASTMatcher(true), seed))
			return false;

		int count = 0;
		if (ingredientMode == IngredientMode.File) {
			List<String> paths = entry.getValue().getSourceFilePaths();
			String sourceFilePath = mp.getSourceFilePath();

			for (String path : paths) {
				if (sourceFilePath.equals(path))
					count++;
			}

		} else if (ingredientMode == IngredientMode.Package) {
			String className = mp.getLCNode().getClassName();
			List<LCNode> lcNodes = entry.getValue().getLCNodes();

			for (LCNode lcNode : lcNodes) {
				String name = lcNode.getClassName();

				if (Helper.isInSamePackage(name, className))
					count++;
			}

		} else
			count = entry.getValue().getSourceFilePaths().size();

		return count == 1 ? true : false;
	}

	public static boolean isNewInScope(Statement seed, ModificationPoint mp) {
		Statement statement = mp.getStatement();

		Set<String> visibleTypeDecls = Helper.getVisibleTypeDeclarations(statement);

		ITypeBinding tb = Helper.getAbstractTypeDeclaration(statement).resolveBinding();
		if (tb == null)
			return false;
		String clsName = tb.getBinaryName();

		ClassInstanceAndArrayCreationVisitor ciacVisitor = new ClassInstanceAndArrayCreationVisitor();
		seed.accept(ciacVisitor);
		Set<String> classes = ciacVisitor.getClasses();

		for (String cls : classes) {
			if (!cls.contains("$")) {
				String pk = null;
				int index = cls.lastIndexOf(".");
				if (index != -1)
					pk = cls.substring(0, index);
				boolean flag1 = visibleTypeDecls.contains(cls);
				boolean flag2 = (pk != null) && visibleTypeDecls.contains(pk);

				if (!flag1 && !flag2)
					return false;
			} else { /*
				int index = cls.lastIndexOf("$");
				String pc = cls.substring(0, index);
				if (!pc.equals(clsName))
					return false;
				*/
				
				int index = cls.lastIndexOf("$");
				String pc = cls.substring(0, index);
				String qc = cls.substring(index + 1);
				
				boolean flag = false;
				for (ITypeBinding innerType : tb.getDeclaredTypes()) {
					if (innerType.getName().equals(qc))
						flag = true;
				}
				
				if (!pc.equals(clsName) && !flag)
					return false; 
			}

		}

		return true;
	}
}
