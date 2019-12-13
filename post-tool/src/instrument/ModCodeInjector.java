package instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.util.TraceClassVisitor;

import localization.MethodInfo;

public class ModCodeInjector {
	Map<String, Set<MethodInfo>> modifiedMethods;
	String patchedBinRoot;
	String pnPatchedBinRoot;
	String storeRoot;
	
	
	public ModCodeInjector(String patchedBinRoot, Map<String, Set<MethodInfo>> modifiedMethods, String pnPatchedBinRoot,
			String storeRoot) {
		this.patchedBinRoot = patchedBinRoot;
		this.modifiedMethods = modifiedMethods;
		this.pnPatchedBinRoot = pnPatchedBinRoot;
		this.storeRoot = storeRoot;
	}
	
	public void inject() throws IOException {
		for (Map.Entry<String, Set<MethodInfo>> entry : modifiedMethods.entrySet())
			inject(entry.getKey(), entry.getValue());
	}
	
	private void inject(String className, Set<MethodInfo> methods) throws IOException {
		String name = className.replace(".", "/") + ".class";
		File infile = new File(patchedBinRoot, name);
		InputStream is = new FileInputStream(infile);

		ClassReader cr = new ClassReader(is);
		ClassWriter cw = new ClassWriter(0);
		
/*		PrintWriter printWriter = new PrintWriter(System.out);
		TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);*/
		
		ClassVisitor cv = new ModClassAdapter(cw, className, methods, storeRoot);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		File outfile = new File(pnPatchedBinRoot, name);
		FileUtils.writeByteArrayToFile(outfile, cw.toByteArray());
	}
	
}
