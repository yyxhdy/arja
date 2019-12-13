package preprocessing;

import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class RemoveTransientAdapter extends ClassVisitor implements Opcodes {
	public RemoveTransientAdapter(ClassVisitor classVisitor) {
		super(ASM5, classVisitor);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (Modifier.isTransient(access)) {
			return cv.visitField(access - ACC_TRANSIENT, name, desc, signature, value);
		}
		return cv.visitField(access, name, desc, signature, value);
	}

}
