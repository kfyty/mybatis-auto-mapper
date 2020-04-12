package com.kfyty.mybatis.auto.mapper.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 功能描述: 通用工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 17:40
 * @since JDK 1.8
 */
public abstract class CommonUtil {
    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z0-9]*");

    public static boolean empty(String s) {
        return !Optional.ofNullable(s).filter(e -> e.trim().length() != 0).isPresent();
    }

    public static <T> boolean empty(T[] arr) {
        return !Optional.ofNullable(arr).filter(e -> e.length != 0).isPresent();
    }

    public static boolean empty(Collection c) {
        return !Optional.ofNullable(c).filter(e -> !e.isEmpty()).isPresent();
    }

    public static boolean empty(Map m) {
        return !Optional.ofNullable(m).filter(e -> !e.isEmpty()).isPresent();
    }

    public static boolean baseType(Class<?> clazz) {
        return byte.class.isAssignableFrom(clazz)           ||
                short.class.isAssignableFrom(clazz)         ||
                int.class.isAssignableFrom(clazz)           ||
                long.class.isAssignableFrom(clazz)          ||
                float.class.isAssignableFrom(clazz)         ||
                double.class.isAssignableFrom(clazz)        ||
                Number.class.isAssignableFrom(clazz)        ||
                CharSequence.class.isAssignableFrom(clazz)  ||
                Date.class.isAssignableFrom(clazz);
    }

    public static int size(Object obj) {
        if(obj == null) {
            return 0;
        }
        if(obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        if(obj instanceof Collection) {
            return ((Collection) obj).size();
        }
        if(obj instanceof Map) {
            return ((Map) obj).size();
        }
        return 1;
    }

    public static List<String> split(String source, String regex) {
        return Arrays.stream(source.split(regex)).filter(e -> !empty(e)).collect(Collectors.toList());
    }

    public static String convert2BeanName(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    public static String convert2Hump(String s) {
        return convert2Hump(s, false);
    }

    public static String convert2Hump(String s, boolean isClass) {
        s = Optional.ofNullable(s).map(e -> e.contains("_") || UPPER_CASE_PATTERN.matcher(e).matches() ? e.toLowerCase() : e).orElseThrow(() -> new NullPointerException("column is null"));
        while(s.contains("_")) {
            int index = s.indexOf('_');
            if(index == s.length() - 1) {
                break;
            }
            char ch = s.charAt(index + 1);
            s = s.replace("_" + ch, "" + Character.toUpperCase(ch));
        }
        return !isClass ? s : s.length() == 1 ? s.toUpperCase() : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String convert2Underline(String s) {
        return convert2Underline(s, true);
    }

    public static String convert2Underline(String s, boolean lower) {
        Optional.ofNullable(s).filter(e -> !e.isEmpty()).orElseThrow(() -> new NullPointerException("field is null"));
        if(UPPER_CASE_PATTERN.matcher(s).matches()) {
            return lower ? s.toLowerCase() : s.toUpperCase();
        }
        char c = s.charAt(0);
        StringBuilder builder = new StringBuilder();
        builder.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : c);
        for(int i = 1; i < s.length(); i++) {
            c = s.charAt(i);
            if(Character.isUpperCase(c)) {
                builder.append("_").append(Character.toLowerCase(c));
                continue;
            }
            builder.append(c);
        }
        return lower ? builder.toString() : builder.toString().toUpperCase();
    }

    public static String fillString(String s, Object ... params) {
        int index = -1;
        int paramIndex = 0;
        StringBuilder sb = new StringBuilder(s);
        while((index = sb.indexOf("{}", index)) != -1) {
            sb.replace(index, index + 2, Optional.ofNullable(params[paramIndex++]).map(Object::toString).orElse("null"));
        }
        return sb.toString();
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, false);
    }

    public static Field getField(Class<?> clazz, String fieldName, boolean containPrivate) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch(NoSuchFieldException e) {
            return getSuperField(clazz, fieldName, containPrivate);
        }
    }

    public static Field getSuperField(Class<?> clazz, String fieldName, boolean containPrivate) {
        if(clazz.getSimpleName().equals("Object")) {
            return null;
        }
        try {
            clazz = clazz.getSuperclass();
            Field field = clazz.getDeclaredField(fieldName);
            return !containPrivate && Modifier.isPrivate(field.getModifiers()) ? null : field;
        } catch(NoSuchFieldException e) {
            return getSuperField(clazz, fieldName, containPrivate);
        }
    }

    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        return getFieldMap(clazz, false);
    }

    public static Map<String, Field> getFieldMap(Class<?> clazz, boolean containPrivate) {
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getSuperFieldMap(clazz, containPrivate));
        return map;
    }

    public static Map<String, Field> getSuperFieldMap(Class<?> clazz, boolean containPrivate) {
        if(clazz == null || clazz.getSimpleName().equals("Object")) {
            return new HashMap<>(0);
        }
        clazz = clazz.getSuperclass();
        Map<String, Field> map = new HashMap<>();
        map.putAll(Arrays.stream(clazz.getDeclaredFields()).filter(e -> containPrivate || !Modifier.isPrivate(e.getModifiers())).collect(Collectors.toMap(Field::getName, e -> e)));
        map.putAll(getSuperFieldMap(clazz, containPrivate));
        return map;
    }

    public static void setAnnotationValue(Annotation annotation, String annotationField, Object value) throws Exception {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        Field field = invocationHandler.getClass().getDeclaredField("memberValues");
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Map memberValues = (Map) field.get(invocationHandler);
        memberValues.put(annotationField, value);
        field.setAccessible(accessible);
    }
}
