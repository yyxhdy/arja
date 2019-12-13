package patching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import jdkcompiler.JavaFileObjectImpl;
import jdkcompiler.JavaJDKCompiler;
import preprocessing.RemoveTransientAdapter;

public class Patcher {
	String patchPath;
	String patchedBinRoot;
	
	String srcJavaDir;
	String binJavaDir;
	Set<String> dependencies;
	
	Map<String, String> javaSources;
	
	Set<String> relativePathsForModifiedFiles;
	int pLevel;
	
	File workingDir;
	
	public Patcher(String patchPath, Set<String> relativePathsForModifiedFiles, int pLevel,
			String srcJavaDir, String binJavaDir, Set<String> dependencies,
			String patchedBinRoot, File workingDir) {
		this.patchPath = patchPath;
		this.srcJavaDir = srcJavaDir;
		this.binJavaDir = binJavaDir;
		this.dependencies = dependencies;
		this.patchedBinRoot = patchedBinRoot;
		this.relativePathsForModifiedFiles = relativePathsForModifiedFiles;
		this.pLevel = pLevel;
		this.workingDir = workingDir;
	}
	
	public void execute() throws Exception {
		getModifiedJavaSources();
		compile();
	}
	
	void getModifiedJavaSources() throws IOException, InterruptedException {
		javaSources = new HashMap<>();
		File wf = new File(workingDir, "modSrcs");
		
		for (String path : this.relativePathsForModifiedFiles) {
			File srcFile = new File(srcJavaDir, path);
			File destFile = new File(wf, path);
			
			copyFile(srcFile, destFile);
		}
		
		PatchCmd pc = new PatchCmd(patchPath, pLevel, wf);
		pc.execute();
		
		for (String path : this.relativePathsForModifiedFiles) {
			File destFile = new File(wf, path);
			String srcCode = FileUtils.readFileToString(destFile, "UTF-8");
			javaSources.put(path, srcCode);
		}
	}
	
	
	void compile() throws Exception {
		List<String> options = this.getCompilerOptions();
		JavaJDKCompiler compiler = new JavaJDKCompiler(ClassLoader.getSystemClassLoader(), options);
		
		compiler.compile(javaSources);
		
		for (Map.Entry<String, JavaFileObject> entry : compiler.getClassLoader().getCompiledClasses().entrySet()) {
			String cls = entry.getKey();
			byte[] bytes = ((JavaFileObjectImpl) (entry.getValue())).getByteCode();
			bytes = getTransformedBytes(bytes);
			File outfile =  new File(patchedBinRoot, cls.replace(".", "/") + ".class");
			FileUtils.writeByteArrayToFile(outfile, bytes);
		}
	}
	
	byte[] getTransformedBytes(byte[] bytes) throws IOException {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(0);
		
		ClassVisitor cv = new RemoveTransientAdapter(cw);
		cr.accept(cv, 0);
		
		return cw.toByteArray();
	}
	
	List<String> getCompilerOptions() {
		List<String> compilerOptions = new ArrayList<String>();
		compilerOptions.add("-nowarn");
		compilerOptions.add("-source");
		compilerOptions.add("1.7");
		compilerOptions.add("-cp");
		String cpStr = binJavaDir;

		if (dependencies != null) {
			for (String str : dependencies)
				cpStr += (File.pathSeparator + str);
		}

		compilerOptions.add(cpStr);
		return compilerOptions;
	}
	
	
	// cannot use FileUtils.copyFile,otherwise there will be a problem for applying the patch
	void copyFile(File srcFile, File destFile) throws IOException {
		List<String> lines = FileUtils.readLines(srcFile, "UTF-8");
		FileUtils.writeLines(destFile, lines);
	}
}
