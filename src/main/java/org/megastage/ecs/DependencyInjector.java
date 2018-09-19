package org.megastage.ecs;

import com.esotericsoftware.minlog.Log;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

public class DependencyInjector {
    public static List<Object> inject(Map<Class, Object> singletons) {
        List<Object> instances = new ArrayList<>();

        for(Object singleton: singletons.values()) {
            for (Field field : singleton.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    try {
                        boolean access = field.isAccessible();
                        if(!access) {
                            field.setAccessible(true);
                        }

                        Log.debug(String.format("Injecting dependency %s.%s = %s",
                                singleton.getClass().getSimpleName(), field.getName(),
                                field.getType().getSimpleName()));

                        if(singletons.containsKey(field.getType())) {
                            field.set(singleton, singletons.get(field.getType()));
                        } else {
                            Object value = field.getType().newInstance();
                            instances.add(value);
                            field.set(singleton, value);
                        }

                        if(!access) {
                            field.setAccessible(false);
                        }
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return instances;
    }

    public static Map<Class, Object> instantiateSingletons(String rootPackage) {
        Reflections reflections = new Reflections(rootPackage);

        Map<Class, Object> singletons = new HashMap<>();

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Singleton.class);
        for(Class clazz: classes) {
            try {
                Log.debug(String.format("Instantiate singleton: %s", clazz.getName()));
                singletons.put(clazz, clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return singletons;
    }
}
