package us.msu.cse.repair.core.filterrules;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.util.Helper;

public class ManipulationFilterRule {

	public static boolean canFiltered(String manipulationName, ModificationPoint mp) {
		if (rule_1(manipulationName, mp))
			return true;
		else if (rule_2(manipulationName, mp))
			return true;
		else if (rule_3(manipulationName, mp))
			return true;
		else if (rule_4(manipulationName, mp))
			return true;
		return false;
	}

	static boolean rule_1(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;

		boolean flag2 = statement instanceof VariableDeclarationStatement;
		if (!flag2)
			return false;

		return true;
	}

	static boolean rule_2(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;

		boolean flag2 = Helper.canReturnOrThrow(statement);
		if (!flag2)
			return false;

		boolean flag3 = Helper.isLastStatementInMethod(statement);
		if (!flag3)
			return false;

		return true;
	}

	static boolean rule_3(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("InsertAfter");
		if (!flag1)
			return false;

		boolean flag2 = Helper.canReturnOrThrow(statement);
		if (!flag2)
			return false;

		return true;
	}

	static boolean rule_4(String manipulationName, ModificationPoint mp) {
		boolean flag1 = manipulationName.equalsIgnoreCase("InsertAfter");
		if (!flag1)
			return false;

		boolean flag2 = Helper.isLastStatementInBlock(mp.getStatement());
		if (flag2)
			return false;

		return true;
	}

}
