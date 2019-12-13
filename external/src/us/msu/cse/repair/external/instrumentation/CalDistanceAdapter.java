package us.msu.cse.repair.external.instrumentation;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;


public class CalDistanceAdapter extends MethodNode implements Opcodes {
	public CalDistanceAdapter(int access, String name, String desc, String signature, String[] exceptions,
			MethodVisitor mv) {
		super(ASM5, access, name, desc, signature, exceptions);
/*		System.out.println(name + desc);
		try {
			FileUtils.write(new File("temp.txt"), name + desc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		this.mv = mv;
	}


	@Override
	public void visitEnd() {
		AbstractInsnNode p = instructions.getFirst();
		while (p != null) {
			int k = isATFNode(p);
			if (k >= 0)
				handleATFNode(p, k == 1);
			p = p.getNext();
		}
		maxStack += 4;
		accept(mv);
	}
	
	void handleATFNode(AbstractInsnNode node, boolean flag) {
		LabelNode label = getLabel(node);
		if (label == null)
			return;
		
		int opcode = flag ? Opcodes.ICONST_1 : Opcodes.ICONST_0;
		
		AbstractInsnNode iconst = findICONST(node, label, opcode);
		if (iconst == null)
			return;
		
		AbstractInsnNode icPre = getPrevious(iconst);
		if (icPre == null)
			return;
		
		int oc = icPre.getOpcode();
		
		if (isIfZeroComp(oc)) 
			instructions.insertBefore(icPre, getInsertedList_Z(oc, false));
		else if (isIfIntComp(oc)) 
			instructions.insertBefore(icPre, getInsertedList_I(oc, false));
		
		LabelNode icLabel = getLabel(iconst);
		
		if (icLabel == null)
			return;
		
		AbstractInsnNode p = icLabel.getPrevious();
		while (p != null) {
			if (p instanceof JumpInsnNode) {
				JumpInsnNode jn = (JumpInsnNode) p;
				int op = jn.getOpcode();
				LabelNode ln = jn.label;
				
				if (isIfZeroComp(op) && ln == icLabel) {
					instructions.insertBefore(p, getInsertedList_Z(op, true));
				}
				else if (isIfIntComp(op) && ln == icLabel) {
					instructions.insertBefore(p, getInsertedList_I(op, true));
				}
			}
			p = p.getPrevious();
		}
	}
	
	InsnList getInsertedList_Z(int opcode, boolean flag) {
		InsnList il = new InsnList();
		il.add(new InsnNode(DUP));
		il.add(new IntInsnNode(SIPUSH, opcode));
		if (flag)
			il.add(new InsnNode(ICONST_1));
		else
			il.add(new InsnNode(ICONST_0));
		il.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(AssertTracer.class), "distance",
					Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE, Type.INT_TYPE,
							Type.BOOLEAN_TYPE }), false));
		return il;
	}
	
	InsnList getInsertedList_I(int opcode, boolean flag) {
		InsnList il = new InsnList();
		il.add(new InsnNode(DUP2));
		il.add(new IntInsnNode(SIPUSH, opcode));
		if (flag)
			il.add(new InsnNode(ICONST_1));
		else
			il.add(new InsnNode(ICONST_0));
		il.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(AssertTracer.class), "distance",
				Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE,
						Type.BOOLEAN_TYPE }), false));
		return il;
	}
	
	boolean isIfZeroComp(int opcode) {
		if (opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE || opcode == Opcodes.IFGE ||
				opcode == Opcodes.IFGT || opcode == Opcodes.IFLE || opcode == Opcodes.IFLT)
			return true;
		return false;
	}
	
	boolean isIfIntComp(int opcode) {
		if (opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IF_ICMPNE || opcode == Opcodes.IF_ICMPGE
				|| opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IF_ICMPLE || opcode == Opcodes.IF_ICMPLT)
			return true;
		return false;
	}
	
	AbstractInsnNode findICONST(AbstractInsnNode node, LabelNode label, int opcode) {
		AbstractInsnNode pre = getPrevious(node);
		
		if (pre.getOpcode() == opcode)
			return pre;
		
		AbstractInsnNode p = pre.getPrevious();
		
		while (p != null) {
			if (isICONSTJump(p, label, opcode))
				return p.getPrevious();
			p = p.getPrevious();
		}
		
		return null;	
	}
	
	boolean isICONSTJump(AbstractInsnNode p, LabelNode label, int opcode) {
		if (p instanceof JumpInsnNode) {
			JumpInsnNode jn = (JumpInsnNode) p;
			if (jn.getOpcode() == Opcodes.GOTO && jn.label == label) {
				if (p.getPrevious() != null && p.getPrevious().getOpcode() == opcode)
					return true;
			}
		}
		
		return false;
	}
	
	
	AbstractInsnNode getPrevious(AbstractInsnNode node) {
		node = node.getPrevious();
		while (node != null) {
			if (node.getOpcode() >= 0) 
				return node;
			node = node.getPrevious();
		}
		return null;
	}
	
	LabelNode getLabel(AbstractInsnNode node) {
		node = node.getPrevious();
		while (node != null) {
			if (node instanceof LabelNode)
				return (LabelNode) node;
			if (node.getOpcode() >= 0) 
				return null;
			node = node.getPrevious();
		}
		return null;
	}

	
	int isATFNode(AbstractInsnNode node) {
		if (node instanceof MethodInsnNode) {
			MethodInsnNode mn = (MethodInsnNode) node;
			String clsName = Type.getInternalName(AssertTracer.class);
			
			if (!mn.owner.equals(clsName))
				return -1;
			
			if (mn.name.equals("assertTrue")) {
				return 1;
			}
			if (mn.name.equals("assertFalse")) {
				return 0;
			}
		}
		
		return -1;
	}
}