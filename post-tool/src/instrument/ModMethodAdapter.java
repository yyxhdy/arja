package instrument;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import localization.MethodInfo;

public class ModMethodAdapter extends LocalVariablesSorter implements Opcodes {
	String className;

	MethodInfo mi;
	
	String storeRoot;
	
	private int id;
	
	public ModMethodAdapter(final int access, final String desc, final MethodVisitor mv, String className,
			MethodInfo mi, String storeRoot) {
		super(ASM5, access, desc, mv);
		this.className = className;
		this.mi = mi;
		this.storeRoot = storeRoot;
	}
	
	@Override
	public void visitCode() {
		mv.visitCode();

		String owner = className.replace(".", "/");
		String name = mi.isConstructor() ? "init" : mi.getName();
		String counter = "c_" + name + "_" + Math.abs(mi.getDesc().hashCode());
		
		mv.visitFieldInsn(GETSTATIC, owner, counter, "I");
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IADD);
		mv.visitInsn(DUP);
		mv.visitFieldInsn(PUTSTATIC, owner, counter, "I");
		id = newLocal(Type.INT_TYPE);
		mv.visitVarInsn(ISTORE, id);
	}
	
	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
			String owner = className.replace(".", "/");
			
			int n = 0;
			if (!mi.isStatic()) {
				insertMI(owner, null, ALOAD, 0, "out2", n);
				n++;
			}
			
			for (int i = 0; i < mi.getNumberOfParameters(); i++) {
				if (mi.isParameterPrimitive(i))
					continue;
				
				int op = mi.getLoadOpcode(i);
				int var = mi.getLoadVar(i);
				insertMI(owner, null, op, var, "out2", n);	
				n++;
			}
			
			if (!mi.isReturnVoid()) {
				if (opcode!= ATHROW && (mi.getReturnType().equals("D") || mi.getReturnType().equals("J")))
					mv.visitInsn(DUP2);
				else
					mv.visitInsn(DUP);
				if (mi.isReturnPrimitive() && opcode != ATHROW)
					transformToObject(mi.getReturnType());
				insertMI(owner, "out2", n);
			}
			
		}
		mv.visitInsn(opcode);
	}
	
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack + 7, maxLocals + 1);
	}
	
	
	void insertMI(String owner, String type, int opcode, int var, String inout, int n) {
		mv.visitVarInsn(opcode, var);
		transformToObject(type);
		insertMI(owner, inout, n);
	}
	
	void transformToObject(String type) {
		if (type == null)
			return;
		
		if (type.equals("Z"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
		else if (type.equals("C"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
		else if (type.equals("B"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
		else if (type.equals("S"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
		else if (type.equals("I"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		else if (type.equals("F"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
		else if (type.equals("J"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
		else if (type.equals("D"))
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
	}
	
	
	void insertMI(String owner, String inout, int n) {
		mv.visitLdcInsn(storeRoot);
		mv.visitLdcInsn(className);
		mv.visitLdcInsn(mi.getName());
		mv.visitLdcInsn(mi.getDesc());
		mv.visitVarInsn(ILOAD, id);
		mv.visitLdcInsn(inout);
		mv.visitIntInsn(BIPUSH, n);
		
		mv.visitMethodInsn(INVOKESTATIC, "serialization/Storer", "generateStorePath",
				"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;I)Ljava/lang/String;",
				false);
		
		String desc = "(Ljava/lang/Object;Ljava/lang/String;)V";
		mv.visitMethodInsn(INVOKESTATIC, "serialization/Storer", "storeObject", desc, false);
	}

	
}
