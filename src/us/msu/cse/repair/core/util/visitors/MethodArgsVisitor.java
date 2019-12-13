package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodArgsVisitor extends ASTVisitor {
	List<Expression> args;
	
	public MethodArgsVisitor() {
		args = new ArrayList<Expression>();
	}
	
	@Override
	public boolean visit(MethodInvocation mi) {	
		for (Object o : mi.arguments()) {
			Expression exp = (Expression) o;	
			ITypeBinding tb = exp.resolveTypeBinding();
			Object cons = exp.resolveConstantExpressionValue();
			if (tb!= null && cons == null && (tb.isClass() || tb.isInterface() || tb.isArray()))
				args.add(exp);
		}
		return true;
	}
	
	public List<Expression> getMethodArgs() {
		return this.args;
	}
}
