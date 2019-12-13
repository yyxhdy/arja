package us.msu.cse.repair.core.util.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;

import us.msu.cse.repair.core.util.Helper;

public class FieldVisitor extends ASTVisitor {
	MethodDeclaration md;
	ITypeBinding curType;
	
	Set<IMethodBinding> methodBindings;
	Set<String> fields; 
	
	public FieldVisitor(MethodDeclaration md, ITypeBinding curType) {
		this.md = md;
		this.curType = curType;
		
		methodBindings = new HashSet<IMethodBinding>();
		fields = new HashSet<String>();
	}
	
	
	@Override 
	public boolean visit(SimpleName sn) {
		boolean flag1 = sn.getParent() instanceof FieldAccess;
		boolean flag2 = sn.getParent() instanceof QualifiedName;
		boolean flag3 = sn.getParent() instanceof SuperFieldAccess;	
		boolean flag4 = sn.resolveBinding() instanceof IVariableBinding;
		
		if (!flag1 && !flag2 && !flag3 && flag4) {
			IVariableBinding vb = (IVariableBinding) (sn.resolveBinding());
			if (vb!= null && vb.isField()) {
				ITypeBinding clsType = vb.getDeclaringClass();
				
				String rel = getRelation(curType, clsType);
				fields.add(rel + sn.getIdentifier());
			}
			
		}
		return true;
	}
	
	@Override 
	public boolean visit(FieldAccess fa) {
		boolean flag = fa.getExpression() instanceof ThisExpression;
		if (flag) {
			IVariableBinding vb = (IVariableBinding) fa.getName().resolveBinding();
			if (vb != null) {
				ITypeBinding clsType = vb.getDeclaringClass();
				String rel = getRelation(curType, clsType);
				fields.add(rel + fa.getName().getIdentifier());
			}
		}
		return true;
	}
	
	@Override 
	public boolean visit(SuperFieldAccess sfa) {
		IVariableBinding vb = (IVariableBinding) sfa.getName().resolveBinding();
		if (vb != null) {
			ITypeBinding clsType = vb.getDeclaringClass();
			String rel = getRelation(curType, clsType);
			fields.add(rel + sfa.getName().getIdentifier());
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation mi) {
		boolean flag1 = (mi.getExpression() instanceof ThisExpression);
		boolean flag2 = (mi.getExpression() == null);
		
		if (flag1 || flag2)
			methodBindings.add(mi.resolveMethodBinding().getMethodDeclaration());
		return true;
	}
	
	public String getRelation(ITypeBinding curType, ITypeBinding clsType) {
		if (curType == clsType)
			return "this.";
		else if (Helper.isSuperClass(curType, clsType)) 
			return "super.";
		else
			return "outer.";
	}
	
	
	
	public Set<String> getFields() {
		return fields;
	}
	
	public Set<IMethodBinding> getMethodBindings() {
		return methodBindings;
	}
	
}
