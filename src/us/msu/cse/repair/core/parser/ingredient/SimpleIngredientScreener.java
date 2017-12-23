package us.msu.cse.repair.core.parser.ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.parser.StatementInfoExtractor;

// for GenProg

public class SimpleIngredientScreener extends AbstractIngredientScreener {
	public SimpleIngredientScreener(List<ModificationPoint> modificationPoints,
			Map<SeedStatement, SeedStatementInfo> seedStatements, IngredientMode ingredientMode) {
		super(modificationPoints, seedStatements, ingredientMode);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void screenIngredients(ModificationPoint mp, IngredientMode ingredientMode, boolean includeSelf) {
		// TODO Auto-generated method stub
		List<Statement> ingredients = new ArrayList<Statement>();

		for (Map.Entry<SeedStatement, SeedStatementInfo> entry : seedStatements.entrySet()) {
			Statement seed = entry.getKey().getStatement();

			if (!IngredientUtil.isInIngredientMode(entry.getValue(), mp, ingredientMode))
				continue;

			if (!includeSelf && IngredientUtil.isSelfInIngredientMode(mp, entry, ingredientMode))
				continue;

			StatementInfoExtractor sie = new StatementInfoExtractor(seed);
			sie.extract();

			if (IngredientUtil.isInVarScope(seed, mp, sie))
				ingredients.add(seed);
		}
		mp.setIngredients(ingredients);
	}
}
