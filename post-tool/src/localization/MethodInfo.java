package localization;

import org.objectweb.asm.Opcodes;

public class MethodInfo {
	String methodName;
	String parameterTypes[]; // type in bytecode
	String returnType; // type in bytecode
	
	String desc;
	boolean isStatic;
	boolean isConstructor;
	
	int[] loadOpcodes;
	int[] loadVars;
	
	int numberOfInputs;
	int numberOfOutputs;

	public MethodInfo(String methodName, String parameterTypes[], String returnType, boolean isStatic,
			boolean isConstructor) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
		this.isStatic = isStatic;
		this.isConstructor = isConstructor;
		fillDesc();
		fillNIO();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MethodInfo))
			return false;
		MethodInfo mi = (MethodInfo) o;
		String a = this.methodName + "#" + this.desc;
		String b = mi.methodName + "#" + mi.desc;
		return a.equals(b);
	}

	@Override
	public int hashCode() {
		String a = this.methodName + "#" + this.desc;
		return a.hashCode();
	}

	@Override
	public String toString() {
		return  this.methodName + "#" + this.desc;
	}
	
	private void fillDesc() {
		desc = "(";
		for (String type : parameterTypes) 
			desc += type;
		desc += ")";
		desc += returnType;
	}
	
	private void fillNIO() {
		numberOfInputs = parameterTypes.length;
		if (!isStatic && !isConstructor)
			numberOfInputs++;
		
		numberOfOutputs = 0;
		if (!isStatic)
			numberOfOutputs++;
		
		for (int i = 0; i < parameterTypes.length; i++) {
			if (!isParameterPrimitive(i))
				numberOfOutputs++;
		}
		
		if (!isReturnVoid())
			numberOfOutputs++;
	}
	
	private void fillLoadIns() {
		loadOpcodes = new int[parameterTypes.length];
		loadVars = new int[parameterTypes.length];
		int start = this.isStatic ? 0 : 1;
		
		for (int i = 0; i < parameterTypes.length; i++) {
			String type = parameterTypes[i];
			loadVars[i] = start;
			loadOpcodes[i] = getOpcodes(type);
			
			if (type.equals("D") || type.equals("J"))
				start += 2;
			else
				start++;
		}
	}
	
	private int getOpcodes(String type) {
		if (type.equals("F")) 
			return Opcodes.FLOAD;
		else if (type.equals("J"))
			return Opcodes.LLOAD;
		else if (type.equals("D")) 
			return Opcodes.DLOAD;
		else if (type.equals("Z") || type.equals("C") || type.equals("B") || type.equals("S") || 
				type.equals("I"))
			return Opcodes.ILOAD;
		else 
			return Opcodes.ALOAD;
			
	}
	
	private boolean isPrimitive(String type) {
		if (type.equals("Z") ||type.equals("C") || type.equals("B") ||
				type.equals("S")  || type.equals("I") || type.equals("F") ||
				type.equals("J") || type.equals("D"))
			return true;
		else 
			return false;
	}
 
	public String getName() {
		return this.methodName;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public int getNumberOfParameters() {
		return this.parameterTypes.length;
	}
	
	public String getReturnType() {
		return this.returnType;
	}
	
	public String[] getParameterTypes() {
		return this.parameterTypes;
	}
	
	public boolean isStatic() {
		return this.isStatic;
	}
	
	public int getLoadOpcode(int index) {
		if (this.loadOpcodes == null)
			fillLoadIns();
		
		return this.loadOpcodes[index];
	}
	
	public int getLoadVar(int index) {
		if (this.loadVars == null)
			fillLoadIns();
		
		return this.loadVars[index];
	}
	
	public int[] getLoadOpcodes() {
		if (this.loadOpcodes == null)
			fillLoadIns();
		
		return this.loadOpcodes;
	}
	
	public int[] getLoadVars() {
		if (this.loadVars == null)
			fillLoadIns();
		
		return this.loadVars;
	}
	
	public boolean isReturnVoid() {
		return returnType.equals("V");
	}
	
	public boolean isConstructor() {
		return this.isConstructor;
	}
	
	public boolean isReturnPrimitive() {
		return isPrimitive(this.returnType);
	}
	public boolean isParameterPrimitive(int index) {
		String type = parameterTypes[index];
		return isPrimitive(type);
		
	}
	
	public String getParameterType(int index) {
		return this.parameterTypes[index];
	}
	
	public int getNumberOfInputs() {
		return this.numberOfInputs;
	}
	
	public int getNumberOfOutputs() {
		return this.numberOfOutputs;
	}
}
