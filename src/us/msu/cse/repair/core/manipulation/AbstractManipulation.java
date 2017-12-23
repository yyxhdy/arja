package us.msu.cse.repair.core.manipulation;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import us.msu.cse.repair.core.parser.ModificationPoint;

public abstract class AbstractManipulation {
	ASTRewrite rewriter;
	ModificationPoint mp;

	Statement ingredStatement;

	public AbstractManipulation(ModificationPoint mp, Statement ingredStatement, ASTRewrite rewriter) {
		this.mp = mp;
		this.ingredStatement = ingredStatement;
		this.rewriter = rewriter;
	}

	public abstract boolean manipulate();
}
