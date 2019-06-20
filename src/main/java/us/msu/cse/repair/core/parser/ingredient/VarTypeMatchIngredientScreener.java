package us.msu.cse.repair.core.parser.ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.parser.StatementInfoExtractor;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.util.visitors.VarConvASTVisitor;

public class VarTypeMatchIngredientScreener extends AbstractIngredientScreener {
	public VarTypeMatchIngredientScreener(List<ModificationPoint> modificationPoints,
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
			seed = getVarTypeMatchedStatement(seed, mp);
			if (seed != null)
				ingredientSet.add(new SeedStatement(seed));
		}

		List<Statement> ingredients = new ArrayList<Statement>();
		for (SeedStatement seedStatement : ingredientSet)
			ingredients.add(seedStatement.getStatement());

		mp.setIngredients(ingredients);
	}

	Statement getVarTypeMatchedStatement(Statement seed, ModificationPoint mp) {
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

		if (IngredientUtil.isInMethodScope(seed, mp, sie)) {
			IngredientUtil.findSuperVars(superVars, inheritedFields);
			IngredientUtil.findThisVars(thisVars, declaredFields, inheritedFields);
			IngredientUtil.findVars(vars, localVars, declaredFields, inheritedFields, outerFields);
			if (!IngredientUtil.doSuperVarMatch(superVarMatchMap, superVars, inheritedFields))
				return null;
			else if (!IngredientUtil.doThisVarMatch(thisVarMatchMap, thisVars, declaredFields, inheritedFields))
				return null;
			else if (!IngredientUtil.doVarMatch(varMatchMap, vars, localVars, declaredFields, inheritedFields,
					outerFields))
				return null;
			else if (varMatchMap.size() > 0 || thisVarMatchMap.size() > 0 || superVarMatchMap.size() > 0) {
				seed = (Statement) ASTNode.copySubtree(seed.getAST(), seed);
				VarConvASTVisitor visitor = new VarConvASTVisitor(varMatchMap, thisVarMatchMap, superVarMatchMap,
						sie.getVarIDs());
				seed.accept(visitor);
			}

			return seed;
		} else
			return null;

	}

}
