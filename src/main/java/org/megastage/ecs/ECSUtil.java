package org.megastage.ecs;

import com.esotericsoftware.kryo.Kryo;
import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;
import com.jme3.math.Vector3f;
import org.megastage.ecs.components.KryoMessage;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ECSUtil {
    private static transient HashMap<Class, Field[]> _cache = new HashMap<>();

    private static boolean copyableField(Field f) {
        int modifiers = f.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    }

    private static Field[] getFields(Class clazz) {
        Field[] fields = _cache.get(clazz);
        if (fields != null) {
            return fields;
        }

        List<Field> list = loadFields(clazz);
        fields = list.toArray(new Field[list.size()]);
        _cache.put(clazz, fields);
        return fields;
    }

    private static List<Field> loadFields(Class clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> result = new ArrayList<>(declaredFields.length);
        for (Field f : declaredFields) {
            if (copyableField(f)) {
                f.setAccessible(true);
                result.add(f);
            }
        }

        clazz = clazz.getSuperclass();
        if (clazz != null) {
            result.addAll(loadFields(clazz));
        }

        return result;
    }

    public static String toString(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName()).append("(");

        Field[] fields = getFields(obj.getClass());
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Field f = fields[i];
            try {
                sb.append(f.getName()).append(": ");
                if (f.getType().equals(char.class)) {
                    sb.append((int) f.getChar(obj));
                } else {
                    sb.append(f.get(obj));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return "ERROR: " + ex.toString();
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static void setConfig(Object obj, Map<String, String> params, Map<String, ECSEntity> family) throws IllegalAccessException {
        Field[] fields = getFields(obj.getClass());
        for(Field f: fields) {
            String name = f.getName();
            if(params.containsKey(name)) {
                String v = params.get(name);
                if(f.getType() == Integer.TYPE && family.containsKey(v)) {
                    f.setInt(obj, family.get(v).eid);
                } else {
                    setField(obj, f, v);
                }
            }
        }
    }

    private static void setField(Object obj, Field field, String value) throws IllegalAccessException {
        Class type = field.getType();
        if(type == String.class) {
            field.set(obj, value);
        } else if(type == Integer.TYPE) {
            field.setInt(obj, Integer.valueOf(value));
        } else if(type == Float.TYPE) {
            field.setFloat(obj, Float.parseFloat(value));
        } else if(type == Vector3f.class) {
            String[] xyz = value.split(" ");
            Vector3f v = new Vector3f(
                    Float.parseFloat(xyz[0]),
                    Float.parseFloat(xyz[1]),
                    Float.parseFloat(xyz[2])
            );

            field.set(obj, v);
        }
    }

    public static List<String> annotated(final Class<? extends Annotation> annotation) {
        final ArrayList<String> result = new ArrayList<>();

        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(new ClassAnnotationDiscoveryListener() {
            @Override
            public String[] supportedAnnotations() {
                return new String[] { annotation.getName() };
            }

            @Override
            public void discovered(String className, String annotation) {
                result.add(className);
            }
        });

        discoverer.discover(true, false, false, true, false);

        result.sort(String::compareTo);

        return result;
    }

    public static void registerKryoClasses(Kryo kryo) {
        try {
            for(String classname: ECSUtil.annotated(KryoMessage.class)) {
                Class clazz = Class.forName(classname);
                kryo.register(clazz);
            }

            kryo.register(char[].class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ECSException(e);
        }
    }

    static final String HEXES = "0123456789ABCDEF";
    public static String hexlify( char[] raw ) {
        final StringBuilder hex = new StringBuilder( 4 * raw.length + 1);
        for (char c : raw ) {
            hex.append(HEXES.charAt((c & 0xF000) >> 12));
            hex.append(HEXES.charAt((c & 0x0F00) >> 8));
            hex.append(HEXES.charAt((c & 0x00F0) >> 4));
            hex.append(HEXES.charAt((c & 0x000F)));
            hex.append(" ");
        }
        return hex.toString();
    }
}
