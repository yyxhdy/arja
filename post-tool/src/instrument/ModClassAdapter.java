package instrument;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import localization.MethodInfo;

public class ModClassAdapter extends ClassVisitor implements Opcodes {
	Set<MethodInfo> methods;
	String className;
	String storeRoot;
	
	public ModClassAdapter(final ClassVisitor cv, String className, Set<MethodInfo> methods, String storeRoot) {
		super(ASM5, cv);
		this.className = className;
		this.methods = methods;
		this.storeRoot = storeRoot;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {

		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		
		if (mv != null) {
			MethodInfo mi = getMethod(name, desc);
			if (mi != null) 
				mv = new ModMethodAdapter(access, desc, mv, className, mi, storeRoot);
		}
		
		return mv;
	}
	
	MethodInfo getMethod(String name, String desc) {
		for (MethodInfo m : methods) {
			if (m.getName().equals(name) && m.getDesc().equals(desc))
				return m;
		}
		return null;
	}
	
	
	@Override
	public void visitEnd() {
		for (MethodInfo method : methods) {
			String mn = method.isConstructor() ? "init" : method.getName();
			String name = "c_" + mn + "_" + Math.abs(method.getDesc().hashCode());
			FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_STATIC, name, "I", null, 0);
			if (fv != null)
				fv.visitEnd();
		}
		
		cv.visitEnd();
	}
	
}
