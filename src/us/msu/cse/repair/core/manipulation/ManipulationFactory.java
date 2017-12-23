package us.msu.cse.repair.core.manipulation;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import us.msu.cse.repair.core.parser.ModificationPoint;
import jmetal.util.Configuration;
import jmetal.util.JMException;

public class ManipulationFactory {
	public static AbstractManipulation getManipulation(String name, ModificationPoint mp, Statement ingredStatement,
			ASTRewrite rewriter) throws JMException {
		if (name.equalsIgnoreCase("Delete"))
			return new DeleteManipulation(mp, ingredStatement, rewriter);
		else if (name.equalsIgnoreCase("Replace"))
			return new ReplaceManipulation(mp, ingredStatement, rewriter);
		else if (name.equalsIgnoreCase("InsertBefore"))
			return new InsertBeforeManipulation(mp, ingredStatement, rewriter);
		else if (name.equalsIgnoreCase("InsertAfter"))
			return new InsertAfterManipulation(mp, ingredStatement, rewriter);
		else if (name.equalsIgnoreCase("RedirectBranch"))
			return new RedirectBranchManipulation(mp, ingredStatement, rewriter);
		else if (name.equalsIgnoreCase("InsertReturnManipulation"))
			return new InsertReturnManipulation(mp, ingredStatement, rewriter);
		else {
			Configuration.logger_
					.severe("ManipulationFactory.getManipulation. " + "Manipulation '" + name + "' not found ");
			throw new JMException("Exception in " + name + ".getManipulation()");
		}
	}
}
