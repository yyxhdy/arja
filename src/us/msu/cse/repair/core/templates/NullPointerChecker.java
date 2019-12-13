package us.msu.cse.repair.core.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

public class NullPointerChecker {
	Map<String, Expression> references;
	
	public NullPointerChecker() {
		references = new HashMap<String, Expression>();
	}
	
	// type can be SimpleName, QualifiedName, FieldAccess ....
	public void add(String ref, Expression node) {
		if (node instanceof ClassInstanceCreation)
			return;
		
		references.put(ref, node);
	}
	
	public int getSize() {
		return references.size();
	}
	
	public List<Expression> getReferences() {
		return new ArrayList<Expression>(references.values());
	}
}
