package localization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class FileASTRequestorImpl extends FileASTRequestor {
	Map<String, Set<Integer>> modifiedLines;
	
	Map<String, Set<MethodInfo>> modifiedMethods;
	
	public FileASTRequestorImpl(Map<String, Set<Integer>> modifiedLines) {
		this.modifiedLines = modifiedLines;
		modifiedMethods = new HashMap<>();
	}
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit cu) {
		Set<Integer> lineNumbers = modifiedLines.get(sourceFilePath);

		MethodDeclVisitor visitor = new MethodDeclVisitor(lineNumbers, modifiedMethods);
		cu.accept(visitor);

	}
	
	public Map<String, Set<MethodInfo>> getModifiedMethods() {
		return this.modifiedMethods;
	}
}
