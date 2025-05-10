package lancet_.tameable_foxes;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TameableAgent implements LanguageAdapter {
    public TameableAgent() {}

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new Error();
    }

    static {
        try {
            ByteBuddyAgent.install();
        } catch (Exception e) {
            TameableFoxes.LOGGER.warn(e.getMessage());
        }

        String foxEntityClassName = MappingUtil.intermediaryToMappedClassName("net.minecraft.class_4019").replace('.', '/');
        String tameableEntityClassName = MappingUtil.intermediaryToMappedClassName("net.minecraft.class_1321").replace('.', '/');

        ByteBuddyAgent.getInstrumentation().addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if(!className.equals(foxEntityClassName)) return classfileBuffer;

                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

                String[] oldParent = new String[1];
                ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
                    @Override
                    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                        oldParent[0] = superName;
                        super.visit(version, access, name, signature, tameableEntityClassName, interfaces);
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                        if(name.equals("<init>")) {
                            return new MethodVisitor(Opcodes.ASM9, visitor) {
                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    if(opcode == Opcodes.INVOKESPECIAL && owner.equals(oldParent[0])) {
                                        super.visitMethodInsn(opcode, tameableEntityClassName, name, descriptor, isInterface);
                                    } else {
                                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                    }
                                }
                            };
                        }
                        return visitor;
                    }
                };

                reader.accept(visitor, 0);
                return writer.toByteArray();
            }
        });
    }
}
