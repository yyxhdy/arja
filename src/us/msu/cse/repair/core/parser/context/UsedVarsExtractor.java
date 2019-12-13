package us.msu.cse.repair.core.parser.context;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.ModificationPoint;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.parser.VarMethodInfoExtractor;
import us.msu.cse.repair.core.util.Helper;
import us.msu.cse.repair.core.util.visitors.FieldVisitor;

public class UsedVarsExtractor {
	ModificationPoint mp;
	Map<IMethodBinding, MethodDeclaration> methodDeclarations;
	
	public UsedVarsExtractor(ModificationPoint mp,
			Map<IMethodBinding, MethodDeclaration> methodDeclarations) {
		this.mp = mp;
		this.methodDeclarations = methodDeclarations;
	}
		
	public Set<String> extract(ASTNode node) {
		VarMethodInfoExtractor extractor = new VarMethodInfoExtractor(node);
		extractor.extract();
		
		Map<String, VarInfo> thisVars =  extractor.getThisVars();
		Map<String, VarInfo> superVars = extractor.getSuperVars();
		Map<String, VarInfo> vars = extractor.getVars();
		
		Map<String, MethodInfo> thisMethods = extractor.getThisMethods();
		Map<String, MethodInfo> superMethods = extractor.getSuperMethods();
		Map<String, MethodInfo> methods = extractor.getMethods();
		
		Set<String> varsInUse_v = getVarsInUseForVars(thisVars, superVars, vars);
		Set<String> varsInUse_m = getVarsInUseForMethods(thisMethods, superMethods, methods);
		
	//	System.out.println("size: " + varsInUse_m.size());
		Set<String> varsInUse = new HashSet<String>();
		varsInUse.addAll(varsInUse_v);
		varsInUse.addAll(varsInUse_m);
		
		return varsInUse;
	}
	
	Set<String> getVarsInUseForMethods(Map<String, MethodInfo> thisMethods,
			Map<String, MethodInfo> superMethods, Map<String, MethodInfo> methods) {
		Statement statement = mp.getStatement();
		Map<String, MethodInfo> declaredMethods = mp.getDeclaredMethods();
		Map<String, MethodInfo> inheritedMethods = mp.getInheritedMethods();
		Map<String, MethodInfo> outerMethods = mp.getOuterMethods();
		
		ITypeBinding curType = Helper.getAbstractTypeDeclaration(statement).resolveBinding();
		
		Set<String> varsInUse = new HashSet<String>();
		
		for (String key : thisMethods.keySet()) {
			if (declaredMethods.containsKey(key)) {
				IMethodBinding mb = declaredMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
			else if (inheritedMethods.containsKey(key)) {
				IMethodBinding mb = inheritedMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
			
		}

		for (String key : superMethods.keySet()) {
			if (inheritedMethods.containsKey(key)) {
				IMethodBinding mb = inheritedMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
		}
		
		for (String key : methods.keySet()) {
			if (declaredMethods.containsKey(key)) {
				IMethodBinding mb = declaredMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
			else if (inheritedMethods.containsKey(key)) {
				IMethodBinding mb = inheritedMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
			else if (outerMethods.containsKey(key)){
				IMethodBinding mb = outerMethods.get(key).getMethodBinding(0);
				varsInUse.addAll(extract(mb, methodDeclarations, curType));
			}
		}
		
		return varsInUse;
	}
	
	Set<String> getVarsInUseForVars(Map<String, VarInfo> thisVars, 
			Map<String, VarInfo> superVars, Map<String, VarInfo> vars) {
		
		Map<String, VarInfo> declaredFields = mp.getDeclaredFields();
		Map<String, VarInfo> inheritedFields = mp.getInheritedFields();
		Map<String, VarInfo> outerFields = mp.getOuterFields();	
		Map<String, VarInfo> localVars = mp.getLocalVars();
		
		Set<String> varsInUse = new HashSet<String>();
		
		for (String v : thisVars.keySet()) {
			if (declaredFields.containsKey(v))
				varsInUse.add("this." + v);
			else if (inheritedFields.containsKey(v))
				varsInUse.add("super." + v);
		}
		
		for (String v : superVars.keySet())
			varsInUse.add("super." + v);
		
		for (String v : vars.keySet()) {
			if (localVars.containsKey(v))
				varsInUse.add("local." + v);
			else if (declaredFields.containsKey(v))
				varsInUse.add("this." + v);
			else if (inheritedFields.containsKey(v))
				varsInUse.add("super." + v);
			else if (outerFields.containsKey(v))
				varsInUse.add("outer." + v);
		}
		
		return varsInUse;
	}
	
	
	Set<String> extract(IMethodBinding mb, Map<IMethodBinding, MethodDeclaration> methodDeclarations,
			ITypeBinding curType) {
		List<IMethodBinding> visitedMethods = new ArrayList<IMethodBinding>();
		return extract(mb, methodDeclarations, curType, visitedMethods);
	}
	
	Set<String> extract(IMethodBinding mb, Map<IMethodBinding, MethodDeclaration> methodDeclarations,
			ITypeBinding curType, List<IMethodBinding> visitedMethods) {
		Set<String> output = new HashSet<String>();
		if (mb == null || methodDeclarations.get(mb) == null || visitedMethods.contains(mb))
			return output;
		
		visitedMethods.add(mb);

		MethodDeclaration md = methodDeclarations.get(mb);

		FieldVisitor fv = new FieldVisitor(md, curType);
		md.accept(fv);

		output.addAll(fv.getFields());
		for (IMethodBinding bd : fv.getMethodBindings()) {
			output.addAll(extract(bd, methodDeclarations, curType, visitedMethods));
		}

		return output;
	}
}
