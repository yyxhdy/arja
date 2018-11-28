package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.util.Helper;

public class StatementInfoASTVisitor extends ASTVisitor {
	Map<String, VarInfo> vars;
	Map<String, VarInfo> thisVars;
	Map<String, VarInfo> superVars;

	Map<String, MethodInfo> methods;
	Map<String, MethodInfo> thisMethods;
	Map<String, MethodInfo> superMethods;

	List<Integer> varIDs;
	Map<String, List<Integer>> methodIDs;
	Map<String, List<Integer>> superMethodIDs;

	int varCount;
	int methodCount;

	Set<String> declaredVarNames;

	public StatementInfoASTVisitor(Map<String, VarInfo> vars, Map<String, VarInfo> thisVars,
			Map<String, VarInfo> superVars, Map<String, MethodInfo> methods, Map<String, MethodInfo> thisMethods,
			Map<String, MethodInfo> superMethods, List<Integer> varIDs, Map<String, List<Integer>> methodIDs,
			Map<String, List<Integer>> superMethodIDs) {
		// TODO Auto-generated constructor stub

		this.vars = vars;
		this.thisVars = thisVars;
		this.superVars = superVars;

		this.methods = methods;
		this.thisMethods = thisMethods;
		this.superMethods = superMethods;

		this.varIDs = varIDs;
		this.methodIDs = methodIDs;
		this.superMethodIDs = superMethodIDs;

		this.varCount = 0;
		this.methodCount = 0;

		this.declaredVarNames = new HashSet<String>();
	}

	@Override
	public boolean visit(FieldAccess fa) {
		Expression expression = fa.getExpression();

		if (expression instanceof ThisExpression) {
			IVariableBinding vb = (IVariableBinding) fa.getName().resolveBinding();

			if (vb != null) {
				VarInfo vi = Helper.getVarInfo(vb);
				thisVars.put(vb.getName(), vi);
			}
		}
		varCount++;
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess sfa) {
		IVariableBinding vb = (IVariableBinding) sfa.getName().resolveBinding();
		if (vb != null) {
			VarInfo vi = Helper.getVarInfo(vb);
			superVars.put(vb.getName(), vi);
		}
		varCount++;
		return true;
	}

	@Override
	public boolean visit(QualifiedName qn) {
		if (qn.getQualifier() instanceof SimpleName) {
			IBinding binding = qn.getQualifier().resolveBinding();
			if (binding != null && binding instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding) binding;
				VarInfo vi = Helper.getVarInfo(vb);
				vars.put(vb.getName(), vi);
			}
		}

		varCount++;
		return true;
	}

	@Override
	public boolean visit(SimpleName sn) {
		IBinding binding = sn.resolveBinding();

		ASTNode parent = sn.getParent();
		boolean flag1 = parent instanceof FieldAccess;
		boolean flag2 = parent instanceof SuperFieldAccess;
		boolean flag3 = parent instanceof QualifiedName;

		if (Helper.isDeclaredVar(sn))
			declaredVarNames.add(sn.getIdentifier());
		else if (binding != null && binding.getKind() == Binding.VARIABLE && !flag1 && !flag2 && !flag3) {
			IVariableBinding vb = (IVariableBinding) binding;
			String name = vb.getName();

			if (!declaredVarNames.contains(name) && !vb.isEnumConstant()) {
				VarInfo vi = Helper.getVarInfo(vb);
				vars.put(name, vi);
				varIDs.add(varCount);
			}
		}

		varCount++;
		return true;
	}

	@Override
	public boolean visit(MethodInvocation mi) {
		Expression expression = mi.getExpression();
		IMethodBinding mb = mi.resolveMethodBinding();

		if (mb != null) {
			String key = Helper.getMethodKey(mb);
			MethodInfo info = Helper.getMethodInfo(mb);

			if (expression == null || expression instanceof ThisExpression) {
				if (expression == null)
					methods.put(key, info);
				else
					thisMethods.put(key, info);

				if (!methodIDs.containsKey(key)) {
					List<Integer> ids = new ArrayList<Integer>();
					ids.add(methodCount);
					methodIDs.put(key, ids);
				} else
					methodIDs.get(key).add(methodCount);
			}

		}

		methodCount++;
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation smi) {
		IMethodBinding mb = smi.resolveMethodBinding();

		if (mb != null) {
			String key = Helper.getMethodKey(mb);
			MethodInfo info = Helper.getMethodInfo(mb);
			superMethods.put(key, info);

			if (!superMethodIDs.containsKey(key)) {
				List<Integer> ids = new ArrayList<Integer>();
				ids.add(methodCount);
				superMethodIDs.put(key, ids);
			} else
				superMethodIDs.get(key).add(methodCount);

		}

		methodCount++;
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}
}