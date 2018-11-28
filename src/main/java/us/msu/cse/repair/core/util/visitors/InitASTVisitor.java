package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import us.msu.cse.repair.core.parser.LCNode;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.SeedStatementInfo;
import us.msu.cse.repair.core.util.Helper;

public class InitASTVisitor extends ASTVisitor {
	String sourceFilePath;

	Map<LCNode, Double> faultyLines;
	Set<LCNode> seedLines;

	List<ModificationPoint> modificationPoints;
	Map<SeedStatement, SeedStatementInfo> seedStatements;

	Map<String, ITypeBinding> declaredClasses;

	public InitASTVisitor(String sourceFilePath, Map<LCNode, Double> faultyLines, Set<LCNode> seedLines,
			List<ModificationPoint> modificationPoints, Map<SeedStatement, SeedStatementInfo> seedStatements,
			Map<String, ITypeBinding> declaredClasses) {
		this.sourceFilePath = sourceFilePath;
		this.faultyLines = faultyLines;
		this.seedLines = seedLines;

		this.modificationPoints = modificationPoints;
		this.seedStatements = seedStatements;

		this.declaredClasses = declaredClasses;
	}

	private void insertStatement(Statement statement) {
		AbstractTypeDeclaration td = Helper.getAbstractTypeDeclaration(statement);

		String className = td.resolveBinding().getBinaryName();

		CompilationUnit cu = (CompilationUnit) statement.getRoot();
		int lineNumber = cu.getLineNumber(statement.getStartPosition());

		LCNode lcNode = new LCNode(className, lineNumber);

		if (faultyLines.containsKey(lcNode)) {
			ModificationPoint mp = new ModificationPoint();

			double suspValue = faultyLines.get(lcNode);
			boolean isInStaticMethod = Helper.isInStaticMethod(statement);

			mp.setSourceFilePath(sourceFilePath);
			mp.setLCNode(lcNode);
			mp.setSuspValue(suspValue);
			mp.setStatement(statement);
			mp.setInStaticMethod(isInStaticMethod);

			modificationPoints.add(mp);
		}

		if (seedLines == null || seedLines.contains(lcNode)) {
			SeedStatement seedStatement = new SeedStatement(statement);

			if (seedStatements.containsKey(seedStatement)) {
				SeedStatementInfo ssi = seedStatements.get(seedStatement);
				ssi.getSourceFilePaths().add(sourceFilePath);
				ssi.getLCNodes().add(lcNode);
			} else {
				List<LCNode> lcNodes = new ArrayList<LCNode>();
				lcNodes.add(lcNode);
				List<String> sourceFilePaths = new ArrayList<String>();
				sourceFilePaths.add(sourceFilePath);

				SeedStatementInfo ssi = new SeedStatementInfo(lcNodes, sourceFilePaths);
				seedStatements.put(seedStatement, ssi);
			}

		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!node.isInterface()) {
			ITypeBinding tb = node.resolveBinding();
			if (tb != null) {
				String name = tb.getBinaryName();
				declaredClasses.put(name, tb);
			}
		}
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ITypeBinding tb = node.resolveBinding();
		if (tb != null) {
			String name = tb.getBinaryName();
			declaredClasses.put(name, tb);
		}
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		ITypeBinding tb = node.resolveBinding();
		if (tb != null) {
			String name = tb.getBinaryName();
			declaredClasses.put(name, tb);
		}

		return true;
	}

	@Override
	public boolean visit(AssertStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		insertStatement(node);
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		insertStatement(node);
		return true;
	}
}
