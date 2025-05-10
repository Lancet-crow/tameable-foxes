package lancet_.tameable_foxes;

import net.minecraft.entity.passive.TameableEntity;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.WeakHashMap;

public class ReflectionUtil {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<Class<?>, MethodHandles.Lookup> lookupCache = new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object instance, String intermediaryClassName, String intermediaryMethodName, String methodDesc, Class<T> returnType, Class<?>[] parameterTypes, Object... parameters) {
        String methodName = MappingUtil.intermediaryToMappedMethodName(intermediaryClassName, intermediaryMethodName, methodDesc);
        MethodType methodType = MethodType.methodType(returnType, parameterTypes);

        Object[] args = new Object[parameters.length + 1];
        args[0] = instance;
        System.arraycopy(parameters, 0, args, 1, parameters.length);

        try {
            MethodHandles.Lookup lookup = getLookup(instance.getClass());
            MethodHandle methodHandle = lookup.findVirtual(TameableEntity.class, methodName, methodType);
            return (T) methodHandle.invokeWithArguments(args);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static MethodHandles.Lookup getLookup(Class<?> reference) {
        return lookupCache.computeIfAbsent(reference, k -> {
            try {
                return MethodHandles.privateLookupIn(k, lookup);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
