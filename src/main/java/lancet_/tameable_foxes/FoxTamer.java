package lancet_.tameable_foxes;

import me.shedaniel.mm.api.ClassTinkerers;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class FoxTamer {
    private static String map(String clazz){
        MappingResolver remap = FabricLoader.getInstance().getMappingResolver();
        return remap.mapClassName("intermediary", clazz).replace('.', '/');
    }
    public static void asm(){
        String tame = map("net.minecraft.class_1321");
        String fox = map("net.minecraft.class_4019");
        ClassTinkerers.addTransformation(fox, (ClassNode clazz) -> {
            clazz.superName = tame;
            // the super() call still refs the old superclass - updop it
            for(MethodNode func : clazz.methods){
                if(!func.name.equals("<init>")) continue;

                InsnList is = func.instructions;
                AbstractInsnNode insn = is.getFirst();
                for(; insn.getNext() != null; insn = insn.getNext()){
                    if(insn.getOpcode() == Opcodes.INVOKESPECIAL){
                        break;
                    }
                }
                // overcomplicated splicing so mixins recognize the old super
                if(insn instanceof MethodInsnNode call){
                    MethodInsnNode updop = (MethodInsnNode)call.clone(null);
                    updop.owner = tame;

                    LabelNode skip = new LabelNode();
                    is.insert(call, skip);
                    is.insert(skip, updop);
                    is.insertBefore(call, new JumpInsnNode(Opcodes.GOTO, skip));
                    //TameableFoxes.LOGGER.info("berries!");
                }
            }
        });
        //TameableFoxes.LOGGER.info("frrr?");
    }
}
