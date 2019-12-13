package us.msu.cse.repair.core.templates;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class OverloadedMethodDetector {
	Expression currentMethod;
	List<IMethodBinding> overloadedMethods;
	
	public OverloadedMethodDetector(Expression currentMethod,  
			List<IMethodBinding> overloadedMethods) {
		this.currentMethod = currentMethod;
		this.overloadedMethods = overloadedMethods;
	}
	
	public Expression getCurrentMethod() {
		return this.currentMethod;
	}
	
	public List<IMethodBinding> getOverloadedMethods() {
		return this.overloadedMethods;
	}
	
	public int getSize() {
		return this.overloadedMethods.size();
	}
}
