package us.msu.cse.repair.core.util.visitors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AllSimpleRefASTVisitor extends ASTVisitor {
	Map<String, Integer> argMap;
	Set<Integer> argIndexes;
	
	
	public AllSimpleRefASTVisitor(Map<String, Integer> argMap) {
		this.argMap = argMap;
		argIndexes = new HashSet<>();
	}
	
	@Override
	public boolean visit(Assignment as) {
		Expression left = as.getLeftHandSide();
		Expression right = as.getRightHandSide();
		
		handleExpression(left, right);
		return true;
	}
	
	@Override 
	public boolean visit(VariableDeclarationFragment vdf) {
		Expression left = vdf.getName();
		Expression right = vdf.getInitializer();
		
		handleExpression(left, right);
		return true;
	}
	
	
	@Override
	public boolean visit(MethodInvocation mi) {
		handleExpression(mi.getExpression());	
		return true;
	}
	
	@Override
	public boolean visit(FieldAccess fa) {
		handleExpression(fa.getExpression());
		return true;
	}
	
	
	@Override
	public boolean visit(QualifiedName qn) {
		handleExpression(qn.getQualifier());
		return true;
	}
	
	boolean isVariable(Expression exp) {
		if (exp != null && exp instanceof Name) {
			Name sn = (Name) exp;
			if (sn.resolveBinding().getKind() == IBinding.VARIABLE) 
				return true;
		}
		
		return false;
	}
	
	void handleExpression(Expression exp) {		
		if (!isVariable(exp))
			return;
		
		Name sn = (Name) exp;
		String identity = sn.getFullyQualifiedName();
		
		if (argMap.containsKey(identity))
			argIndexes.add(argMap.get(identity));
	}
	
	void handleExpression(Expression left, Expression right) {
		if (right instanceof CastExpression) {
			CastExpression ce = (CastExpression) right;
			right = ce.getExpression();
		}
		
		boolean flag1 = isVariable(left);
		boolean flag2 = isVariable(right);
		
		if (flag1 && flag2) {
			Name sn1 = (Name) left;
			Name sn2 = (Name) right;
			
			String identity = sn2.getFullyQualifiedName();
			
			if (argMap.containsKey(identity)) {
				int index = argMap.get(identity);
				argMap.put(sn1.getFullyQualifiedName(), index);
			}
		}
	}
	
	public Set<Integer> getArgIndexes() {
		return this.argIndexes;
	}
}
