package us.msu.cse.repair.core.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import us.msu.cse.repair.core.compiler.JavaFileObjectImpl;

import javax.tools.JavaFileObject;

public class CustomURLClassLoader extends URLClassLoader {

	private Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

	public CustomURLClassLoader(URL[] urls, final Map<String, JavaFileObject> classes) {
		super(urls);
		this.classes = classes;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
		JavaFileObject file = classes.get(qualifiedClassName);
		if (file != null) {
			byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
			return defineClass(qualifiedClassName, bytes, 0, bytes.length);
		}

		// Workaround for "feature" in Java 6
		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
		try {
			Class<?> c = Class.forName(qualifiedClassName);
			return c;
		} catch (ClassNotFoundException nf) {
			// Ignore and fall through
		}
		return super.findClass(qualifiedClassName);
	}

	/**
	 * Add a class name/JavaFileObject mapping
	 * 
	 * @param qualifiedClassName
	 *            the name
	 * @param javaFile
	 *            the file associated with the name
	 */
	void add(final String qualifiedClassName, final JavaFileObject javaFile) {
		classes.put(qualifiedClassName, javaFile);
	}

	public Map<String, JavaFileObject> getCompiledClasses() {
		return classes;
	}

}