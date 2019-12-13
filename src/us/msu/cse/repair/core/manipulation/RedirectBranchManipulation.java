package us.msu.cse.repair.core.manipulation;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import us.msu.cse.repair.core.parser.ModificationPoint;

public class RedirectBranchManipulation extends AbstractManipulation {

	private boolean condition = true;

	public RedirectBranchManipulation(ModificationPoint mp, Statement ingredStatement, ASTRewrite rewriter) {
		super(mp, ingredStatement, rewriter);
		// TODO Auto-generated constructor stub

	}

	@Override
	public boolean manipulate() {
		// TODO Auto-generated method stub
		Statement statement = mp.getStatement();
		if (!(statement instanceof IfStatement))
			return false;

		IfStatement ifs = (IfStatement) statement;
		Expression bl = statement.getAST().newBooleanLiteral(condition);
		rewriter.set(ifs, IfStatement.EXPRESSION_PROPERTY, bl, null);
		return true;
	}

	public void setCondition(boolean condition) {
		this.condition = condition;
	}

	public boolean getCondition() {
		return this.condition;
	}

}
