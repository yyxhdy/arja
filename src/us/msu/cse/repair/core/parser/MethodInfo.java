package us.msu.cse.repair.core.parser;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class MethodInfo {
	String returnTypeName;
	ITypeBinding returnTypeBinding;
	int mod;

	public MethodInfo(String returnTypeName, ITypeBinding returnTypeBinding, int mod) {
		this.returnTypeName = returnTypeName;
		this.returnTypeBinding = returnTypeBinding;
		this.mod = mod;
	}

	public ITypeBinding getReturnTypeBinding() {
		return this.returnTypeBinding;
	}

	public String getReturnTypeName() {
		return this.returnTypeName;
	}

	public int getModifiers() {
		return this.mod;
	}

	public boolean isStronglyReturnTypeMatched(MethodInfo mi) {
		if (returnTypeName.equals(mi.getReturnTypeName()))
			return true;

		return false;
	}

	public boolean isWeaklyReturnTypeMatched(MethodInfo mi) {
		ITypeBinding tb = mi.getReturnTypeBinding();

		if (tb != null && returnTypeBinding != null) {
			if (tb.isAssignmentCompatible(returnTypeBinding))
				return true;
		}

		return false;
	}

}
