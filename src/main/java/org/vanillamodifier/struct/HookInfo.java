package org.vanillamodifier.struct;

import org.vanillamodifier.annotations.Unique;
import org.vanillamodifier.exceptions.NoSuchFlagException;

import java.lang.reflect.Method;
import java.util.List;

public class HookInfo {

    public HookInfo(Class<?> hook, Class<?> target, List<Method[]> methods){

    }

    public static Method getUniqueMethod(Class<?> target,String flag){
        for(Method m : target.getDeclaredMethods()){
            if(m.isAnnotationPresent(Unique.class)){
                Unique unique = m.getAnnotation(Unique.class);
                if(unique.value().equals(flag))
                    return m;
            }
        }
        throw new NoSuchFlagException(flag);
    }
}
