package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class ReturnOrThrowStatementASTVisitor extends ASTVisitor {
	List<Statement> rtStatements;

	public ReturnOrThrowStatementASTVisitor() {
		rtStatements = new ArrayList<Statement>();
	}

	@Override
	public boolean visit(ReturnStatement rs) {
		rtStatements.add(rs);
		return true;
	}

	@Override
	public boolean visit(ThrowStatement ts) {
		rtStatements.add(ts);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}

	public List<Statement> getReturnThrowStatements() {
		return this.rtStatements;
	}
}