package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import us.msu.cse.repair.core.util.Helper;

public class BooleanExpressionVisitor extends ASTVisitor {
	List<Expression> booleanExpressions;
	
	public BooleanExpressionVisitor() {
		booleanExpressions = new ArrayList<Expression>();
	}
	
	@Override
	public boolean visit(SimpleName sn) {
		ITypeBinding tb = sn.resolveTypeBinding();
		IBinding binding = sn.resolveBinding();
		boolean flag1 = ((binding != null) && (binding instanceof IVariableBinding));
		boolean flag2 = Helper.isDeclaredVar(sn);
		if (Helper.isBooleanType(tb) && flag1 && !flag2)
			booleanExpressions.add(sn);
		return true;
	}
	
	@Override
	public boolean visit(QualifiedName qn) {
		ITypeBinding tb = qn.resolveTypeBinding();
		IBinding binding = qn.resolveBinding();
		boolean flag = (binding != null && binding.getKind() == IBinding.VARIABLE);
		if (Helper.isBooleanType(tb) && flag)
			booleanExpressions.add(qn);
		return true;
	}
	
	@Override
	public boolean visit(FieldAccess fa) {
		if (fa.getExpression() instanceof ThisExpression) 
			return true;
		
		ITypeBinding tb = fa.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(fa);
		return true;
	}
	
	@Override
	public boolean visit(SuperFieldAccess sfa) {
		ITypeBinding tb = sfa.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(sfa);
		return true;
	}
	
	@Override
	public boolean visit(ArrayAccess aa) {
		ITypeBinding tb = aa.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(aa);
		return true;
	}
	
	@Override
	public boolean visit(InfixExpression ie) {
		ITypeBinding tb = ie.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(ie);
		return true;
	}
	
	@Override
	public boolean visit(PrefixExpression pe) {
		if (pe.getOperator() == PrefixExpression.Operator.NOT)
			return true;
		
		ITypeBinding tb = pe.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(pe);
		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression pe) {
		ITypeBinding tb = pe.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(pe);
		return true;
	}
	
	@Override
	public boolean visit(InstanceofExpression ie) {
		ITypeBinding tb = ie.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(ie);
		return true;
	}
	
	@Override
	public boolean visit(ConditionalExpression ce) {
		ITypeBinding tb = ce.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(ce);
		return true;
	}
	
	
	@Override
	public boolean visit(MethodInvocation mi) {
		if (mi.getExpression() instanceof ThisExpression)
			return true;
		
		ITypeBinding tb = mi.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(mi);
		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation smi) {
		ITypeBinding tb = smi.resolveTypeBinding();
		if (Helper.isBooleanType(tb))
			booleanExpressions.add(smi);
		return true;
	}
	
	public List<Expression> getExpressions() {
		return this.booleanExpressions;
	}
	
}
