package preprocessing;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class TransientRemover {
	String orgBinJavaDir;
	String newBinJavaDir;
	
	public TransientRemover(String orgBinJavaDir, String newBinJavaDir) {
		this.orgBinJavaDir = orgBinJavaDir;
		this.newBinJavaDir = newBinJavaDir;
	}
	
	public void remove() throws IOException {
		Collection<File> files =  FileUtils.listFiles(new File(orgBinJavaDir), 
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for (File fl : files) {
			File nfl = getNewFile(fl);
			
			if (fl.getName().endsWith(".class")) {
				byte[] bytes = getTransformedBytes(fl);
				FileUtils.writeByteArrayToFile(nfl, bytes);
			}
			else
				FileUtils.copyFile(fl, nfl);
		}
	}
	
	public File getNewFile(File fl) {
		String relative = new File(orgBinJavaDir).toURI().relativize(fl.toURI()).getPath();
		return new File(newBinJavaDir, relative);
	}
	
	
	byte[] getTransformedBytes(File  fl) throws IOException {
		InputStream is = new FileInputStream(fl);
		ClassReader cr = new ClassReader(is);
		ClassWriter cw = new ClassWriter(0);
		
		ClassVisitor cv = new RemoveTransientAdapter(cw);
		cr.accept(cv, 0);
		
		return cw.toByteArray();
	}
}
