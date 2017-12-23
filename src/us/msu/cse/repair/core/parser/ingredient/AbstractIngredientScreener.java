package us.msu.cse.repair.core.parser.ingredient;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;

public abstract class AbstractIngredientScreener {
	protected List<ModificationPoint> modificationPoints;
	protected Map<SeedStatement, SeedStatementInfo> seedStatements;
	protected IngredientMode ingredientMode;

	public AbstractIngredientScreener(List<ModificationPoint> modificationPoints,
			Map<SeedStatement, SeedStatementInfo> seedStatements, IngredientMode ingredientMode) {
		this.modificationPoints = modificationPoints;
		this.seedStatements = seedStatements;
		this.ingredientMode = ingredientMode;
	}

	public void screen() {
		screen(true);
	}

	public void screen(boolean includeSelf) {
		for (ModificationPoint mp : modificationPoints) {
			screenIngredients(mp, ingredientMode, includeSelf);
			removeRedundantThrow(mp);
		}
	}

	private void removeRedundantThrow(ModificationPoint mp) {
		List<Statement> ingredients = mp.getIngredients();
		Set<String> throwTypes = new HashSet<String>();

		Iterator<Statement> iterator = ingredients.iterator();

		while (iterator.hasNext()) {
			Statement statement = iterator.next();
			if (statement instanceof ThrowStatement) {
				ThrowStatement throwStatement = (ThrowStatement) statement;
				ITypeBinding tb = throwStatement.getExpression().resolveTypeBinding();
				if (tb != null) {
					String name = tb.getName();
					if (throwTypes.contains(name))
						iterator.remove();
					else
						throwTypes.add(name);
				}
			}
		}
	}

	protected boolean canPreFiltered(ModificationPoint mp, Map.Entry<SeedStatement, SeedStatementInfo> entry,
			IngredientMode ingredientMode, boolean includeSelf) {
		Statement seed = entry.getKey().getStatement();

		if (!IngredientUtil.isInIngredientMode(entry.getValue(), mp, ingredientMode)
				|| !IngredientUtil.isReturnThrowCompatible(seed, mp))
			return true;

		if (!includeSelf && IngredientUtil.isSelfInIngredientMode(mp, entry, ingredientMode))
			return true;

		if (!IngredientUtil.isNewInScope(seed, mp))
			return true;

		return false;
	}

	protected abstract void screenIngredients(ModificationPoint mp, IngredientMode ingredientMode,
			boolean includeSelf);
}
