package lancet_.tameable_foxes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class FoxMixinPlugin implements IMixinConfigPlugin {
    private final static String foxTamerMixin = "lancet_.tameable_foxes.mixin.FoxTamerMixin";
    private boolean finishedFoxTransformation = false;
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "lancet_.tameable_foxes.mixin.compat.NEAPetAnimationMixin", () -> LoadingModList.get().getModFileById("notenoughanimations") != null,
            "lancet_.tameable_foxes.mixin.compat.CompanionHooksMixin", () -> LoadingModList.get().getModFileById("companion") != null,
            "lancet_.tameable_foxes.mixin.compat.CompanionTargetGoalMixin", () -> LoadingModList.get().getModFileById("companion") != null
    );

    @Override
    public void onLoad(String rawMixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        String tame = "net/minecraft/world/entity/TamableAnimal";
        String fox = "net.minecraft.world.entity.animal.Fox";
        if (targetClassName.equals(fox) && mixinClassName.equals(foxTamerMixin) && !finishedFoxTransformation){
            targetClass.superName = tame;
            // the super() call still refs the old superclass - updop it
            for (MethodNode func : targetClass.methods) {
                if (!func.name.equals("<init>")) continue;

                InsnList is = func.instructions;
                AbstractInsnNode insn = is.getFirst();
                for (; insn.getNext() != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                        break;
                    }
                }
                // overcomplicated splicing so mixins recognize the old super
                if (insn instanceof MethodInsnNode call) {
                    MethodInsnNode updop = (MethodInsnNode) call.clone(null);
                    updop.owner = tame;

                    LabelNode skip = new LabelNode();
                    is.insert(call, skip);
                    is.insert(skip, updop);
                    is.insertBefore(call, new JumpInsnNode(Opcodes.GOTO, skip));
                    finishedFoxTransformation = true;
                }
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        targetClass.interfaces.remove(mixinClassName.replace('.', '/'));
    }
}
