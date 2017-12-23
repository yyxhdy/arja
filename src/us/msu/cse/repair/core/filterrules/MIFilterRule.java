package us.msu.cse.repair.core.filterrules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import jmetal.util.PseudoRandom;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.util.Helper;

public class MIFilterRule {
	public static int canFiltered(String manipulationName, Statement seed, ModificationPoint mp) {
		int res;
		if ((res = rule_1(manipulationName, seed, mp)) != -1)
			return res;
		else if ((res = rule_2(manipulationName, seed, mp)) != -1)
			return res;
		else if ((res = rule_3(manipulationName, seed, mp)) != -1)
			return res;
		else if ((res = rule_4(manipulationName, seed, mp)) != -1)
			return res;
		else if ((res = rule_5(manipulationName, seed, mp)) != -1)
			return res;
		else if ((res = rule_6(manipulationName, seed, mp)) != -1)
			return res;
		return -1;
	}
	

	static int rule_1(String manipulationName, Statement seed, ModificationPoint mp) {
		boolean flag1 = manipulationName.equalsIgnoreCase("Replace");
		if (!flag1)
			return -1;

		boolean flag2 = seed.subtreeMatch(new ASTMatcher(true), mp.getStatement());
		if (!flag2)
			return -1;

		return mp.getIngredients().size();
	}

	static int rule_2(String manipulationName, Statement seed, ModificationPoint mp) {
		Statement statement = mp.getStatement();

		boolean flag1 = manipulationName.equalsIgnoreCase("Replace");
		if (!flag1)
			return -1;

		boolean flag2 = statement instanceof VariableDeclarationStatement;
		if (!flag2)
			return -1;

		boolean flag3 = seed instanceof VariableDeclarationStatement;
		if (flag3)
			return -1;

		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < mp.getIngredients().size(); i++) {
			Statement st = mp.getIngredients().get(i);
			if (st instanceof VariableDeclarationStatement) 
				ids.add(i);
		}	
		if (ids.size() > 0) {
			int index = PseudoRandom.randInt(0, ids.size() -1);
			return ids.get(index);
		}
		else
			return mp.getIngredients().size();
	}

	static int rule_3(String manipulationName, Statement seed, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("InsertBefore")
				|| manipulationName.equalsIgnoreCase("InsertAfter");
		if (!flag1)
			return -1;

		boolean flag2 = statement instanceof VariableDeclarationStatement;
		if (!flag2)
			return -1;

		boolean flag3 = seed instanceof VariableDeclarationStatement;
		if (!flag3)
			return -1;

		return mp.getIngredients().size();
	}

	static int rule_4(String manipulationName, Statement seed, ModificationPoint mp) {
		Statement statement = mp.getStatement();

		boolean flag1 = manipulationName.equalsIgnoreCase("Replace");
		if (!flag1)
			return -1;

		boolean flag2 = Helper.canReturnOrThrow(statement);
		if (!flag2)
			return -1;

		boolean flag3 = Helper.isLastStatementInMethod(statement);
		if (!flag3)
			return -1;

		boolean flag4 = Helper.canReturnOrThrow(seed);
		if (flag4)
			return -1;

	
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < mp.getIngredients().size(); i++) {
			Statement st = mp.getIngredients().get(i);
			if (Helper.canReturnOrThrow(st)) 
				ids.add(i);
		}		
		if (ids.size() > 0) {
			int index = PseudoRandom.randInt(0, ids.size() -1);
			return ids.get(index);
		}
		else
			return mp.getIngredients().size();
	}

	static int rule_5(String manipulationName, Statement seed, ModificationPoint mp) {
		Statement statement = mp.getStatement();

		boolean flag1 = manipulationName.equalsIgnoreCase("InsertBefore");
		if (!flag1)
			return -1;

		boolean flag2 = statement instanceof ExpressionStatement;
		if (!flag2)
			return -1;

		boolean flag3 = ((ExpressionStatement) statement).getExpression() instanceof Assignment;
		if (!flag3)
			return -1;

		boolean flag4 = seed instanceof ExpressionStatement;
		if (!flag4)
			return -1;

		boolean flag5 = ((ExpressionStatement) seed).getExpression() instanceof Assignment;
		if (!flag5)
			return -1;

		Expression left1 = ((Assignment) ((ExpressionStatement) statement).getExpression()).getLeftHandSide();
		Expression left2 = ((Assignment) ((ExpressionStatement) seed).getExpression()).getLeftHandSide();
		boolean flag6 = left1.subtreeMatch(new ASTMatcher(true), left2);
		if (!flag6)
			return -1;

		return mp.getIngredients().size();
	}

	static int rule_6(String manipulationName, Statement seed, ModificationPoint mp) {
		boolean flag1 = manipulationName.equalsIgnoreCase("InsertBefore");
		if (!flag1)
			return -1;

		boolean flag2 = Helper.canReturnOrThrow(seed);
		if (!flag2)
			return -1;

		return mp.getIngredients().size();
	}

}
