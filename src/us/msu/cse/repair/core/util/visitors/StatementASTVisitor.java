package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class StatementASTVisitor extends ASTVisitor {
	List<Statement> statements;
	
	public StatementASTVisitor() {
		statements = new ArrayList<Statement>();
	}
	
	void handleStatement(Statement statement) {
		boolean flag1 = statement.getParent() instanceof Block;
		boolean flag2 = statement.getParent() instanceof SwitchStatement;
		boolean flag3 = statement.getParent() instanceof LabeledStatement;
		
		if (!flag1 && !flag2 && !flag3)
			statements.add(statement);
	}
	
	public List<Statement> getStatements() {
		return statements;
	}
	
	
	@Override
	public boolean visit(AssertStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		handleStatement(node);
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		handleStatement(node);
		return true;
	}
}
