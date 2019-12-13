package testexecution;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class ClassFinder {
	Set<String> binJavaClasses;
	Set<String> binExecuteTestClasses;
	Set<String> binAbstractTestClasses;

	Set<String> dependences;

	private String binJavaDir;
	private String binTestDir;

	public ClassFinder(String binJavaDir, String binTestDir, Set<String> dependences)
			throws ClassNotFoundException, IOException {
		this.binJavaDir = binJavaDir;
		this.binTestDir = binTestDir;
		this.dependences = dependences;

		List<String> tempList = new ArrayList<String>();
		tempList.add(binJavaDir);
		tempList.add(binTestDir);
		if (dependences != null)
			tempList.addAll(dependences);

		URL urls[] = getURLs(tempList);

		URLClassLoader classLoader = new URLClassLoader(urls);

		binExecuteTestClasses = new HashSet<String>();
		binAbstractTestClasses = new HashSet<String>();
		binJavaClasses = new HashSet<String>();

		scanTestDir(classLoader);
		scanJavaDir();

		classLoader.close();

	}

	public ClassFinder(String binJavaDir, String binTestDir) throws ClassNotFoundException, IOException {
		this.binTestDir = binTestDir;
		this.binJavaDir = binJavaDir;

		URL testURL = new File(binTestDir).toURI().toURL();
		URL javaURL = new File(binJavaDir).toURI().toURL();

		URLClassLoader classLoader = new URLClassLoader(new URL[] { testURL, javaURL });

		binExecuteTestClasses = new HashSet<String>();
		binAbstractTestClasses = new HashSet<String>();
		binJavaClasses = new HashSet<String>();

		scanTestDir(classLoader);
		scanJavaDir();

		classLoader.close();
	}

	void scanTestDir(URLClassLoader classLoader) throws ClassNotFoundException {
		Collection<File> files = FileUtils.listFiles(new File(binTestDir), new SuffixFileFilter(".class"),
				TrueFileFilter.INSTANCE);

		File dir = new File(binTestDir);

		for (File file : files) {
			String relative = dir.toURI().relativize(file.toURI()).getPath();

			String temp = relative.replace("/", ".");
			String className = temp.substring(0, temp.length() - 6);

			Class<?> target = classLoader.loadClass(className);

			if (JUnitIdentifier.isJUnitTest(target)) {
				if (!isAbstractClass(target))
					binExecuteTestClasses.add(className);
				else
					binAbstractTestClasses.add(className);
			}
		}
	}

	void scanJavaDir() throws ClassNotFoundException {
		File dir = new File(binJavaDir);
		
		Collection<File> files = FileUtils.listFiles(dir, new SuffixFileFilter(".class"),
				TrueFileFilter.INSTANCE);

		for (File file : files) {
			String relative = dir.toURI().relativize(file.toURI()).getPath();

			String temp = relative.replace("/", ".");
			String className = temp.substring(0, temp.length() - 6);

			if (!binExecuteTestClasses.contains(className) && !binAbstractTestClasses.contains(className))
				binJavaClasses.add(className);
		}
	}

	public Set<String> findBinExecuteTestClasses() {
		return this.binExecuteTestClasses;
	}

	public Set<String> findBinJavaClasses() {
		return this.binJavaClasses;
	}

	URL[] getURLs(Collection<String> paths) throws MalformedURLException {
		URL[] urls = new URL[paths.size()];
		int i = 0;
		for (String path : paths) {
			File file = new File(path);
			URL url = file.toURI().toURL();
			urls[i++] = url;
		}

		return urls;
	}
	
	public static boolean isAbstractClass(Class<?> target) {
		int mod = target.getModifiers();
		boolean isAbstract = Modifier.isAbstract(mod);
		boolean isInterface = Modifier.isInterface(mod);

		if (isAbstract || isInterface)
			return true;

		return false;
	}
}
