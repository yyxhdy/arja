package us.msu.cse.repair.external.instrumentation;



import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ReplaceMethodAdapter extends MethodVisitor implements Opcodes {
	final String newOwner = Type.getInternalName(AssertTracer.class);
	final String assertJ4Class = "org/junit/Assert";
	final String assertJ3Class = "junit/framework/Assert";
	final Set<String> assertJ3Methods = new HashSet<>(
			Arrays.asList( 
					"fail(Ljava/lang/String;)V",
					"fail()V",
					"assertTrue(Ljava/lang/String;Z)V",
					"assertTrue(Z)V",
					"assertFalse(Z)V",
					"assertFalse(Ljava/lang/String;Z)V",
					"assertEquals(BB)V",
					"assertEquals(Ljava/lang/String;CC)V",
					"assertEquals(CC)V",
					"assertEquals(ZZ)V",
					"assertEquals(Ljava/lang/String;ZZ)V",
					"assertEquals(Ljava/lang/String;BB)V",
					"assertEquals(II)V",
					"assertEquals(Ljava/lang/String;II)V",
					"assertEquals(SS)V",
					"assertEquals(Ljava/lang/String;SS)V",
					"assertEquals(Ljava/lang/String;DDD)V",
					"assertEquals(Ljava/lang/String;Ljava/lang/String;)V",
					"assertEquals(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
					"assertEquals(Ljava/lang/Object;Ljava/lang/Object;)V",
					"assertEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
					"assertEquals(JJ)V",
					"assertEquals(Ljava/lang/String;JJ)V",
					"assertEquals(FFF)V",
					"assertEquals(Ljava/lang/String;FFF)V",
					"assertEquals(DDD)V",
					"assertNotNull(Ljava/lang/String;Ljava/lang/Object;)V",
					"assertNotNull(Ljava/lang/Object;)V",
					"assertNull(Ljava/lang/Object;)V",
					"assertNull(Ljava/lang/String;Ljava/lang/Object;)V",
					"assertSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
					"assertSame(Ljava/lang/Object;Ljava/lang/Object;)V",
					"assertNotSame(Ljava/lang/Object;Ljava/lang/Object;)V",
					"assertNotSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
					"failSame(Ljava/lang/String;)V",
					"failNotSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
					"failNotEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
	));
	
	
	final Set<String> assertJ4Methods = new HashSet<>(Arrays.asList(
			"assertTrue(Z)V",
			"assertTrue(Ljava/lang/String;Z)V",
			"fail(Ljava/lang/String;)V",
			"fail()V",
			"assertFalse(Z)V",
			"assertFalse(Ljava/lang/String;Z)V",
			"assertEquals(JJ)V",
			"assertEquals(FFF)V",
			"assertEquals(DDD)V",
			"assertEquals(Ljava/lang/String;[Ljava/lang/Object;[Ljava/lang/Object;)V",
			"assertEquals([Ljava/lang/Object;[Ljava/lang/Object;)V",
			"assertEquals(Ljava/lang/String;FFF)V",
			"assertEquals(Ljava/lang/String;DDD)V",
			"assertEquals(Ljava/lang/String;JJ)V",
			"assertEquals(DD)V",
			"assertEquals(Ljava/lang/String;DD)V",
			"assertEquals(Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertNotNull(Ljava/lang/String;Ljava/lang/Object;)V",
			"assertNotNull(Ljava/lang/Object;)V",
			"assertNull(Ljava/lang/Object;)V",
			"assertNull(Ljava/lang/String;Ljava/lang/Object;)V",
			"assertSame(Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertNotSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertNotSame(Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertNotEquals(Ljava/lang/String;DDD)V",
			"assertNotEquals(FFF)V",
			"assertNotEquals(DDD)V",
			"assertNotEquals(Ljava/lang/String;FFF)V",
			"assertNotEquals(JJ)V",
			"assertNotEquals(Ljava/lang/String;JJ)V",
			"assertNotEquals(Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertNotEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			"assertArrayEquals([C[C)V",
			"assertArrayEquals(Ljava/lang/String;[S[S)V",
			"assertArrayEquals([Ljava/lang/Object;[Ljava/lang/Object;)V",
			"assertArrayEquals(Ljava/lang/String;[Ljava/lang/Object;[Ljava/lang/Object;)V",
			"assertArrayEquals(Ljava/lang/String;[I[I)V",
			"assertArrayEquals(Ljava/lang/String;[Z[Z)V",
			"assertArrayEquals([Z[Z)V",
			"assertArrayEquals(Ljava/lang/String;[B[B)V",
			"assertArrayEquals([B[B)V",
			"assertArrayEquals(Ljava/lang/String;[C[C)V",
			"assertArrayEquals([D[DD)V",
			"assertArrayEquals(Ljava/lang/String;[F[FF)V",
			"assertArrayEquals([F[FF)V",
			"assertArrayEquals([S[S)V",
			"assertArrayEquals([I[I)V",
			"assertArrayEquals(Ljava/lang/String;[J[J)V",
			"assertArrayEquals([J[J)V",
			"assertArrayEquals(Ljava/lang/String;[D[DD)V",
			"assertThat(Ljava/lang/Object;Lorg/hamcrest/Matcher;)V",
			"assertThat(Ljava/lang/String;Ljava/lang/Object;Lorg/hamcrest/Matcher;)V"
));
	
	
	public ReplaceMethodAdapter(final MethodVisitor mv) {
		super(ASM5, mv);
	}
	

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {		
		if ((opcode == Opcodes.INVOKESTATIC) && (owner.equals(assertJ4Class) || 
				owner.equals(assertJ3Class) ||
				isSubJMI(assertJ3Class, owner, name, descriptor) || 
				isSubJMI(assertJ4Class, owner, name, descriptor))) {
			mv.visitMethodInsn(opcode, newOwner, name, descriptor, isInterface);
		}
		else {
			mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}
	}
	
	
	boolean isSubJMI(String assertClsName, String owner, String name, String descriptor) {
		try {	
			String method = name + descriptor;
			
			if (assertClsName.equals(assertJ3Class) && !assertJ3Methods.contains(method))
				return false;
			
			if (assertClsName.equals(assertJ4Class) && !assertJ4Methods.contains(method))
				return false;
			
			Class<?> assertJCls = Class.forName(assertClsName.replace("/", "."));
			Class<?> ownerCls = Class.forName(owner.replace("/", "."));
		    boolean isSub = assertJCls.isAssignableFrom(ownerCls);
		    if (!isSub)
		    	return false;
		    
			return true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return false;
	}
	
}
