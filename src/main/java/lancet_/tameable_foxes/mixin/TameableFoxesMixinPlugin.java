package lancet_.tameable_foxes.mixin;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * :irritatered:
 */
public final class TameableFoxesMixinPlugin implements IMixinConfigPlugin {
    private boolean finishedFoxTransformation;
    private static final String foxTamerMixin = "lancet_.tameable_foxes.mixin.FoxTamerMixin";
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "lancet_.tameable_foxes.mixin.compat.NEAPetAnimationMixin", () -> FabricLoader.getInstance().isModLoaded("notenoughanimations"),
            "lancet_.tameable_foxes.mixin.compat.CompanionHooksMixin", () -> FabricLoader.getInstance().isModLoaded("companion"),
            "lancet_.tameable_foxes.mixin.compat.CompanionTargetGoalMixin", () -> FabricLoader.getInstance().isModLoaded("companion")
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).get();
    }

    // Boilerplate

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        String tame = map("net.minecraft.class_1321");
        String fox = "net.minecraft.class_4019";
        if (targetClassName.equals(fox) && mixinClassName.equals(foxTamerMixin) && !finishedFoxTransformation) {
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

    }

    private static String map(String clazz) {
        MappingResolver remap = FabricLoader.getInstance().getMappingResolver();
        return remap.mapClassName("intermediary", clazz).replace('.', '/');
    }
}