package us.msu.cse.repair.external.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ReplaceCompAdapter extends MethodVisitor implements Opcodes {
	public ReplaceCompAdapter(final MethodVisitor mv) {
		super(ASM5, mv);
	}
	
	@Override
	public void visitInsn(int opcode) {
		if (opcode == LCMP) {
			mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ComparisonHelper.class), "longSub",
					Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.LONG_TYPE, Type.LONG_TYPE }), false);
		} else if (opcode == DCMPG) {
			mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ComparisonHelper.class), "doubleSubG",
					Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.DOUBLE_TYPE, Type.DOUBLE_TYPE }), false);
		} else if (opcode == DCMPL) {
			mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ComparisonHelper.class), "doubleSubL",
					Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.DOUBLE_TYPE, Type.DOUBLE_TYPE }), false);
		} else if (opcode == FCMPG) {
			mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ComparisonHelper.class), "floatSubG",
					Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.FLOAT_TYPE, Type.FLOAT_TYPE }), false);
		} else if (opcode == FCMPL) {
			mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ComparisonHelper.class), "floatSubL",
					Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.FLOAT_TYPE, Type.FLOAT_TYPE }), false);
		} else {
			mv.visitInsn(opcode);
		}
	}
}
