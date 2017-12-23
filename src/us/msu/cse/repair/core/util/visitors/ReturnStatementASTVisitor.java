package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class ReturnStatementASTVisitor extends ASTVisitor {
	List<ReturnStatement> returnStatements;

	public ReturnStatementASTVisitor() {
		returnStatements = new ArrayList<ReturnStatement>();
	}

	@Override
	public boolean visit(ReturnStatement rs) {
		returnStatements.add(rs);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}

	public List<ReturnStatement> getReturnStatements() {
		return this.returnStatements;
	}
}