package us.msu.cse.repair.core.parser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import us.msu.cse.repair.core.util.Helper;

public class FieldVarDetector {
	List<ModificationPoint> modificationPoints;

	Map<String, ITypeBinding> declaredClasses;

	Set<String> dependences;

	Map<String, Map<String, VarInfo>> declaredFieldMap;
	Map<String, Map<String, VarInfo>> inheritedFieldMap;
	Map<String, Map<String, VarInfo>> outerFieldMap;

	URLClassLoader classLoader;

	public FieldVarDetector(List<ModificationPoint> modificationPoints, Map<String, ITypeBinding> declaredClasses,
			Set<String> dependences) throws MalformedURLException {
		this.modificationPoints = modificationPoints;
		this.declaredClasses = declaredClasses;

		declaredFieldMap = new HashMap<String, Map<String, VarInfo>>();
		inheritedFieldMap = new HashMap<String, Map<String, VarInfo>>();
		outerFieldMap = new HashMap<String, Map<String, VarInfo>>();

		if (dependences != null) {
			URL[] urls = Helper.getURLs(dependences);
			classLoader = new URLClassLoader(urls);
		} else
			classLoader = new URLClassLoader(new URL[0]);
	}

	public void detect() throws ClassNotFoundException, IOException {
		for (ModificationPoint mp : modificationPoints)
			detectVisibleFields(mp);
		classLoader.close();
	}

	void detectVisibleFields(ModificationPoint mp) throws ClassNotFoundException {
		String className = mp.getLCNode().getClassName();

		detectVisibleFields(className);

		Map<String, VarInfo> declaredFields = declaredFieldMap.get(className);
		Map<String, VarInfo> inheritedFields = inheritedFieldMap.get(className);
		Map<String, VarInfo> outerFields = outerFieldMap.get(className);

		if (mp.isInStaticMethod()) {
			declaredFields = getStaticFields(declaredFields);
			inheritedFields = getStaticFields(inheritedFields);
		}

		mp.setDeclaredFields(declaredFields);
		mp.setInheritedFields(inheritedFields);
		mp.setOuterFields(outerFields);
	}

	void detectVisibleFields(String className) throws ClassNotFoundException {
		if (declaredFieldMap.containsKey(className))
			return;

		Map<String, VarInfo> fields;
		String superClassName = null, outerClassName = null;
		boolean isStaticClass;

		if (declaredClasses.containsKey(className)) {
			ITypeBinding tb = declaredClasses.get(className);
			IVariableBinding[] vbs = tb.getDeclaredFields();
			fields = Helper.getVarInfos(vbs);

			ITypeBinding superClass = tb.getSuperclass();
			if (superClass != null && !superClass.isInterface())
				superClassName = superClass.getBinaryName();

			ITypeBinding outerClass = tb.getDeclaringClass();
			if (outerClass != null && !outerClass.isInterface())
				outerClassName = outerClass.getBinaryName();

			isStaticClass = Modifier.isStatic(tb.getModifiers());
		} else {
			Class<?> target = classLoader.loadClass(className);
			Field[] fs = target.getDeclaredFields();
			fields = Helper.getVarInfos(fs);

			Class<?> superClass = target.getSuperclass();
			if (superClass != null && !superClass.isInterface())
				superClassName = superClass.getName();

			Class<?> outerClass = target.getDeclaringClass();
			if (outerClass != null && !outerClass.isInterface())
				outerClassName = outerClass.getName();

			isStaticClass = Modifier.isStatic(target.getModifiers());
		}
		declaredFieldMap.put(className, fields);

		Map<String, VarInfo> inheritedFields = new HashMap<String, VarInfo>();
		if (superClassName != null) {
			detectVisibleFields(superClassName);

			Map<String, VarInfo> declaredFieldsOfSuper = declaredFieldMap.get(superClassName);
			Map<String, VarInfo> inheritedFieldsOfSuper = inheritedFieldMap.get(superClassName);

			collectInheritedFields(inheritedFieldsOfSuper, inheritedFields, className, superClassName);
			collectInheritedFields(declaredFieldsOfSuper, inheritedFields, className, superClassName);
		}

		HashMap<String, VarInfo> outerFields = new HashMap<String, VarInfo>();
		if (outerClassName != null) {
			detectVisibleFields(outerClassName);

			Map<String, VarInfo> declaredFieldsOfOuter = declaredFieldMap.get(outerClassName);
			Map<String, VarInfo> inheritedFieldsOfOuter = inheritedFieldMap.get(outerClassName);
			Map<String, VarInfo> outerFieldsOfOuter = outerFieldMap.get(outerClassName);

			collectOuterFields(outerFieldsOfOuter, outerFields, isStaticClass);
			collectOuterFields(inheritedFieldsOfOuter, outerFields, isStaticClass);
			collectOuterFields(declaredFieldsOfOuter, outerFields, isStaticClass);

		}

		inheritedFieldMap.put(className, inheritedFields);
		outerFieldMap.put(className, outerFields);
	}

	Map<String, VarInfo> getStaticFields(Map<String, VarInfo> fields) {
		Map<String, VarInfo> staticFields = new HashMap<String, VarInfo>();

		for (Map.Entry<String, VarInfo> entry : fields.entrySet()) {
			if (Helper.isStaticVar(entry.getValue()))
				staticFields.put(entry.getKey(), entry.getValue());
		}

		return staticFields;
	}

	void collectOuterFields(Map<String, VarInfo> map, Map<String, VarInfo> outerFields, boolean isStaticClass) {

		if (!isStaticClass) {
			for (Map.Entry<String, VarInfo> entry : map.entrySet())
				outerFields.put(entry.getKey(), entry.getValue());
		} else {
			for (Map.Entry<String, VarInfo> entry : map.entrySet()) {
				if (Helper.isStaticVar(entry.getValue()))
					outerFields.put(entry.getKey(), entry.getValue());
			}
		}
	}

	void collectInheritedFields(Map<String, VarInfo> map, Map<String, VarInfo> inheritedFields, String className,
			String superClassName) {
		for (Map.Entry<String, VarInfo> entry : map.entrySet()) {
			boolean flag1 = Helper.isPublicVar(entry.getValue());
			boolean flag2 = Helper.isProtectedVar(entry.getValue());
			boolean flag3 = Helper.isPackagePrivateVar(entry.getValue())
					&& Helper.isInSamePackage(className, superClassName);

			if (flag1 || flag2 || flag3)
				inheritedFields.put(entry.getKey(), entry.getValue());
		}
	}
}
