package us.msu.cse.repair.core.filterrules;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.util.Helper;

public class IngredientFilterRule {
	public static boolean canFiltered(Statement seed, ModificationPoint mp) {
		if (rule_1(seed, mp))
			return true;
		else if (rule_2(seed, mp))
			return true;
		else if (rule_3(seed, mp))
			return true;
		else if (rule_4(seed, mp))
			return true;
		else if (rule_5(seed, mp))
			return true;
		else if (rule_6(seed, mp))
			return true;
		return false;
	}

	static boolean rule_1(Statement seed, ModificationPoint mp) {
		boolean flag1 = seed instanceof ContinueStatement;
		if (!flag1)
			return false;

		boolean flag2 = Helper.isInLoop(mp.getStatement());
		if (flag2)
			return false;

		return true;
	}

	static boolean rule_2(Statement seed, ModificationPoint mp) {
		boolean flag1 = seed instanceof BreakStatement;
		if (!flag1)
			return false;

		boolean flag2 = Helper.isInSwitch(mp.getStatement());
		if (flag2)
			return false;

		boolean flag3 = Helper.isInLoop(mp.getStatement());
		if (flag3)
			return false;

		return true;
	}

	static boolean rule_3(Statement seed, ModificationPoint mp) {
		boolean flag1 = seed instanceof VariableDeclarationStatement;
		if (!flag1)
			return false;

		Statement statement = mp.getStatement();
		boolean flag2 = statement instanceof VariableDeclarationStatement;
		boolean flag3 = false;
		if (flag2) {
			flag3 = Helper.isAlternativeVariableDeclaration((VariableDeclarationStatement) statement,
					(VariableDeclarationStatement) seed);
		}
		if (flag2 && flag3)
			return false;

		return true;
	}

	static boolean rule_4(Statement seed, ModificationPoint mp) {
		boolean flag1 = seed instanceof SwitchCase;
		if (!flag1)
			return false;

		ASTNode parent = mp.getStatement().getParent();
		boolean flag2 = parent instanceof SwitchStatement;
		boolean flag3 = false;
		if (flag2) {
			SwitchStatement sws = (SwitchStatement) parent;
			SwitchCase sc = (SwitchCase) seed;

			if (sc.getExpression() != null) {
				ITypeBinding tb1 = sws.getExpression().resolveTypeBinding();
				ITypeBinding tb2 = sc.getExpression().resolveTypeBinding();
				if (tb1 == tb2)
					flag3 = true;
			}
		}

		if (flag2 && flag3)
			return false;

		return true;
	}

	static boolean rule_5(Statement seed, ModificationPoint mp) {
		boolean flag1 = Helper.canReturnOrThrow(seed);
		if (!flag1)
			return false;

		Statement statement = mp.getStatement();
		boolean flag2 = Helper.isLastStatementInBlock(statement);
		if (flag2)
			return false;

		return true;
	}

	static boolean rule_6(Statement seed, ModificationPoint mp) {
		boolean flag1 = seed instanceof VariableDeclarationStatement;
		boolean flag2 = Helper.isAssignment(seed);
		if (!flag1 && !flag2)
			return false;

		Statement statement = mp.getStatement();
		boolean flag3 = seed.subtreeMatch(new ASTMatcher(true), statement);
		if (!flag3)
			return false;

		return true;
	}
}
