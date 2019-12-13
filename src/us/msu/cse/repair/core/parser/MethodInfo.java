package us.msu.cse.repair.core.parser;

import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class MethodInfo {
	List<IMethodBinding> methodBindingList;

	List<String> returnTypeNameList;
	List<Integer> modList;
	List<String> parameterTypeNamesList;
	
	String name;
	int numberOfParameters;

	public MethodInfo(String name, int numberOfParameters, List<IMethodBinding> methodBindingList,
			List<String> returnTypeNameList, List<String> parameterTypeNamesList, List<Integer> modList) {
		this.methodBindingList = methodBindingList;
		this.returnTypeNameList = returnTypeNameList;
		this.parameterTypeNamesList = parameterTypeNamesList;
		this.modList = modList;
		this.name = name;
		this.numberOfParameters = numberOfParameters;
	}

	public IMethodBinding getMethodBinding(int k) {
		return methodBindingList.get(k);
	}

	public ITypeBinding getReturnTypeBinding(int k) {
		IMethodBinding mb = methodBindingList.get(k);
		if (mb != null)
			return mb.getMethodDeclaration().getReturnType();
		else
			return null;
	}

	public String getReturnTypeName(int k) {
		return returnTypeNameList.get(k);
	}

	public int getModifiers(int k) {
		return modList.get(k);
	}

	public String getParameterTypeNames(int k) {
		return parameterTypeNamesList.get(k);
	}
	
	public ITypeBinding[] getParameterTypeBindings(int k) {
		IMethodBinding mb = methodBindingList.get(k);
		if (mb != null) 
			return mb.getMethodDeclaration().getParameterTypes();
		else 
			return null;
	}

	public void add(IMethodBinding mb, String returnTypeName, String parameters, int mod) {
		this.methodBindingList.add(mb);
		this.returnTypeNameList.add(returnTypeName);
		this.parameterTypeNamesList.add(parameters);
		this.modList.add(mod);
	}
	
	public void add(MethodInfo mi) {
		this.methodBindingList.addAll(mi.methodBindingList);
		this.returnTypeNameList.addAll(mi.returnTypeNameList);
		this.parameterTypeNamesList.addAll(mi.parameterTypeNamesList);
		this.modList.addAll(mi.modList);
	}

	public boolean isStronglyReturnTypeMatched(MethodInfo mi) {
		String returnTypeName1 = returnTypeNameList.get(0);

		for (int k = 0; k < mi.getSize(); k++) {
			String returnTypeName2 = mi.getReturnTypeName(k);
			if (returnTypeName1.equals(returnTypeName2))
				return true;
		}
		return false;
	}

	public boolean isWeaklyReturnTypeMatched(MethodInfo mi) {
		ITypeBinding returnTypeBinding1 = getReturnTypeBinding(0);
		for (int i = 0; i < mi.getSize(); i++) {
			ITypeBinding returnTypeBinding2 = mi.getReturnTypeBinding(i);

			if (returnTypeBinding1 != null && returnTypeBinding2 != null
					&& returnTypeBinding2.isAssignmentCompatible(returnTypeBinding1))
				return true;
		}
		return false;
	}

	public int getSize() {
		return methodBindingList.size();
	}
	
	public String getName() {
		return this.name;
	}

	public int getNumberOfParameters() {
		return this.numberOfParameters;
	}
}
