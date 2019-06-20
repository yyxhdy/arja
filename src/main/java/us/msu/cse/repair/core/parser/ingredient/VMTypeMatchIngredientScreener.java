package us.msu.cse.repair.core.parser.ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.parser.StatementInfoExtractor;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.util.visitors.VMConvASTVisitor;

public class VMTypeMatchIngredientScreener extends AbstractIngredientScreener {

	public VMTypeMatchIngredientScreener(List<ModificationPoint> modificationPoints,
			Map<SeedStatement, SeedStatementInfo> seedStatements, IngredientMode ingredientMode) {
		super(modificationPoints, seedStatements, ingredientMode);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void screenIngredients(ModificationPoint mp, IngredientMode ingredientMode, boolean includeSelf) {
		// TODO Auto-generated method stub
		Set<SeedStatement> ingredientSet = new HashSet<SeedStatement>();
		for (Map.Entry<SeedStatement, SeedStatementInfo> entry : seedStatements.entrySet()) {
			if (canPreFiltered(mp, entry, ingredientMode, includeSelf))
				continue;

			Statement seed = entry.getKey().getStatement();
			seed = getVMTypeMatchedStatement(seed, mp);
			if (seed != null)
				ingredientSet.add(new SeedStatement(seed));
		}

		List<Statement> ingredients = new ArrayList<Statement>();
		for (SeedStatement seedStatement : ingredientSet)
			ingredients.add(seedStatement.getStatement());

		mp.setIngredients(ingredients);
	}

	Statement getVMTypeMatchedStatement(Statement seed, ModificationPoint mp) {
		StatementInfoExtractor sie = new StatementInfoExtractor(seed);
		sie.extract();

		Map<String, VarInfo> vars = sie.getVars();
		Map<String, VarInfo> thisVars = sie.getThisVars();
		Map<String, VarInfo> superVars = sie.getSuperVars();

		Map<String, String> varMatchMap = new HashMap<String, String>();
		Map<String, String> thisVarMatchMap = new HashMap<String, String>();
		Map<String, String> superVarMatchMap = new HashMap<String, String>();

		Map<String, VarInfo> declaredFields = new HashMap<String, VarInfo>(mp.getDeclaredFields());
		Map<String, VarInfo> inheritedFields = new HashMap<String, VarInfo>(mp.getInheritedFields());
		Map<String, VarInfo> outerFields = new HashMap<String, VarInfo>(mp.getOuterFields());
		Map<String, VarInfo> localVars = new HashMap<String, VarInfo>(mp.getLocalVars());

		Map<String, MethodInfo> thisMethods = sie.getThisMethods();
		Map<String, MethodInfo> superMethods = sie.getSuperMethods();
		Map<String, MethodInfo> methods = sie.getMethods();

		Map<String, String> methodMatchMap = new HashMap<String, String>();
		Map<String, String> thisMethodMatchMap = new HashMap<String, String>();
		Map<String, String> superMethodMatchMap = new HashMap<String, String>();

		Map<String, MethodInfo> declaredMethods = new HashMap<String, MethodInfo>(mp.getDeclaredMethods());
		Map<String, MethodInfo> inheritedMethods = new HashMap<String, MethodInfo>(mp.getInheritedMethods());
		Map<String, MethodInfo> outerMethods = new HashMap<String, MethodInfo>(mp.getOuterMethods());

		IngredientUtil.findSuperVars(superVars, inheritedFields);
		IngredientUtil.findThisVars(thisVars, declaredFields, inheritedFields);
		IngredientUtil.findVars(vars, localVars, declaredFields, inheritedFields, outerFields);

		IngredientUtil.findSuperMethods(superMethods, inheritedMethods);
		IngredientUtil.findThisMethods(thisMethods, declaredMethods, inheritedMethods);
		IngredientUtil.findMethods(methods, declaredMethods, inheritedMethods, outerMethods);

		if (!IngredientUtil.doSuperVarMatch(superVarMatchMap, superVars, inheritedFields))
			return null;
		else if (!IngredientUtil.doThisVarMatch(thisVarMatchMap, thisVars, declaredFields, inheritedFields))
			return null;
		else if (!IngredientUtil.doVarMatch(varMatchMap, vars, localVars, declaredFields, inheritedFields, outerFields))
			return null;
		else if (!IngredientUtil.doSuperMethodMatch(superMethodMatchMap, superMethods, inheritedMethods))
			return null;
		else if (!IngredientUtil.doThisMethodMatch(thisMethodMatchMap, thisMethods, declaredMethods, inheritedMethods))
			return null;
		else if (!IngredientUtil.doMethodMatch(methodMatchMap, methods, declaredMethods, inheritedMethods,
				outerMethods))
			return null;
		else if (varMatchMap.size() > 0 || thisVarMatchMap.size() > 0 || superVarMatchMap.size() > 0
				|| methodMatchMap.size() > 0 || thisMethodMatchMap.size() > 0 || superMethodMatchMap.size() > 0) {
			seed = (Statement) ASTNode.copySubtree(seed.getAST(), seed);
			VMConvASTVisitor visitor = new VMConvASTVisitor(varMatchMap, thisVarMatchMap, superVarMatchMap,
					methodMatchMap, thisMethodMatchMap, superMethodMatchMap, sie.getVarIDs(), sie.getMethodIDs(),
					sie.getSuperMethodIDs());
			seed.accept(visitor);
		}

		return seed;
	}

}
