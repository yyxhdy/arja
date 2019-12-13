package us.msu.cse.repair.external.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AssertClassAdapter extends ClassVisitor implements Opcodes  {
	public AssertClassAdapter(final ClassVisitor cv) {
		super(ASM5, cv);
	}
	
	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null)
			return null;
		
		mv = new CalDistanceAdapter(access, name,  desc,  signature, exceptions, mv);
		mv = new ReplaceMethodAdapter(mv);
		mv = new ReplaceCompAdapter(mv);
		
		return mv;
	}
	
}
