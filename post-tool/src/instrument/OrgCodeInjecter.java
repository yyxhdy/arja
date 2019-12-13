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

public class OrgCodeInjecter {
	String binJavaDir;
	Map<String, Set<MethodInfo>> modifiedMethods;
	
	String injectedBinRoot;
	String storeRoot;
	

	
	public OrgCodeInjecter(String binJavaDir, Map<String, Set<MethodInfo>> modifiedMethods, 
			String injectedBinRoot, String storeRoot) {
		this.binJavaDir = binJavaDir;
		this.modifiedMethods = modifiedMethods;
		this.injectedBinRoot = injectedBinRoot;
		this.storeRoot = storeRoot;
	}
	
	public void inject() throws IOException {
		for (Map.Entry<String, Set<MethodInfo>> entry : modifiedMethods.entrySet())
			inject(entry.getKey(), entry.getValue());
	}
	
	private void inject(String className, Set<MethodInfo> methods) throws IOException {
		String name = className.replace(".", "/") + ".class";
		File infile = new File(binJavaDir, name);

		InputStream is = new FileInputStream(infile);
		
/*		for (MethodInfo m : methods) {
			System.out.println(m.getName() + m.getDesc());
		}*/

		ClassReader cr = new ClassReader(is);
		ClassWriter cw = new ClassWriter(0);
		
	//	PrintWriter printWriter = new PrintWriter(System.out);
	//	TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);
		
		ClassVisitor cv = new OrgClassAdapter(cw, className, methods, storeRoot);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		File outfile = new File(injectedBinRoot, name);
		FileUtils.writeByteArrayToFile(outfile, cw.toByteArray());
	}
}
