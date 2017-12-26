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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.eclipse.jdt.core.dom.WhileStatement;

import jmetal.util.PseudoRandom;
import us.msu.cse.repair.core.parser.MethodInfo;
import us.msu.cse.repair.core.parser.VarInfo;

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
			map.put(key, mi);
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
			map.put(key, mi);
		}

		return map;
	}

	public static VarInfo getVarInfo(IVariableBinding vb) {
		ITypeBinding typeBinding = vb.getVariableDeclaration().getType();
		String typeName = typeBinding.getQualifiedName();
		int mod = vb.getModifiers();
		VarInfo vi = new VarInfo(typeName, vb, mod);
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
		
		MethodInfo mi = new MethodInfo(returnTypeName, returnTypeBinding, parameterTypeNames, mod);
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
		if (method.getGenericParameterTypes().length > 0) 
			parameterTypeNames += getGenericParameterTypeNamesForMethod(method.toGenericString());
		
		MethodInfo mi = new MethodInfo(returnTypeName, null, parameterTypeNames, mod);
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
		int mod = mi.getModifiers();
		return Modifier.isStatic(mod);
	}

	public static boolean isPublicMethod(MethodInfo mi) {
		int mod = mi.getModifiers();
		return Modifier.isPublic(mod);
	}

	public static boolean isProtectedMethod(MethodInfo mi) {
		int mod = mi.getModifiers();
		return Modifier.isProtected(mod);
	}

	public static boolean isPackagePrivateMethod(MethodInfo mi) {
		int mod = mi.getModifiers();
		boolean isPublic = Modifier.isPublic(mod);
		boolean isPrivate = Modifier.isPrivate(mod);
		boolean isProtected = Modifier.isProtected(mod);
		return !(isPublic || isPrivate || isProtected);
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

	public static boolean isRuntimeException(ITypeBinding tb) {
		while (tb != null) {
			if (tb.getName().equals("RuntimeException"))
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
}
