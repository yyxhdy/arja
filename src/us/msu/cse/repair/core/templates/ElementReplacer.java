package us.msu.cse.repair.core.templates;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class ElementReplacer {
	// SimpleName
	// FieldAccess-1, FieldAccess-2
	// SuperFieldAccess-1, SuperFieldAccess-2
	// QualifiedName-1, QualifiedName-2, QualifiedName-3
	// MethodInvocation
	// NumberLiteral
	// CastExpression
	
	List<ASTNode> replacements;	
	ASTNode target;
	
	public ElementReplacer(ASTNode target, List<ASTNode> replacements) {
		this.target = target;
		this.replacements = replacements;
	}
		
	
	public ASTNode getTarget() {
		return this.target;
	}
	
	public void setTarget(ASTNode target) {
		this.target = target;
	}
	
	public List<ASTNode> getReplacements() {
		return this.replacements;
	}
	
	public void setReplacements(List<ASTNode> replacements) {
		this.replacements = replacements;
	}
	
	public int getSize() {
		return this.replacements.size();
	}
}
