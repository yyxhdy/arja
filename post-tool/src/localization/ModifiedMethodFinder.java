package localization;


//import java.io.File;
import java.io.IOException;
//import java.util.Collection;
import java.util.Map;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.filefilter.SuffixFileFilter;
//import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class ModifiedMethodFinder {
	String srcJavaDir;
	Set<String> dependencies;
	
	String patchPath;
	
	Map<String, Set<Integer>> modifiedLines;
	
	
	public ModifiedMethodFinder(Map<String, Set<Integer>> modifiedLines,
			String srcJavaDir, Set<String> dependencies) {
		this.modifiedLines = modifiedLines;
		this.srcJavaDir = srcJavaDir;
		this.dependencies = dependencies;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Set<MethodInfo>> findModifiedMethods() throws IOException {	
	//	System.out.println(modifiedLines);
		FileASTRequestorImpl requestor = new FileASTRequestorImpl(modifiedLines);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String[] classpathEntries = null;
		if (dependencies != null)
			classpathEntries = dependencies.toArray(new String[dependencies.size()]);

		parser.setEnvironment(classpathEntries, new String[] { srcJavaDir }, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);


		String sourceFilePaths[] = modifiedLines.keySet().toArray(new String[0]);
		parser.createASTs(sourceFilePaths, null, new String[] { "UTF-8" }, requestor, null);
		return requestor.getModifiedMethods();
	}
}
