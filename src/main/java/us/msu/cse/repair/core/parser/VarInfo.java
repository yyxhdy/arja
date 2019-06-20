package us.msu.cse.repair.core.parser;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import us.msu.cse.repair.core.util.Helper;

public class VarInfo {
	String typeName;

	IVariableBinding varBinding;

	int mod;

	public VarInfo(String typeName, IVariableBinding varBinding, int mod) {
		this.typeName = typeName;
		this.varBinding = varBinding;
		this.mod = mod;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public IVariableBinding getVariableBinding() {
		return this.varBinding;
	}

	public ITypeBinding getTypeBinding() {
		return this.varBinding.getVariableDeclaration().getType();
	}

	public int getModifiers() {
		return mod;
	}

	public boolean isStronglyTypeMatched(VarInfo vi) {
		if (typeName.equals(vi.getTypeName()))
			return true;

		return false;
	}

	public boolean isWeaklyTypeMatched(VarInfo vi) {
		if (varBinding == null || vi.getVariableBinding() == null)
			return false;
		
		ITypeBinding tb = vi.getTypeBinding();
		ITypeBinding typeBinding = varBinding.getVariableDeclaration().getType();

		if (tb != null && typeBinding != null) {
			if (tb.isAssignmentCompatible(typeBinding))
				return true;

			if (varBinding.isParameter() && Helper.isSameParentType(tb, typeBinding))
				return true;
		}

		return false;
	}
}
