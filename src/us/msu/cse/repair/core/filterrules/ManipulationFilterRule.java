package us.msu.cse.repair.core.filterrules;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

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
		
		// the following rules are from anti-patterns paper
		else if (rule_5(manipulationName, mp))
			return true;
		else if (rule_6(manipulationName, mp))
			return true;
		else if (rule_7(manipulationName, mp))
			return true;
		else if (rule_8(manipulationName, mp))
			return true;
		else if (rule_9(manipulationName, mp))
			return true;
		
		return false;
	}
	
	public static boolean canFiltered2(String manipulationName, ModificationPoint mp) {
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
	
	
	
	
	// The following rules are from Anti-patterns paper
	static boolean rule_5(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;
		
		boolean flag2 = (statement instanceof ReturnStatement) || (statement instanceof ThrowStatement);
		if (!flag2)
			return false;
		
		return true;
	}
	
	static boolean rule_6(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;
		
		boolean flag2 = (statement instanceof IfStatement) || (statement instanceof SwitchStatement)
				|| (statement instanceof ForStatement)
				|| (statement instanceof WhileStatement) || (statement instanceof DoStatement);
		
		if (!flag2)
			return false;
		
		return true;
	}
	
	
	static boolean rule_7(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;
		
		ASTNode parent = statement.getParent();
		
		// here enhanced for should also be disabled, in the experiments we did not consider
		boolean flag2 = (parent instanceof IfStatement) || (parent instanceof ForStatement)
				|| (parent instanceof WhileStatement) || (parent instanceof DoStatement)
				|| ((parent instanceof Block) && ((Block) parent).statements().size() == 1);
		
		if (!flag2)
			return false;
		
		return true;
	}
	
	static boolean rule_8(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;
		
		boolean flag2 = statement instanceof VariableDeclarationStatement;
		boolean flag3 = (statement instanceof ExpressionStatement)
				&& (((ExpressionStatement) statement).getExpression() instanceof Assignment);
		
		boolean flag4 = flag2 || flag3;
		if (!flag4)
			return false;
		
		boolean flag5 = statement.getParent() instanceof Block;
		if (!flag5)
			return false;
		
		Block block = (Block) statement.getParent();
		int index = block.statements().indexOf(statement);
		boolean flag6 = (index + 1 < block.statements().size())
				&& (block.statements().get(index + 1) instanceof IfStatement);
		if (!flag6)
			return false;
		
		Set<IVariableBinding> definedVars;
		if (flag2) 
			definedVars = Helper.getDeclaredVariables((VariableDeclarationStatement) statement);
		else {
			Assignment as = (Assignment)((ExpressionStatement) statement).getExpression();
			definedVars = Helper.getAssignedVariables(as);
		}
		
		IfStatement is = (IfStatement)(block.statements().get(index + 1));
		Set<IVariableBinding> varsInPredicate = Helper.getVariables(is.getExpression());
		
		boolean flag7 = Helper.isOneVarContained(definedVars, varsInPredicate);
		if (!flag7)
			return false;
		
		return true;
	}

	static boolean rule_9(String manipulationName, ModificationPoint mp) {
		Statement statement = mp.getStatement();
		boolean flag1 = manipulationName.equalsIgnoreCase("Delete");
		if (!flag1)
			return false;
		
		boolean flag2 = statement instanceof ExpressionStatement;
		if (!flag2)
			return false;
		
		boolean flag3 = ((ExpressionStatement) statement).getExpression() instanceof PostfixExpression;
		boolean flag4 = ((ExpressionStatement) statement).getExpression() instanceof PrefixExpression;
		boolean flag5 = flag3 || flag4;
		if (!flag5)
			return false;
		
		boolean flag6 = Helper.isInLoop(statement);
		if (!flag6)
			return false;

		if (flag3) {
			PostfixExpression pfe = (PostfixExpression)((ExpressionStatement) statement).getExpression();
			boolean flag7 = pfe.getOperand() instanceof SimpleName;
			if (!flag7)
				return false;
		}
		else {
			PrefixExpression pfe = (PrefixExpression)((ExpressionStatement) statement).getExpression();
			boolean flag8 = (pfe.getOperator() == PrefixExpression.Operator.DECREMENT)
					|| (pfe.getOperator() == PrefixExpression.Operator.INCREMENT);
			if (!flag8)
				return false;
			
			boolean flag9 = pfe.getOperand() instanceof SimpleName;
			if (!flag9)
				return false;
		}
		
		return true;
	}

}
