package us.msu.cse.repair.core.compiler;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class FileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {
	private final ClassLoaderImpl classLoader;

	public FileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
		super(fileManager);
		this.classLoader = classLoader;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind,
			FileObject outputFile) throws IOException {
		JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
		classLoader.add(qualifiedName, file);
		return file;
	}
}
