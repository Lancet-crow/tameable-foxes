package lancet_.tameable_foxes;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class MappingUtil {
    private static final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

    public static String intermediaryToMappedMethodName(String intermediaryClassName, String intermediaryMethodName, String desc) {
        return resolver.mapMethodName("intermediary", intermediaryClassName, intermediaryMethodName, desc);
    }

    public static String intermediaryToMappedClassName(String intermediaryClassName) {
        return resolver.mapClassName("intermediary", intermediaryClassName);
    }

    public static String definitionToIntermediaryClassName(Class<?> clazz) {
        return resolver.unmapClassName("intermediary", clazz.getName());
    }

    public static String definitionToIntermediaryBytecodeName(Class<?> clazz) {
        return "L" + definitionToIntermediaryClassName(clazz).replace('.', '/') + ";";
    }

}
