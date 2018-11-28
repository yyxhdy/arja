package us.msu.cse.repair.core.parser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import us.msu.cse.repair.core.util.Helper;

public class MethodDetector {
	List<ModificationPoint> modificationPoints;

	Map<String, ITypeBinding> declaredClasses;

	Set<String> dependences;
	URLClassLoader classLoader;

	Map<String, Map<String, MethodInfo>> declaredMethodMap;
	Map<String, Map<String, MethodInfo>> inheritedMethodMap;
	Map<String, Map<String, MethodInfo>> outerMethodMap;

	public MethodDetector(List<ModificationPoint> modificationPoints, Map<String, ITypeBinding> declaredClasses,
			Set<String> dependences) throws MalformedURLException {
		this.modificationPoints = modificationPoints;
		this.declaredClasses = declaredClasses;
		this.dependences = dependences;

		declaredMethodMap = new HashMap<String, Map<String, MethodInfo>>();
		inheritedMethodMap = new HashMap<String, Map<String, MethodInfo>>();
		outerMethodMap = new HashMap<String, Map<String, MethodInfo>>();

		if (dependences != null) {
			URL[] urls = Helper.getURLs(dependences);
			classLoader = new URLClassLoader(urls);
		} else
			classLoader = new URLClassLoader(new URL[0]);

	}

	public void detect() throws ClassNotFoundException, IOException {
		for (ModificationPoint mp : modificationPoints)
			detectVisibleMethods(mp);
		classLoader.close();
	}

	private void detectVisibleMethods(ModificationPoint mp) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		String className = mp.getLCNode().getClassName();

		detectVisibleMethods(className);

		Map<String, MethodInfo> declaredMethods = declaredMethodMap.get(className);
		Map<String, MethodInfo> inheritedMethods = inheritedMethodMap.get(className);
		Map<String, MethodInfo> outerMethods = outerMethodMap.get(className);

		if (mp.isInStaticMethod()) {
			declaredMethods = getStaticMethods(declaredMethods);
			inheritedMethods = getStaticMethods(inheritedMethods);
		}

		mp.setDeclaredMethods(declaredMethods);
		mp.setInheritedMethods(inheritedMethods);
		mp.setOuterMethods(outerMethods);
	}

	void detectVisibleMethods(String className) throws ClassNotFoundException {
		if (declaredMethodMap.containsKey(className))
			return;

		Map<String, MethodInfo> methods;
		String superClassName = null, outerClassName = null;
		boolean isStaticClass;

		if (declaredClasses.containsKey(className)) {
			ITypeBinding tb = declaredClasses.get(className);
			IMethodBinding[] mbs = tb.getDeclaredMethods();

			methods = Helper.getMethodInfos(mbs);

			ITypeBinding superClass = tb.getSuperclass();
			if (superClass != null && !superClass.isInterface())
				superClassName = superClass.getBinaryName();

			ITypeBinding outerClass = tb.getDeclaringClass();
			if (outerClass != null && !outerClass.isInterface())
				outerClassName = outerClass.getBinaryName();

			isStaticClass = Modifier.isStatic(tb.getModifiers());
		} else {
			Class<?> target = classLoader.loadClass(className);
			Method[] mds = target.getDeclaredMethods();
			methods = Helper.getMethodInfos(mds);

			Class<?> superClass = target.getSuperclass();
			if (superClass != null && !superClass.isInterface())
				superClassName = superClass.getName();

			Class<?> outerClass = target.getDeclaringClass();
			if (outerClass != null && !outerClass.isInterface())
				outerClassName = outerClass.getName();

			isStaticClass = Modifier.isStatic(target.getModifiers());
		}
		declaredMethodMap.put(className, methods);

		Map<String, MethodInfo> inheritedMethods = new HashMap<String, MethodInfo>();
		if (superClassName != null) {
			detectVisibleMethods(superClassName);

			Map<String, MethodInfo> declaredMethodsOfSuper = declaredMethodMap.get(superClassName);
			Map<String, MethodInfo> inheritedMethodsOfSuper = inheritedMethodMap.get(superClassName);

			collectInheritedMethods(inheritedMethodsOfSuper, inheritedMethods, className, superClassName);
			collectInheritedMethods(declaredMethodsOfSuper, inheritedMethods, className, superClassName);
		}

		HashMap<String, MethodInfo> outerMethods = new HashMap<String, MethodInfo>();
		if (outerClassName != null) {
			detectVisibleMethods(outerClassName);

			Map<String, MethodInfo> declaredMethodsOfOuter = declaredMethodMap.get(outerClassName);
			Map<String, MethodInfo> inheritedMethodsOfOuter = inheritedMethodMap.get(outerClassName);
			Map<String, MethodInfo> outerMethodsOfOuter = outerMethodMap.get(outerClassName);

			collectOuterMethods(outerMethodsOfOuter, outerMethods, isStaticClass);
			collectOuterMethods(inheritedMethodsOfOuter, outerMethods, isStaticClass);
			collectOuterMethods(declaredMethodsOfOuter, outerMethods, isStaticClass);

			filterOuterMethods(outerMethods, methods, inheritedMethods);

		}

		inheritedMethodMap.put(className, inheritedMethods);
		outerMethodMap.put(className, outerMethods);
	}

	void filterOuterMethods(Map<String, MethodInfo> outerMethods, Map<String, MethodInfo> methods,
			Map<String, MethodInfo> inheritedMethods) {
		Iterator<String> iterator = outerMethods.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			if (containsMethodName(key, methods) || containsMethodName(key, inheritedMethods))
				iterator.remove();
		}
	}

	boolean containsMethodName(String key, Map<String, MethodInfo> methods) {
		String methodName = Helper.getMethodName(key);

		for (String str : methods.keySet()) {
			String name = Helper.getMethodName(str);
			if (name.equals(methodName))
				return true;
		}
		return false;
	}

	void collectInheritedMethods(Map<String, MethodInfo> map, Map<String, MethodInfo> inheritedMethods,
			String className, String superClassName) {
		for (Map.Entry<String, MethodInfo> entry : map.entrySet()) {
			boolean flag1 = Helper.isPublicMethod(entry.getValue());
			boolean flag2 = Helper.isProtectedMethod(entry.getValue());
			boolean flag3 = Helper.isPackagePrivateMethod(entry.getValue())
					&& Helper.isInSamePackage(className, superClassName);

			if (flag1 || flag2 || flag3)
				inheritedMethods.put(entry.getKey(), entry.getValue());
		}
	}

	void collectOuterMethods(Map<String, MethodInfo> map, Map<String, MethodInfo> outerMethods, boolean isStaticClass) {

		if (!isStaticClass) {
			for (Map.Entry<String, MethodInfo> entry : map.entrySet())
				outerMethods.put(entry.getKey(), entry.getValue());
		} else {
			for (Map.Entry<String, MethodInfo> entry : map.entrySet()) {
				if (Helper.isStaticMethod(entry.getValue()))
					outerMethods.put(entry.getKey(), entry.getValue());
			}
		}
	}

	Map<String, MethodInfo> getStaticMethods(Map<String, MethodInfo> methods) {
		Map<String, MethodInfo> staticMethods = new HashMap<String, MethodInfo>();

		for (Map.Entry<String, MethodInfo> entry : methods.entrySet()) {
			if (Helper.isStaticMethod(entry.getValue()))
				staticMethods.put(entry.getKey(), entry.getValue());
		}

		return staticMethods;
	}

}
