package us.msu.cse.repair.core.util.visitors;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ClassInstanceAndArrayCreationVisitor extends ASTVisitor {
	Set<String> classes;

	public ClassInstanceAndArrayCreationVisitor() {
		classes = new HashSet<String>();
	}

	public Set<String> getClasses() {
		return classes;
	}

	@Override
	public boolean visit(ClassInstanceCreation cic) {
		ITypeBinding tb = cic.resolveTypeBinding();
		if (tb != null) {
			String cls = tb.getBinaryName();
			classes.add(cls);
		}
		return true;
	}

	@Override
	public boolean visit(ArrayCreation ac) {
		ITypeBinding tb = ac.resolveTypeBinding();
		if (tb != null && !tb.getElementType().isPrimitive()) {
			String cls = tb.getElementType().getBinaryName();
			classes.add(cls);
		}
		return true;
	}

}
