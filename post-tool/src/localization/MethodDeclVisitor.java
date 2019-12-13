package localization;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclVisitor extends ASTVisitor {
	Set<Integer> lineNumbers;
	Map<String, Set<MethodInfo>> modifiedMethods;
	
	public MethodDeclVisitor(Set<Integer> lineNumbers, Map<String, Set<MethodInfo>> modifiedMethods) { 
		this.lineNumbers = lineNumbers;
		this.modifiedMethods = modifiedMethods;
	}
	
	@Override
	public boolean visit(MethodDeclaration md) {
		CompilationUnit unit = (CompilationUnit) md.getRoot();
		
		int len = md.getLength();
		int startPos = md.getStartPosition();
		int startLineNumber = unit.getLineNumber(startPos);
		int endLineNumber = unit.getLineNumber(startPos + len - 1);
		
		if (isModified(startLineNumber, endLineNumber)) {
			IMethodBinding tb = md.resolveBinding();
			String clsName = tb.getDeclaringClass().getBinaryName();
			
			boolean isConstructor = tb.isConstructor();
			String methodName = isConstructor ? "<init>" : tb.getName();
			
			int np = tb.getParameterTypes().length;
			String returnType = tb.getReturnType().getBinaryName();
			
			returnType = transform(returnType);
			
			String parameterTypes[] = new String[np];
			
			for (int i = 0; i < np; i++) {
				parameterTypes[i] = tb.getParameterTypes()[i].getBinaryName();
			//	System.out.println("abc: " + tb.getParameterTypes()[i].getBinaryName());
				parameterTypes[i] = transform(parameterTypes[i]);
			}
			
			boolean isStatic = Modifier.isStatic(tb.getModifiers());	
			
			
			MethodInfo mi = new MethodInfo(methodName, parameterTypes, returnType, isStatic, isConstructor);
			add(clsName, mi);
		}
		
		return true;
	}
	
	String transform(String binaryName) {
		if (binaryName.contains(".")) {
			binaryName = binaryName.replace(".", "/");
			
			if (!binaryName.endsWith(";")) {
				int index = binaryName.lastIndexOf("[");
				binaryName = binaryName.substring(0, index + 1) + "L" + binaryName.substring(index + 1) + ";";
			}
		}
		return binaryName;
	}
	
	boolean isModified(int startLineNumber, int endLineNumber) {
		for (int l : lineNumbers) {
			if (l >= startLineNumber && l <= endLineNumber)
				return true;
		}
		return false;
	}
	
	void add(String clsName, MethodInfo mi) {
		if (!modifiedMethods.containsKey(clsName)) {
			Set<MethodInfo> set = new HashSet<>();
			set.add(mi);
			modifiedMethods.put(clsName, set);
		}
		else {
			modifiedMethods.get(clsName).add(mi);
		}
	}
}
