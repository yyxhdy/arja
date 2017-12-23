package us.msu.cse.repair.core.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import us.msu.cse.repair.core.util.visitors.InitASTVisitor;

public class FileASTRequestorImpl extends FileASTRequestor {

	Map<LCNode, Double> faultyLines;
	Set<LCNode> seedLines;

	List<ModificationPoint> modificationPoints;
	Map<SeedStatement, SeedStatementInfo> seedStatements;

	Map<String, CompilationUnit> sourceASTs;

	Map<String, String> sourceContents;

	Map<String, ITypeBinding> declaredClasses;

	public FileASTRequestorImpl(Map<LCNode, Double> faultyLines, Set<LCNode> seedLines,
			List<ModificationPoint> modificationPoints, Map<SeedStatement, SeedStatementInfo> seedStatements,
			Map<String, CompilationUnit> sourceASTs, Map<String, String> sourceContents,
			Map<String, ITypeBinding> declaredClasses) {
		this.faultyLines = faultyLines;
		this.seedLines = seedLines;

		this.modificationPoints = modificationPoints;
		this.seedStatements = seedStatements;

		this.sourceASTs = sourceASTs;
		this.sourceContents = sourceContents;

		this.declaredClasses = declaredClasses;
	}

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit cu) {
		sourceASTs.put(sourceFilePath, cu);

		InitASTVisitor visitor = new InitASTVisitor(sourceFilePath, faultyLines, seedLines, modificationPoints,
				seedStatements, declaredClasses);
		cu.accept(visitor);

		try {
			String content = new String(FileUtils.readFileToByteArray(new File(sourceFilePath)));
			sourceContents.put(sourceFilePath, content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
