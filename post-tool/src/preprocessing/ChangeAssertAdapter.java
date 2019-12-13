package preprocessing;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ChangeAssertAdapter extends ClassVisitor implements Opcodes {
	public  ChangeAssertAdapter(final ClassVisitor cv) {
		super(ASM5, cv);
	}
	
	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null)
			return null;
		mv = new ReplaceMethodAdapter(mv);
		
		return mv;
	}
}
