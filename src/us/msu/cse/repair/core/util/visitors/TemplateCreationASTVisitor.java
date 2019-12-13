package us.msu.cse.repair.core.util.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;


import us.msu.cse.repair.core.parser.ExtendedModificationPoint;
import us.msu.cse.repair.core.templates.CastChecker;
import us.msu.cse.repair.core.templates.DivideZeroChecker;
import us.msu.cse.repair.core.templates.ElementReplacer;
import us.msu.cse.repair.core.templates.NullPointerChecker;
import us.msu.cse.repair.core.templates.OverloadedMethodDetector;
import us.msu.cse.repair.core.templates.RangeChecker;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.templates.TemplatePreparation;

public class TemplateCreationASTVisitor extends ASTVisitor {	
	List<ElementReplacer> elementReplacers;
	
	NullPointerChecker nullPointerChecker;

	CastChecker castChecker;
	DivideZeroChecker divideZeroChecker;
	
	RangeChecker rangeChecker;
	
	List<OverloadedMethodDetector> overloadedMethodDetectors;
	
	Map<IMethodBinding, MethodDeclaration> methodDeclarations;
	
	ExtendedModificationPoint mp;
	
	static final String[] namesOfList = { "java.util.List", "java.util.AbstractList",
			"java.util.AbstractSequentialList", "java.util.ArrayList", "java.util.AttributeList",
			"java.util.CopyOnWriteArrayList", "java.util.LinkedList", "java.util.RoleList",
			"java.util.RoleUnresolvedList", "java.util.Stack", "java.util.Vector" };
	
	public TemplateCreationASTVisitor(ExtendedModificationPoint mp,
			Map<IMethodBinding, MethodDeclaration> methodDeclarations) {
		this.mp = mp;
		this.methodDeclarations = methodDeclarations;
		
		elementReplacers = new ArrayList<ElementReplacer>();
		nullPointerChecker = new NullPointerChecker();
		
		castChecker = new CastChecker();
		divideZeroChecker = new DivideZeroChecker();	
		rangeChecker = new RangeChecker();
		
		overloadedMethodDetectors = new ArrayList<OverloadedMethodDetector>();
	}
	
	
	@Override
	public boolean visit(SimpleName sn) {
		if (Helper.isForInitializersVar(sn))
			return true;
		
		IBinding binding = sn.resolveBinding();

		ASTNode parent = sn.getParent();
		boolean flag1 = (binding!= null && binding.getKind() == IBinding.VARIABLE);
		boolean flag2 = parent instanceof FieldAccess;
		boolean flag3 = parent instanceof SuperFieldAccess;
		boolean flag4 = parent instanceof QualifiedName;
		boolean flag5 = Helper.isDeclaredVar(sn);
		
		if (flag1 && !flag2 && !flag3 && !flag4 && !flag5) {
			IVariableBinding vb = (IVariableBinding) binding;
			ElementReplacer ep = TemplatePreparation.createReplacerForSimpleName(sn, vb, mp, sn);
			elementReplacers.add(ep);
		}
		
		return true;
	}
	
	@Override
	public boolean visit(FieldAccess fa) {
		Expression exp = fa.getExpression();
		IVariableBinding vb = fa.resolveFieldBinding();
		
		if (vb == null)
			return true;
		
		if (exp instanceof ThisExpression) {
			ElementReplacer ep = TemplatePreparation.createReplacerForFieldAccess_1(fa, vb, mp);
			elementReplacers.add(ep);
		}
		else if (Helper.isSpecialFieldAccess(fa)) {
			ElementReplacer ep1 = TemplatePreparation.createReplacerForFieldAccess_1(fa, vb, mp);
			ElementReplacer ep2 = TemplatePreparation.createReplacerForFieldAccess_2(fa, vb, mp);
			elementReplacers.add(ep1);
			elementReplacers.add(ep2);
		}
		else {
			ElementReplacer ep = TemplatePreparation.createReplacerForFieldAccess_2(fa, vb, mp);
			elementReplacers.add(ep);
		}
		
		if (!(exp instanceof ThisExpression)) {
			nullPointerChecker.add(exp.toString(), exp);
		}
		
		return true;
	}
	
	@Override
	public boolean visit(SuperFieldAccess sfa) {	
		IVariableBinding vb = sfa.resolveFieldBinding();
		if (vb != null) {
			ElementReplacer ep = TemplatePreparation.createReplacerForSuperFieldAccess(sfa, vb, mp);
			elementReplacers.add(ep);
		}
		return true;
	}
	
	@Override
	public boolean visit(QualifiedName qn) {
		Name qualifier = qn.getQualifier();
		SimpleName name = qn.getName();
		
		IBinding nameBinding = name.resolveBinding();
		
		ITypeBinding qualifierType = qualifier.resolveTypeBinding();
		if (qualifierType == null)
			return true;
		
		if (qualifier instanceof SimpleName) {
			SimpleName qualifierS = (SimpleName) qualifier;
			IBinding binding = qualifierS.resolveBinding();
			
			if (binding != null && binding instanceof IVariableBinding) {
				IVariableBinding vbq = (IVariableBinding) binding;
				ElementReplacer ep = TemplatePreparation.createReplacerForQualifiedName_1(qn, vbq, mp);
				elementReplacers.add(ep);
			}
			
			if (nameBinding != null && nameBinding instanceof IVariableBinding) {
				IVariableBinding vbn = (IVariableBinding) nameBinding;
				ElementReplacer ep1 = TemplatePreparation.createReplacerForQualifiedName_2(qn, vbn, mp);
				ElementReplacer ep2 = TemplatePreparation.createReplacerForQualifiedName_3(qn, vbn, mp);
				elementReplacers.add(ep1);
				elementReplacers.add(ep2);
			}
		}
		else if (nameBinding != null && nameBinding instanceof IVariableBinding) {
			IVariableBinding vbn = (IVariableBinding) nameBinding;
			ElementReplacer ep1 = TemplatePreparation.createReplacerForQualifiedName_2(qn, vbn, mp);
			elementReplacers.add(ep1);
			
			if (qualifier.resolveBinding().getKind() != IBinding.VARIABLE) {
				ElementReplacer ep2 = TemplatePreparation.createReplacerForQualifiedName_3(qn, vbn, mp);
				elementReplacers.add(ep2);
			}
			
		}
		
		IBinding ibd = qualifier.resolveBinding();
		if (ibd != null && ibd instanceof IVariableBinding) {
			nullPointerChecker.add(qualifier.toString(), qualifier);
		}
		
		return true;
	}
	
	
	@Override
	public boolean visit(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();
		if (mb != null) {
			ElementReplacer ep = TemplatePreparation.createReplacerForMethodInvocation(mi, mb, mp);
			elementReplacers.add(ep);
		}
		
		Expression exp = mi.getExpression();
		if (exp != null && !(exp instanceof ThisExpression) && !Helper.isClassName(exp)) {
			nullPointerChecker.add(exp.toString(), exp);
		}	
		
		if (mb != null) {
			OverloadedMethodDetector omd = TemplatePreparation.detectOverloadedMethods(mi, mp);
			overloadedMethodDetectors.add(omd);
		}
		
		if (exp != null && exp.resolveTypeBinding() != null) {
			String methodName = mi.getName().getIdentifier();
			ITypeBinding tb = exp.resolveTypeBinding();
			
			boolean flag1 = Arrays.asList(namesOfList).contains(tb.getBinaryName());
			boolean flag2 = methodName.equals("get");
			
			boolean flag3 = tb.getQualifiedName().equals("java.lang.String");
			boolean flag4 = methodName.equals("substring");
			boolean flag5 = methodName.equals("charAt");
			boolean flag6 = (mi.arguments().size() == 2);
			
			if (flag1 && flag2) 
				rangeChecker.add(exp, (Expression) (mi.arguments().get(0)), "List");
			if (flag3 && flag4 && flag6) 
				rangeChecker.add(exp, (Expression) (mi.arguments().get(1)), "String");
			if (flag3 && flag5) 
				rangeChecker.add(exp, (Expression) (mi.arguments().get(0)), "String");
		}
		
		
		MethodDeclaration md = methodDeclarations.get(mb);
		if (md != null && md.getBody() != null) {
			List<Integer> indexes = Helper.getRefArgIndexes(md);
			
			for (int index : indexes) {
				if (index < mi.arguments().size()) {
					Expression expArg = (Expression) (mi.arguments().get(index));
					nullPointerChecker.add(expArg.toString(), expArg);
				}
			}
		}

		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation smi) {
		IMethodBinding mb = smi.resolveMethodBinding();
		if (mb != null) {
			ElementReplacer ep = TemplatePreparation.createReplacerForSuperMethodInvocation(smi, mb, mp);
			elementReplacers.add(ep);
		}
		
		if (mb != null) {
			OverloadedMethodDetector omd = TemplatePreparation.detectOverloadedMethods(smi, mp);
			overloadedMethodDetectors.add(omd);
		}
		return true;
	}
	
	@Override
	public boolean visit(BooleanLiteral bl) {
		ElementReplacer ep = TemplatePreparation.createReplacerForBooleanLiteral(bl, mp);
		elementReplacers.add(ep);
		return true;
	}
	
	@Override
	public boolean visit(NumberLiteral nl) {
		ElementReplacer ep = TemplatePreparation.createReplacerForNumberLiteral(nl, mp);
		elementReplacers.add(ep);
		return true;
	}
	
		
	@Override
	public boolean visit(InfixExpression ie) {
		ElementReplacer ep = TemplatePreparation.createReplacerForInfixExpression(ie);
		elementReplacers.add(ep);
		
		if (ie.getOperator() == InfixExpression.Operator.DIVIDE) {
			Expression divisor = ie.getRightOperand();
			if (!(divisor instanceof NumberLiteral))
				divideZeroChecker.add(divisor);
		}
		
		return true;
	}
	
	@Override
	public boolean visit(Assignment as) {
		ElementReplacer ep = TemplatePreparation.createReplacerForAssignment(as);
		elementReplacers.add(ep);
		
		if (as.getOperator() == Assignment.Operator.DIVIDE_ASSIGN) {
			Expression divisor = as.getRightHandSide();
			if (!(divisor instanceof NumberLiteral))
				divideZeroChecker.add(divisor);
		}
		
		return true;
	}
	

	@Override
	public boolean visit(PrimitiveType pt) {
		ElementReplacer ep = TemplatePreparation.createReplacerForPrimitiveType(pt);
		elementReplacers.add(ep);
		return true;
	}
	
	@Override
	public boolean visit(CastExpression ce) {		
		ITypeBinding tb = ce.getType().resolveBinding();
		if (tb != null && (tb.isClass() || tb.isInterface() || tb.isArray()))
			castChecker.add(ce);
		return true;
	}
	
	@Override
	public boolean visit(PrefixExpression pre) {
		ElementReplacer ep = TemplatePreparation.createReplacerForPrefixExpression(pre);
		elementReplacers.add(ep);
		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression poe) {
		ElementReplacer ep = TemplatePreparation.createReplacerForPostfixExpression(poe);
		elementReplacers.add(ep);
		return true;
	}
	
	
	@Override
	public boolean visit(ConditionalExpression ce) {
		ElementReplacer ep = TemplatePreparation.createReplacerForConditionalExpression(ce);
		elementReplacers.add(ep);
		
		return true;
	}
	
	@Override
	public boolean visit(ClassInstanceCreation cic) {
		
		IMethodBinding mb = cic.resolveConstructorBinding();
		if (mb != null) {
			OverloadedMethodDetector omd = TemplatePreparation.detectOverloadedMethods(cic, mp);
			overloadedMethodDetectors.add(omd);
		}
		return true;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}
	
	
	@Override
	public boolean visit(ArrayAccess aa) {
		rangeChecker.add(aa.getArray(), aa.getIndex(), "Array");
		
		nullPointerChecker.add(aa.getArray().toString(), aa.getArray());
		return true;
	}
	
	
	public List<ElementReplacer> getElementReplacers() {
		return this.elementReplacers;
	}
	
	public NullPointerChecker getNullPointerChecker() {
		return this.nullPointerChecker;
	}
	
	public CastChecker getCastChecker() {
		return this.castChecker;
	}
	
	public RangeChecker getRangeChecker() {
		return this.rangeChecker;
	}
	
	public DivideZeroChecker getDivideZeroChecker() {
		return this.divideZeroChecker;
	}
	
	public List<OverloadedMethodDetector> getOverloadedMethodDetectors() {
		return this.overloadedMethodDetectors;
	}
}
