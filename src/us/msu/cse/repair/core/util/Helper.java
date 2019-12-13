package us.msu.cse.repair.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
//import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.eclipse.jdt.core.dom.WhileStatement;

import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.SeedStatement;
import us.msu.cse.repair.core.parser.VarInfo;
import us.msu.cse.repair.core.util.visitors.AllSimpleRefASTVisitor;
import us.msu.cse.repair.core.util.visitors.StatementASTVisitor;

public class Helper {			
	public static AbstractTypeDeclaration getAbstractTypeDeclaration(Statement s) {
		ASTNode node = s;
		while (!(node instanceof AbstractTypeDeclaration)) {
			node = node.getParent();
		}
		return (AbstractTypeDeclaration) node;
	}

	public static MethodDeclaration getMethodDeclaration(Statement s) {
		ASTNode node = s;
		while (node != null && !(node instanceof MethodDeclaration) && !(node instanceof Initializer)) {
			node = node.getParent();
		}
		if (node instanceof MethodDeclaration)
			return (MethodDeclaration) node;
		else
			return null;
	}

	public static List<VariableDeclarationFragment> getVariableDeclarationFragments(VariableDeclarationStatement vs) {
		List<VariableDeclarationFragment> fragments = new ArrayList<VariableDeclarationFragment>();
		Iterator<?> iter = vs.fragments().iterator();
		while (iter.hasNext()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
			fragments.add(fragment);

		}
		return fragments;
	}

	public static List<VariableDeclarationFragment> getVariableDeclarationFragments(VariableDeclarationExpression vde) {
		List<VariableDeclarationFragment> fragments = new ArrayList<VariableDeclarationFragment>();
		Iterator<?> iter = vde.fragments().iterator();
		while (iter.hasNext()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
			fragments.add(fragment);
		}
		return fragments;
	}

	public static Map<String, VarInfo> getVarInfos(IVariableBinding[] vbs) {
		Map<String, VarInfo> map = new HashMap<String, VarInfo>();

		for (IVariableBinding vb : vbs) {
			VarInfo vi = getVarInfo(vb);
			map.put(vb.getName(), vi);
		}

		return map;
	}

	public static Map<String, VarInfo> getVarInfos(Field[] fields) {
		Map<String, VarInfo> map = new HashMap<String, VarInfo>();

		for (Field field : fields) {
			String name = field.getName();
			if (name.startsWith("this"))
				continue;

			VarInfo vi = getVarInfo(field);
			map.put(name, vi);
		}

		return map;
	}

	public static Map<String, MethodInfo> getMethodInfos(Method[] methods) {
		Map<String, MethodInfo> map = new HashMap<String, MethodInfo>();

		for (Method method : methods) {
			String key = getMethodKey(method);
			MethodInfo mi = getMethodInfo(method);
			
			if (!map.containsKey(key))
				map.put(key, mi);
			else
				map.get(key).add(mi);
		}
		return map;
	}

	public static Map<String, MethodInfo> getMethodInfos(IMethodBinding[] mbs) {
		HashMap<String, MethodInfo> map = new HashMap<String, MethodInfo>();

		for (IMethodBinding mb : mbs) {
			if (mb.isConstructor())
				continue;
			String key = getMethodKey(mb);
			MethodInfo mi = getMethodInfo(mb);
			
			if (!map.containsKey(key))
				map.put(key, mi);
			else
				map.get(key).add(mi);
		}

		return map;
	}

	public static VarInfo getVarInfo(IVariableBinding vb) {
		ITypeBinding typeBinding = vb.getVariableDeclaration().getType();
		String typeName = typeBinding.getQualifiedName();
		int mod = vb.getModifiers();
	//	VarInfo vi = new VarInfo(typeName, vb, mod);  //org
		VarInfo vi = new VarInfo(typeName, vb.getVariableDeclaration(), mod);
		return vi;
	}

	public static VarInfo getVarInfo(Field field) {
		String typeName = getGenericTypeNameForField(field.toGenericString());
		int mod = field.getModifiers();

		VarInfo vi = new VarInfo(typeName, null, mod);
		return vi;
	}

	static String getGenericTypeNameForField(String genericString) {
		String strs[] = genericString.split("\\s+");
		return strs[strs.length - 2];
	}

	public static MethodInfo getMethodInfo(IMethodBinding mb) {
		int mod = mb.getModifiers();
		ITypeBinding returnTypeBinding = mb.getMethodDeclaration().getReturnType();
		String returnTypeName = returnTypeBinding.getQualifiedName();
		
		ITypeBinding[] parameterTypes = mb.getMethodDeclaration().getParameterTypes();
		String parameterTypeNames = "";	
		if (parameterTypes.length > 0) {
			parameterTypeNames = parameterTypes[0].getQualifiedName();
			for (int i = 1; i < parameterTypes.length; i++) {
				ITypeBinding tb = parameterTypes[i];
				parameterTypeNames += (":" + tb.getQualifiedName());
			}	
		}
			
		List<IMethodBinding> methodBindingList = new ArrayList<IMethodBinding>();
		List<String> returnTypeNameList = new ArrayList<String>();
		List<Integer> modList = new ArrayList<Integer>();
		List<String> parameterTypeNamesList = new ArrayList<String>();
		
		methodBindingList.add(mb);
		returnTypeNameList.add(returnTypeName);
		modList.add(mod);
		parameterTypeNamesList.add(parameterTypeNames);
		
		MethodInfo mi = new MethodInfo(mb.getMethodDeclaration().getName(), parameterTypes.length, methodBindingList, returnTypeNameList,
			 parameterTypeNamesList, modList);
		
		return mi;
	}

	public static String getMethodKey(IMethodBinding mb) {
		String key = mb.getName();
		int len = mb.getMethodDeclaration().getParameterTypes().length;
		key += (":" + len);
		return key;
	}

	public static MethodInfo getMethodInfo(Method method) {
		int mod = method.getModifiers();
		String returnTypeName = getGenericReturnTypeNameForMethod(method.toGenericString());
		String parameterTypeNames = "";
		int params = method.getGenericParameterTypes().length;
		if (params > 0) 
			parameterTypeNames += getGenericParameterTypeNamesForMethod(method.toGenericString());
		
		List<IMethodBinding> methodBindingList = new ArrayList<IMethodBinding>();
		List<String> returnTypeNameList = new ArrayList<String>();
		List<Integer> modList = new ArrayList<Integer>();
		List<String> parameterTypeNamesList = new ArrayList<String>();
		
		methodBindingList.add(null);
		returnTypeNameList.add(returnTypeName);
		modList.add(mod);
		parameterTypeNamesList.add(parameterTypeNames);
		
		MethodInfo mi = new MethodInfo(method.getName(), params, methodBindingList, returnTypeNameList,
				 parameterTypeNamesList, modList);
		
		return mi;
	}

	public static String getMethodKey(Method method) {
		String key = method.getName();
		int len = method.getGenericParameterTypes().length;
		key += (":" + len);
		return key;
	}

	static String getGenericReturnTypeNameForMethod(String genericString) {
		String strs[] = genericString.trim().split("\\s+");
		for (int i = 0; i < strs.length; i++) {
			if (strs[i].indexOf("(") != -1)
				return strs[i - 1];
		}
		return null;
	}

	static String getGenericParameterTypeNamesForMethod(String genericString) {
		int beginIndex = genericString.indexOf("(");
		int endIndex = genericString.indexOf(")");
		return genericString.substring(beginIndex + 1, endIndex).trim().replace(",", ":");
	}

	public static String getMethodName(String key) {
		int index = key.indexOf(":");
		if (index == -1)
			return key;
		else
			return key.substring(0, index);
	}

	public static boolean isInStaticMethod(Statement statement) {
		MethodDeclaration md = getMethodDeclaration(statement);
		if (md == null)
			return false;
		else {
			int mod = md.getModifiers();
			return Modifier.isStatic(mod);
		}
	}

	public static boolean isStaticMethod(MethodInfo mi) {
		for (int i = 0; i < mi.getSize(); i++) {
			int mod = mi.getModifiers(i);
			if (Modifier.isStatic(mod))
				return true;
		}
		return false;
	}

	public static boolean isPublicMethod(MethodInfo mi) {
		for (int i = 0; i < mi.getSize(); i++) {
			int mod = mi.getModifiers(i);
			if (Modifier.isPublic(mod))
				return true;
		}
		return false;
	}

	public static boolean isProtectedMethod(MethodInfo mi) {
		for (int i = 0; i < mi.getSize(); i++) {
			int mod = mi.getModifiers(i);
			if (Modifier.isProtected(mod))
				return true;
		}
		return false;
	}

	public static boolean isPackagePrivateMethod(MethodInfo mi) {
		for (int i = 0; i < mi.getSize(); i++) {
			int mod = mi.getModifiers(i);
			if (isPackagePrivate(mod))
				return true;
		}
		return false;
	}

	public static boolean isStaticVar(VarInfo vi) {
		int mod = vi.getModifiers();
		return Modifier.isStatic(mod);
	}

	public static boolean isPublicVar(VarInfo vi) {
		int mod = vi.getModifiers();
		return Modifier.isPublic(mod);
	}

	public static boolean isProtectedVar(VarInfo vi) {
		int mod = vi.getModifiers();
		return Modifier.isProtected(mod);
	}

	public static boolean isPackagePrivateVar(VarInfo vi) {
		int mod = vi.getModifiers();	
		return isPackagePrivate(mod);
	}
	
	public static boolean isPackagePrivate(int mod) {
		boolean isPublic = Modifier.isPublic(mod);
		boolean isPrivate = Modifier.isPrivate(mod);
		boolean isProtected = Modifier.isProtected(mod);
		return !(isPublic || isPrivate || isProtected);
	}

	public static boolean isDeclaredVar(SimpleName s) {
		StructuralPropertyDescriptor property1 = VariableDeclarationFragment.NAME_PROPERTY;
		StructuralPropertyDescriptor property2 = SingleVariableDeclaration.NAME_PROPERTY;

		StructuralPropertyDescriptor spd = s.getLocationInParent();
		return spd == property1 || spd == property2;
	}

	public static boolean isInSamePackage(String className1, String className2) {
		int index1 = className1.lastIndexOf(".");
		int index2 = className2.lastIndexOf(".");

		String pk1 = className1.substring(0, index1);
		String pk2 = className2.substring(0, index2);

		return pk1.equals(pk2);
	}
	
	public static boolean isInSamePackage2(String sourceFilePath1, String sourceFilePath2) {
		int index1 = sourceFilePath1.lastIndexOf(File.separator);
		int index2 = sourceFilePath2.lastIndexOf(File.separator);
		
		String pk1 = sourceFilePath1.substring(0, index1);
		String pk2 = sourceFilePath2.substring(0, index2);
		return pk1.equals(pk2);
	}
	
	public static boolean isInSamePackage(ITypeBinding tb, Statement statement) {
		AbstractTypeDeclaration td = Helper.getAbstractTypeDeclaration(statement);
		ITypeBinding typeBinding = td.resolveBinding();
		
		if (!tb.isArray() && tb != null && typeBinding != null) {
//			System.out.println(tb.getBinaryName());
//			System.out.println(typeBinding.getBinaryName());
			return isInSamePackage(tb.getBinaryName(), typeBinding.getBinaryName());
		}
		else
			return false;
	}

	public static boolean isRuntimeException(ITypeBinding tb) {
		while (tb != null) {
			if (tb.getName().equals("RuntimeException"))
				return true;
			tb = tb.getSuperclass();
		}
		return false;
	}
	
	
	public static boolean isException(ITypeBinding tb) {
		while (tb != null) {
			if (tb.getQualifiedName().equals("java.lang.Exception"))
				return true;
			tb = tb.getSuperclass();
		}
		return false;
	}

	public static URL[] getURLs(Collection<String> paths) throws MalformedURLException {
		URL[] urls = new URL[paths.size()];
		int i = 0;
		for (String path : paths) {
			File file = new File(path);
			URL url = file.toURI().toURL();
			urls[i++] = url;
		}

		return urls;
	}

	public static InputStream getInputStreamOfClass(String parent, String binJavaClass) throws FileNotFoundException {
		String child = binJavaClass.replace(".", File.separator) + ".class";
		File file = new File(parent, child);
		InputStream is = new FileInputStream(file);
		return is;
	}

	public static void saveBinaryClassToFile(byte[] bytes, String fullClassName, String dir) throws IOException {
		String name = fullClassName.replace(".", File.separator);
		File file = new File(dir, name);
		FileUtils.writeByteArrayToFile(file, bytes);
	}

	public static String getTestNameFromJUnitOutput(String line) {
		if (line.indexOf(")") == -1)
			return null;

		String[] strArray = line.split("\\)");

		if (!isInteger(strArray[0]))
			return null;

		String names[] = strArray[1].trim().split("\\(");

		return names[1] + "#" + names[0];

	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	public static boolean isInLoop(Statement statement) {
		ASTNode node = statement;
		while (!(node.getParent() instanceof MethodDeclaration || node.getParent() instanceof Initializer)) {
			ASTNode parent = node.getParent();

			boolean flag1 = parent instanceof ForStatement;
			boolean flag2 = parent instanceof EnhancedForStatement;
			boolean flag3 = parent instanceof WhileStatement;
			boolean flag4 = parent instanceof DoStatement;

			if (flag1 || flag2 || flag3 || flag4)
				return true;

			node = node.getParent();
		}

		return false;
	}

	public static boolean isInSwitch(Statement statement) {
		ASTNode node = statement;
		while (!(node.getParent() instanceof MethodDeclaration || node.getParent() instanceof Initializer)) {
			ASTNode parent = node.getParent();

			boolean flag = parent instanceof SwitchStatement;
			if (flag)
				return true;

			node = node.getParent();
		}

		return false;
	}

	public static boolean isAlternativeVariableDeclaration(VariableDeclarationStatement current,
			VariableDeclarationStatement replace) {
		ITypeBinding curTypeBinding = current.getType().resolveBinding();
		ITypeBinding repTypeBinding = replace.getType().resolveBinding();

		if (curTypeBinding != null && repTypeBinding != null) {
			if (!repTypeBinding.isCastCompatible(curTypeBinding))
				return false;
		} else {
			Type curType = current.getType();
			Type repType = replace.getType();

			if (!curType.subtreeMatch(new ASTMatcher(true), repType))
				return false;
		}

		if (current.fragments().size() != replace.fragments().size())
			return false;

		Set<String> curS = new HashSet<String>();
		Set<String> repS = new HashSet<String>();
		for (int i = 0; i < current.fragments().size(); i++) {
			String curID = ((VariableDeclarationFragment) current.fragments().get(i)).getName().getIdentifier();
			String repID = ((VariableDeclarationFragment) replace.fragments().get(i)).getName().getIdentifier();
			curS.add(curID);
			repS.add(repID);
		}

		return curS.equals(repS);
	}

	public static boolean isLastStatementInMethod(Statement statement) {
		ASTNode node = statement;
		while (!(node.getParent() instanceof MethodDeclaration)) {
			if (node.getParent() instanceof Block) {
				Block block = (Block) node.getParent();
				int index = block.statements().indexOf(node);
				if (index != block.statements().size() - 1)
					return false;
			}

			node = node.getParent();
			if (node == null)
				return false;
		}
		return true;
	}

	public static boolean isLastStatementInBlock(Statement statement) {
		if (!(statement.getParent() instanceof Block))
			return true;

		Block block = (Block) statement.getParent();

		int index = block.statements().indexOf(statement);

		return (index == block.statements().size() - 1) ? true : false;
	}

	public static boolean canReturnOrThrow(Statement statement) {
		if (statement instanceof ReturnStatement || statement instanceof ThrowStatement)
			return true;

		if (statement instanceof IfStatement) {
			IfStatement ifs = (IfStatement) statement;
			if (ifs.getElseStatement() == null)
				return false;

			Statement thenS = ifs.getThenStatement();
			Statement elseS = ifs.getElseStatement();

			if (thenS instanceof Block) {
				List<?> sList = ((Block) thenS).statements();
				if (sList.isEmpty())
					return false;
				thenS = (Statement) sList.get(sList.size() - 1);
			}
			if (elseS instanceof Block) {
				List<?> sList = ((Block) elseS).statements();
				if (sList.isEmpty())
					return false;
				elseS = (Statement) sList.get(sList.size() - 1);
			}

			if (canReturnOrThrow(thenS) && canReturnOrThrow(elseS))
				return true;
			else
				return false;

		} else if (statement instanceof TryStatement) {
			TryStatement trs = (TryStatement) statement;
			if (trs.getFinally() != null) {
				List<?> sList = trs.getFinally().statements();
				if (!sList.isEmpty()) {
					Statement finalS = (Statement) sList.get(sList.size() - 1);
					if (canReturnOrThrow(finalS))
						return true;
				}
			}

			List<Block> tcBlocks = new ArrayList<Block>();
			tcBlocks.add(trs.getBody());
			for (Object o : trs.catchClauses()) {
				CatchClause cc = (CatchClause) o;
				tcBlocks.add(cc.getBody());
			}

			for (Block bl : tcBlocks) {
				List<?> sList = bl.statements();
				if (sList.isEmpty())
					return false;

				Statement st = (Statement) sList.get(sList.size() - 1);
				if (!canReturnOrThrow(st))
					return false;
			}

			return true;
		} else
			return false;
	}

	public static boolean isAbstractClass(Class<?> target) {
		int mod = target.getModifiers();
		boolean isAbstract = Modifier.isAbstract(mod);
		boolean isInterface = Modifier.isInterface(mod);

		if (isAbstract || isInterface)
			return true;

		return false;
	}

	public static boolean isAssignment(Statement statement) {
		if (!(statement instanceof ExpressionStatement))
			return false;

		Expression expression = ((ExpressionStatement) statement).getExpression();
		return (expression instanceof Assignment);
	}

	public static boolean isSameParentType(ITypeBinding tb1, ITypeBinding tb2) {
		ITypeBinding tb1_sc = tb1.getSuperclass();
		ITypeBinding tb2_sc = tb2.getSuperclass();

		if (tb1_sc == tb2_sc && tb1_sc != null && !tb1_sc.getQualifiedName().startsWith("java.lang")) {
			return true;
		}

		ITypeBinding[] tb1_its = tb1.getInterfaces();
		ITypeBinding[] tb2_its = tb2.getInterfaces();

		for (int i = 0; i < tb1_its.length; i++) {
			for (int j = 0; j < tb2_its.length; j++) {
				if (tb1_its[i] == tb2_its[j] && !tb1_its[i].getQualifiedName().startsWith("java.lang"))
					return true;
			}
		}

		return false;
	}

	public static Set<String> getVisibleTypeDeclarations(Statement statement) {
		Set<String> typeDecls = new HashSet<String>();

		CompilationUnit cu = (CompilationUnit) statement.getRoot();
		for (Object obj : cu.imports()) {
			ImportDeclaration ide = (ImportDeclaration) obj;
			IBinding ib = ide.resolveBinding();
			if (ib == null)
				continue;

			if (ib instanceof ITypeBinding) {
				ITypeBinding tb = (ITypeBinding) ib;
				typeDecls.add(tb.getBinaryName());
			} else if (ib instanceof IPackageBinding) {
				IPackageBinding pb = (IPackageBinding) ib;
				typeDecls.add(pb.getName());
			}
		}

		typeDecls.add(cu.getPackage().getName().toString());
		typeDecls.add("java.lang");
		return typeDecls;
	}
	
    private static String characterTable[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
    		"j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
    		"z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    public static String getRandomID() {
    	int count = 4;
    	String id = "";
    	for (int i = 0; i < count; i++) {
    		int index = PseudoRandom.randInt(0, characterTable.length - 1);
    		id = (id + characterTable[index]);
    	}
    	return id;
    }
    
    public static String convert2Double(String token) {
    	char ch = token.charAt(token.length() - 1);
    	if (Character.isLetter(ch))
    		return token.substring(0, token.length() - 1) + "D";
    	else
    		return token + "D";
    }
    
    
    // e.g., A or org.math.A
    public static boolean isClassName(ASTNode node) {
    	if (!(node instanceof SimpleName || node instanceof QualifiedName))
    		return false;
    	
    	Name sn = (Name) node;
    	
    	IBinding binding = sn.resolveBinding();
    	ITypeBinding tb = sn.resolveTypeBinding();
    	
    	boolean flag1 = (binding != null);
    	boolean flag2 = (binding instanceof IVariableBinding);
    	boolean flag3 = (tb != null);
    	
    	if (flag1 && !flag2 && flag3)
    		return true;
    	
    	return false;		
    }
    
    
    public static boolean isReplaceableMethod(ITypeBinding[] curParams, 
    		ITypeBinding curReturn, boolean isConstructor, IMethodBinding rep) {
    	ITypeBinding[] repParams = rep.getParameterTypes();
    	
    	if (curParams.length != repParams.length)
    		return false;
    	
    	if (isConstructor && !rep.isConstructor())
    		return false;
    	if (!isConstructor && rep.isConstructor())
    		return false;
    	
    	ITypeBinding repReturn = rep.getReturnType();
    	
    	if (!repReturn.isAssignmentCompatible(curReturn))
    		return false;
    	
    	for (int i = 0; i < curParams.length; i++) {
    		if (!curParams[i].isAssignmentCompatible(repParams[i]))
    			return false;
    	}
    	
    	return true;
    }
    
    
    // is  this.a.b or not  ? a can be array access, currently no
    public static boolean isSpecialFieldAccess(FieldAccess fa) {
    	Expression exp = fa.getExpression();
    	
    	if (!(exp instanceof FieldAccess))
    		return false;
    	
    	FieldAccess nfa = (FieldAccess) exp; 	
    	Expression nexp = nfa.getExpression();
    	
    	return nexp instanceof ThisExpression;
    }
    
    
    public static boolean isInParameter(ASTNode node) {
    	while (!(node instanceof Statement)) {
    		if (node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY)
    			return true;
    		node = node.getParent();
    	}
		return false;
    }
    
    public static InstanceofExpression getInstanceofExpression(CastExpression ce) {
    	InstanceofExpression ie = ce.getAST().newInstanceofExpression();
    	Expression expCopy = (Expression) ASTNode.copySubtree(ce.getAST(), ce.getExpression());
    	Type typeCopy = (Type) ASTNode.copySubtree(ce.getAST(), ce.getType());
    	ie.setLeftOperand(expCopy);
    	ie.setRightOperand(typeCopy);
    	return ie;
    }
    
    public static InfixExpression getIfNotZeroExpression(Expression exp) {
    	InfixExpression ie = exp.getAST().newInfixExpression();
    	Expression expCopy = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
    	
    	NumberLiteral nl = exp.getAST().newNumberLiteral("0");
    	
    	ie.setLeftOperand(expCopy);
    	ie.setOperator(InfixExpression.Operator.NOT_EQUALS);
    	ie.setRightOperand(nl);
    	
    	return ie;
    }
    
    public static InfixExpression getIfNotNullExpression(Expression exp) {
    	InfixExpression ie = exp.getAST().newInfixExpression();
    	Expression expCopy = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
    	
    	NullLiteral nl = exp.getAST().newNullLiteral();
    	
    	ie.setLeftOperand(expCopy);
    	ie.setOperator(InfixExpression.Operator.NOT_EQUALS);
    	ie.setRightOperand(nl);
    	
    	return ie;
    }
    
   
       
    public static IfStatement getIfThrowStatement(Expression exp, String exceptionQualifiedName) {
    	IfStatement ifStatement = exp.getAST().newIfStatement();
    	
    	Expression expCopy = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
    	
    	ifStatement.setExpression(expCopy);
    	
    	ThrowStatement ths = exp.getAST().newThrowStatement();
    	
    	ClassInstanceCreation ccc = exp.getAST().newClassInstanceCreation();
    	
    	ASTParser parser = ASTParser.newParser(AST.JLS8);
    	
    	parser.setSource(exceptionQualifiedName.toCharArray());
    	parser.setKind(ASTParser.K_EXPRESSION);
    	QualifiedName qn = (QualifiedName) parser.createAST(null);
    	 	
    	QualifiedName qnCopy = (QualifiedName) ASTNode.copySubtree(exp.getAST(), qn);
    	
    	Type type = exp.getAST().newSimpleType(qnCopy);
    	ccc.setType(type);
    
    	ths.setExpression(ccc);
    	
    	ifStatement.setThenStatement(ths);
    	
    	return ifStatement;
    }
    
    
    public static IfStatement getIfStatementForNullChecker(Expression exp, Statement statement) {
    	IfStatement ifs = exp.getAST().newIfStatement();
    	Expression expCopy = (Expression) ASTNode.copySubtree(exp.getAST(), exp);
    	Statement statementCopy = (Statement) ASTNode.copySubtree(statement.getAST(), statement);
    	
    	ifs.setExpression(expCopy);
    	ifs.setThenStatement(statementCopy);
    	return ifs;
    }
    
    public static ThrowStatement getThrowStatement(AST ast, String exceptionQualifiedName) {
    	ThrowStatement ths = ast.newThrowStatement();
    	
    	ClassInstanceCreation ccc = ast.newClassInstanceCreation();
    	
    	ASTParser parser = ASTParser.newParser(AST.JLS8);
    	parser.setSource(exceptionQualifiedName.toCharArray());
    	parser.setKind(ASTParser.K_EXPRESSION);
    	QualifiedName qn = (QualifiedName) parser.createAST(null);
    	QualifiedName qnCopy = (QualifiedName) ASTNode.copySubtree(ast, qn);
    	
    	Type type = ast.newSimpleType(qnCopy);
    	ccc.setType(type);
    
    	ths.setExpression(ccc);
    	
    	return ths;
    }
    
    
    public static List<Statement> getThrowStatementsFromImportThrow(CompilationUnit cu) {
    	List<Statement> throwStatements = new ArrayList<Statement>();
    	for (Object o : cu.imports()) {
    		ImportDeclaration imd = (ImportDeclaration) o;
    		IBinding bd = imd.resolveBinding();
    		if (bd.getKind() != IBinding.TYPE)
    			continue;
    		
    		ITypeBinding tb = (ITypeBinding) bd;
    		
    		if (isException(tb)) {
    			ThrowStatement ths = getThrowStatement(cu.getAST(), tb.getQualifiedName());
    			throwStatements.add(ths);
    		}
    		
    	}
    	return throwStatements;
    }
    
	public static List<Statement> getThrowStatementsFromMethodThrow(MethodDeclaration md) {
		List<Statement> throwStatements = new ArrayList<Statement>();
		for (Object obj : md.thrownExceptionTypes()) {
			Type type = (Type) obj;
			ITypeBinding tb = type.resolveBinding();
			if (tb != null) {
				String expName = tb.getQualifiedName();
				ThrowStatement ths = getThrowStatement(md.getAST(), expName);
				throwStatements.add(ths);
			}
		}
		
		return throwStatements;
	}
   
    public static int[][] getPermutations(int n) {
    	if (n == 1) {
    		int[][] res = new int[1][];
    		res[0] = new int[1];
    		res[0][0] = 0;
    		return res;
    	}
    	int size = 1;
    	for (int i = 2; i <= n; i++) 
    		size *= i;
    	int[][] curPerms = new int[size][n];
    	
    	int[][] prePerms = getPermutations(n - 1);
    	
    	for (int i = 0; i < prePerms.length; i++) {
    		for (int k = 0; k < n; k++) {
    			int prePointer = 0;
    			int curPointer = 0;
    			
    			while (curPointer < n) {
    				if (curPointer == k) 
    					curPerms[i * n + k][curPointer++] = n - 1;
    				else 
    					curPerms[i * n + k][curPointer++] = prePerms[i][prePointer++];
    			}
    		}
    	}
    	
    	return curPerms;
    }
    
    
    @SuppressWarnings("rawtypes")
	public static List getArguments(Expression exp) {
    	if (exp instanceof MethodInvocation)
    		return ((MethodInvocation) exp).arguments();
    	else if (exp instanceof SuperMethodInvocation)
    		return ((SuperMethodInvocation) exp).arguments();
    	else if (exp instanceof ClassInstanceCreation)
    		return ((ClassInstanceCreation) exp).arguments();
    	else 
    		return null;
    }
    
    @SuppressWarnings("rawtypes")
	public static int getNumberOfArguments(Expression exp) {
    	List arguments = getArguments(exp);
    	if (arguments != null)
    		return arguments.size();
    	return -1;
    }
    
    public static boolean isBooleanType(ITypeBinding tb) {
		if (tb != null) {
			String qName = tb.getQualifiedName();
			if (qName.equals("boolean") || qName.equals("java.lang.Boolean")) 
				return true;
		}
		return false;
	}
    
	public static boolean isAndOrOrInfixExpression(Expression exp) {
		boolean flag1 = (exp instanceof InfixExpression);

		if (!flag1)
			return false;

		InfixExpression ie = (InfixExpression) exp;

		InfixExpression.Operator op = ie.getOperator();

		if (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR)
			return true;
		else
			return false;
	}
	
	
	public static InfixExpression getRangeCheckExpression(Expression node, Expression index, String type) {
		InfixExpression ie = node.getAST().newInfixExpression();
		
		Expression indexCopy = (Expression) ASTNode.copySubtree(index.getAST(), index);
		ie.setLeftOperand(indexCopy);
	
		
		if (type.equals("Array")) {
			boolean flag = (node instanceof SimpleName || node instanceof QualifiedName);
		
			Expression nodeCopy = (Expression) ASTNode.copySubtree(node.getAST(), node);
			SimpleName sn = node.getAST().newSimpleName("length");
			
			Expression right;	
			if (flag) {
				QualifiedName qName = node.getAST().newQualifiedName((Name) nodeCopy, sn);
				right = qName;
			}
		
			else {
				FieldAccess fa = node.getAST().newFieldAccess();
				fa.setExpression(nodeCopy);
				fa.setName(sn);
				right = fa;
			}
			
			ie.setOperator(InfixExpression.Operator.LESS);
			ie.setRightOperand(right);		
		}
		else if (type.equals("List")) {
			MethodInvocation mi = node.getAST().newMethodInvocation();
			Expression nodeCopy = (Expression) ASTNode.copySubtree(node.getAST(), node);
			mi.setExpression(nodeCopy);
			
			SimpleName sn = node.getAST().newSimpleName("size");
			mi.setName(sn);
			
			ie.setOperator(InfixExpression.Operator.LESS);
			ie.setRightOperand(mi);
		}
		else {
			MethodInvocation mi = node.getAST().newMethodInvocation();
			Expression nodeCopy = (Expression) ASTNode.copySubtree(node.getAST(), node);
			mi.setExpression(nodeCopy);
			
			SimpleName sn = node.getAST().newSimpleName("length");
			mi.setName(sn);
			
			String methodName = ((MethodInvocation) node.getParent()).getName().getIdentifier();
			
			if (methodName.equals("substring"))
				ie.setOperator(InfixExpression.Operator.LESS_EQUALS);
			else
				ie.setOperator(InfixExpression.Operator.LESS);
			
			ie.setRightOperand(mi);
		}
		
		return ie;
	}
	

	
	public static boolean isInForExpression(ASTNode node) {
		while (!(node instanceof Statement)) {
			if (node.getLocationInParent() == ForStatement.EXPRESSION_PROPERTY)
				return true;
			node = node.getParent();
		}
		return false;
	}
	
	public static boolean isForInitializersVar(SimpleName sn) {
		ASTNode node = sn;
		
		while (!(node instanceof Statement)) {
			if (node.getLocationInParent() == ForStatement.EXPRESSION_PROPERTY)
				break;
			node = node.getParent();
		}
		
		if (node instanceof Statement)
			return false;
		else {
			ForStatement fs = (ForStatement) node.getParent();
			List<String> initializers = new ArrayList<String>();
			
			for (Object o : fs.initializers()) {
				if (o instanceof Assignment) {
					Assignment as = (Assignment) o;
					if (as.getLeftHandSide() instanceof SimpleName) {
						SimpleName left = (SimpleName) (as.getLeftHandSide());
						initializers.add(left.getIdentifier());
					}
				}
				else if (o instanceof VariableDeclarationExpression) {
					VariableDeclarationExpression vde = (VariableDeclarationExpression) o;
					for (Object obj : vde.fragments()) {
						VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
						initializers.add(vdf.getName().getIdentifier())	;
					}
				}
			}
			
			if (initializers.contains(sn.getIdentifier()))
				return true;
		}
		
		return false;
	}
	
	
	public static List<Statement> getStatementListWithoutDuplicates(List<Statement> orgList) {
		Set<SeedStatement> set = new HashSet<SeedStatement>();
		List<Statement> newList = new ArrayList<Statement>();
		for (Statement statement : orgList) {
			SeedStatement sst = new SeedStatement(statement);
			if (set.add(sst)) 
				newList.add(statement);
		}
		
		return newList;
	}
	
	public static void blocklizeSource(String path) throws IOException {
		File file = new File(path);
		String content = FileUtils.readFileToString(file, "UTF-8");

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(content.toCharArray());

		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		StatementASTVisitor saVisitor = new StatementASTVisitor();
		unit.accept(saVisitor);

		List<Statement> statements = saVisitor.getStatements();

		if (statements.isEmpty())
			return;
		
		List<Brace> braceList = new ArrayList<Brace>();
		for (Statement st : statements) {
			Brace brace1 = new Brace('{', st.getStartPosition());
			Brace brace2 = new Brace('}', st.getStartPosition() + st.getLength());
			braceList.add(brace1);
			braceList.add(brace2);
		}

		Collections.sort(braceList);

		String finalSt = "";
		int i = 0;
		for (i = 0; i < braceList.size(); i++) {
			int start = 0;
			if (i > 0)
				start = braceList.get(i - 1).getPosition();
			int end = braceList.get(i).getPosition();

			finalSt += content.substring(start, end);
			finalSt += braceList.get(i).getMark();
		}

		finalSt += content.substring(braceList.get(i - 1).getPosition());
		FileUtils.write(file, finalSt, "UTF-8");
	}
	
	
	// tb2 is the superclass
	public static boolean isSuperClass(ITypeBinding tb1, ITypeBinding tb2) {
		while (tb1 != null) {
			tb1 = tb1.getSuperclass();
			if (tb1 == tb2)
				return true;
		}
		return false;
	}
	
	
	public static Set<IVariableBinding> getVariables(ASTNode node) {
		final Set<IVariableBinding> varBindings = new HashSet<IVariableBinding>();
		
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName sn) {
				IBinding binding = sn.resolveBinding();
				if (binding != null && binding instanceof IVariableBinding) {
					IVariableBinding vb = (IVariableBinding) binding;
					varBindings.add(vb.getVariableDeclaration());
				}
				return true;
			}
		});
		
		return varBindings;
	}
	
	
	public static boolean isOneVarContained(Set<IVariableBinding> set1,
			Set<IVariableBinding> set2) {
		for (IVariableBinding vb : set1) {
			if (set2.contains(vb))
				return true;
		}
		return false;
	}
	
	
	public static Set<IVariableBinding> getDeclaredVariables(VariableDeclarationStatement vds) {
		Set<IVariableBinding> varBindings = new HashSet<IVariableBinding>();
		
		for (Object o : vds.fragments()) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
			IVariableBinding vb = vdf.resolveBinding();
			if (vb != null)
				varBindings.add(vb.getVariableDeclaration());
		}
		
		return varBindings;
	}
	
	public static Set<IVariableBinding> getAssignedVariables(Assignment as) {
		Set<IVariableBinding> varBindings = new HashSet<IVariableBinding>();
		
		while (true) {
			boolean flag = as.getLeftHandSide() instanceof SimpleName;
			if (flag) {
				SimpleName sn = (SimpleName) as.getLeftHandSide();
				IBinding binding = sn.resolveBinding();
				if (binding instanceof IVariableBinding) {
					IVariableBinding vb = (IVariableBinding) binding;
					varBindings.add(vb.getVariableDeclaration());
				}
				
				if (as.getRightHandSide() instanceof Assignment)
					as = (Assignment) as.getRightHandSide();
				else
					break;
			}
			else
				break;
		}
		
		return varBindings;
	}
	
	public static Statement getEnclosingStatement(Statement statement) {
		ASTNode node = statement;

		while (!(node instanceof MethodDeclaration) && !(node instanceof Initializer)) {
			ASTNode parent = node.getParent();

			if (parent instanceof Block || parent instanceof SwitchStatement) {
				return (Statement) node;
			}
			node = parent;
		}
		return null;
	}
	
	public static Expression getExpression(Statement statement) {
		if (statement instanceof IfStatement) 
			return ((IfStatement) statement).getExpression();
		else if (statement instanceof ForStatement)
			return ((ForStatement) statement).getExpression();
		else if (statement instanceof EnhancedForStatement)
			return ((EnhancedForStatement) statement).getExpression();
		else if (statement instanceof WhileStatement)
			return ((WhileStatement) statement).getExpression();
		else if (statement instanceof DoStatement) 
			return ((DoStatement) statement).getExpression();
		else
			return null;
	}
	
	public static boolean isNoVarStatement(Statement statement) {
		boolean flag1 = statement instanceof BreakStatement;
		boolean flag2 = statement instanceof ContinueStatement;
		boolean flag3 = statement instanceof EmptyStatement;
		boolean flag4 = statement instanceof SwitchCase;
		boolean flag5 = statement instanceof LabeledStatement;
		
		if (flag1 || flag2 || flag3 || flag4 || flag5)
			return true;
		else
			return false;
	}
	
	public static SolutionSet removeDuplications(SolutionSet union, List<List<String>> availableManips) throws JMException {
		SolutionSet pop = new SolutionSet(union.size());
		
		Set<SolEdits> set = new HashSet<>();
		for (int i = 0; i < union.size(); i++) {
			SolEdits se = new SolEdits(union.get(i), availableManips);
			set.add(se);
		}
		
		for (SolEdits se : set) {
			pop.add(se.solution);
		}
		return pop;
	}
	
	
	public static List<Integer> getRefArgIndexes(MethodDeclaration md) {
		Map<String, Integer> argMap = new HashMap<>();
		List<Integer> argIndexes = new ArrayList<>();
		
		for (int i = 0; i < md.parameters().size(); i++) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) (md.parameters().get(i));
			
			ITypeBinding tb = svd.getName().resolveTypeBinding();
			
			if (tb.isArray())
				argIndexes.add(i);
			else if (tb.isClass() || tb.isInterface())
				argMap.put(svd.getName().getIdentifier(), i);
		}
		
		if (argMap.size() == 0)
			return argIndexes;
		
		AllSimpleRefASTVisitor asrVisitor = new AllSimpleRefASTVisitor(argMap);
		md.getBody().accept(asrVisitor);
		
		argIndexes.addAll(asrVisitor.getArgIndexes());
		
		return argIndexes;
	}
	
	// exp should be boolean expression
	public static PrefixExpression getNotBooleanExpression(AST ast, Expression exp) {
		PrefixExpression notExp = ast.newPrefixExpression();
		notExp.setOperator(PrefixExpression.Operator.NOT);
		ParenthesizedExpression pe = ast.newParenthesizedExpression();
		pe.setExpression(exp);
		notExp.setOperand(pe);
		
		return notExp;
	}
	
}
