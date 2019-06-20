package us.msu.cse.repair.core.parser.ingredient;

import java.util.List;
import java.util.Map;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;

public class IngredientScreenerFactory {
	public static AbstractIngredientScreener getIngredientScreener(String name,
			List<ModificationPoint> modificationPoints, Map<SeedStatement, SeedStatementInfo> seedStatements,
			IngredientMode ingredientMode) throws JMException {
		if (name.equalsIgnoreCase("Direct"))
			return new DirectIngredientScreener(modificationPoints, seedStatements, ingredientMode);
		else if (name.equalsIgnoreCase("VarTypeMatch"))
			return new VarTypeMatchIngredientScreener(modificationPoints, seedStatements, ingredientMode);
		else if (name.equalsIgnoreCase("MethodTypeMatch"))
			return new MethodTypeMatchIngredientScreener(modificationPoints, seedStatements, ingredientMode);
		else if (name.equalsIgnoreCase("VMTypeMatch"))
			return new VMTypeMatchIngredientScreener(modificationPoints, seedStatements, ingredientMode);
		else if (name.equalsIgnoreCase("Simple"))
			return new SimpleIngredientScreener(modificationPoints, seedStatements, ingredientMode);
		else {
			Configuration.logger_.severe("IngredientSearcherFactory.getIngredientScreener. " + "IngredientScreener '"
					+ name + "' not found ");
			throw new JMException("Exception in " + name + ".getIngredientScreener()");
		}
	}
}
