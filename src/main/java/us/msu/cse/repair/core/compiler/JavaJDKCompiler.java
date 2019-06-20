package us.msu.cse.repair.core.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler.CompilationTask;

public class JavaJDKCompiler {
	private final ClassLoaderImpl classLoader;
	private final JavaCompiler compiler;
	private final FileManagerImpl javaFileManager;
	private final List<String> options;
	private DiagnosticCollector<JavaFileObject> diagnostics;

	public JavaJDKCompiler(ClassLoader parentLoader, List<String> options) {
		compiler = ToolProvider.getSystemJavaCompiler();

		if (compiler == null) {
			throw new IllegalStateException(
					"Cannot find the system Java compiler. " + "Check that your class path includes tools.jar");
		}
		classLoader = new ClassLoaderImpl(parentLoader);
		diagnostics = new DiagnosticCollector<JavaFileObject>();

		final JavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		javaFileManager = new FileManagerImpl(fileManager, classLoader);

		this.options = options;
	}

	public synchronized boolean compile(final Map<String, String> classes) throws Exception {
		List<JavaFileObject> sources = new ArrayList<JavaFileObject>();
		for (Entry<String, String> entry : classes.entrySet()) {
			String sourceFilePath = entry.getKey();
			String sourceCode = entry.getValue();

			JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(sourceFilePath, sourceCode);
			sources.add(javaFileObject);
		}
		// Get a CompliationTask from the compiler and compile the sources
		final CompilationTask task = compiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
		return task.call();
	}

	public synchronized boolean compile(String sourceFilePath, String sourceCode) throws Exception {
		Map<String, String> classes = new HashMap<String, String>(1);
		classes.put(sourceFilePath, sourceCode);
		return compile(classes);
	}

	public ClassLoaderImpl getClassLoader() {
		return classLoader;
	}

	public List<String> getErrors() {
		List<String> errors = new ArrayList<String>();
		for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
			if (d.getKind() == Kind.ERROR || d.getKind() == Kind.MANDATORY_WARNING)
				errors.add(d.toString());
		}
		return errors;
	}
}
