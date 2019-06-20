package us.msu.cse.repair.core.manipulation;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import us.msu.cse.repair.core.parser.ModificationPoint;

public class DeleteManipulation extends AbstractManipulation {

	public DeleteManipulation(ModificationPoint mp, Statement ingredStatement, ASTRewrite rewriter) {
		super(mp, ingredStatement, rewriter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean manipulate() {
		// TODO Auto-generated method stub
		Statement statement = mp.getStatement();
		if (statement.getParent() instanceof Block) {
			Block block = (Block) statement.getParent();
			ListRewrite lrw = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			lrw.remove(statement, null);
		} else {
			Statement emptyStatement = statement.getAST().newEmptyStatement();
			rewriter.replace(statement, emptyStatement, null);
		}
		return true;
	}

}
