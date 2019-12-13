package us.msu.cse.repair.core.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.util.Helper;

public class StaticMFDetector {
	List<ExtendedModificationPoint> modificationPoints;
	Map<String, ITypeBinding> declaredClasses;
	
	Map<String, Set<IMethodBinding>> availableStaticMethodMap;
	Map<String, Set<IVariableBinding>> availableStaticFieldMap;
	
	boolean withinPackage;
	
	public StaticMFDetector(List<ExtendedModificationPoint> modificationPoints,
			Map<String, ITypeBinding> declaredClasses, boolean withinPackage) {
		this.modificationPoints = modificationPoints;
		this.declaredClasses = declaredClasses;
		this.withinPackage = withinPackage;
		availableStaticMethodMap = new HashMap<String, Set<IMethodBinding>>();
		availableStaticFieldMap = new HashMap<String, Set<IVariableBinding>>();
		
	}

	public StaticMFDetector(List<ExtendedModificationPoint> modificationPoints,
			Map<String, ITypeBinding> declaredClasses) {
		this(modificationPoints, declaredClasses, true);
	}

	public void detect() {
		for (ExtendedModificationPoint mp : modificationPoints)
			detectAvailableStaticMFs(mp);
	}

	private void detectAvailableStaticMFs(ExtendedModificationPoint mp) {
		Statement statement = mp.getStatement();
		
		AbstractTypeDeclaration abstractTypeDeclaration = Helper.getAbstractTypeDeclaration(statement);
		if (abstractTypeDeclaration == null || abstractTypeDeclaration.resolveBinding() == null)
			return;
		
		ITypeBinding typeBinding = abstractTypeDeclaration.resolveBinding();		
		String curPackageName = typeBinding.getPackage().getName();
		String clsName = typeBinding.getBinaryName();
		
		if (availableStaticMethodMap.containsKey(clsName)) {
			mp.setAvailableStaticMethods(availableStaticMethodMap.get(clsName));
			mp.setAvailableStaticFields(availableStaticFieldMap.get(clsName));
			return;
		}
		
		Set<IMethodBinding> availableStaticMethods = new HashSet<IMethodBinding>();
		Set<IVariableBinding> availableStaticFields = new HashSet<IVariableBinding>();
					
				
		Set<String> packageNames = new HashSet<String>();
		Set<String> typeDeclNames = new HashSet<String>();
			
		packageNames.add(curPackageName);
		if (!withinPackage) {
			CompilationUnit unit = (CompilationUnit) statement.getRoot();
			for (Object obj : unit.imports()) {
				ImportDeclaration importDecl = (ImportDeclaration) obj;
				String name = importDecl.getName().toString();
				if (importDecl.isOnDemand())
					packageNames.add(name);
				else
					typeDeclNames.add(name);
			}
		}
		
		for (ITypeBinding tb : declaredClasses.values()) {
			if (tb == typeBinding)
				continue;

			String pkName = tb.getPackage().getName();
			if (packageNames.contains(pkName))
				collectAvailableStaticMFs(tb, availableStaticMethods, availableStaticFields, pkName, curPackageName);
		}
		
		
		if (!withinPackage) {
			for (String tdn : typeDeclNames) {
				ITypeBinding tb = declaredClasses.get(tdn);
				if (tb != null && tb != typeBinding) {
					String pkName = tb.getPackage().getName();
					collectAvailableStaticMFs(tb, availableStaticMethods, availableStaticFields, pkName,
							curPackageName);
				}
			}
		}

		availableStaticMethodMap.put(clsName, availableStaticMethods);
		availableStaticFieldMap.put(clsName, availableStaticFields);
		
		mp.setAvailableStaticMethods(availableStaticMethods);
		mp.setAvailableStaticFields(availableStaticFields);
	}
	
	
	void collectAvailableStaticMFs(ITypeBinding tb, Set<IMethodBinding> availableStaticMethods,
			Set<IVariableBinding> availableStaticFields, String pkName, String curPackageName) {
		collectAvailableStaticMethods(tb, availableStaticMethods, pkName, curPackageName);
		collectAvailableStaticFields(tb, availableStaticFields, pkName, curPackageName);
	}

	void collectAvailableStaticMethods(ITypeBinding tb, Set<IMethodBinding> availableStaticMethods, String pkName,
			String curPackageName) {
		for (IMethodBinding mb : tb.getDeclaredMethods()) {
			int mod = mb.getModifiers();
			
			if (!Modifier.isStatic(mod))
				continue;

			if (pkName.equals(curPackageName)) {
				if (!Modifier.isPrivate(mod))
					availableStaticMethods.add(mb);
			} else {
				if (Modifier.isPublic(mod))
					availableStaticMethods.add(mb);
			}
		}
	}

	void collectAvailableStaticFields(ITypeBinding tb, Set<IVariableBinding> availableStaticFields, String pkName,
			String curPackageName) {
		for (IVariableBinding vb : tb.getDeclaredFields()) {
			int mod = vb.getModifiers();
			
			if (!Modifier.isStatic(mod))
				continue;
			
			if (pkName.equals(curPackageName)) {
				if (!Modifier.isPrivate(mod))
					availableStaticFields.add(vb);
			} else {
				if (Modifier.isPublic(mod))
					availableStaticFields.add(vb);
			}
		}
	}

}
