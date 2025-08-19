package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;
import org.reflections.Reflections;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.util.*;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Class<?>> catalog = new HashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections r = new Reflections(basePackage);
        Set<Class<?>> all = new HashSet<>();
        all.addAll(r.getTypesAnnotatedWith(Component.class));
        all.addAll(r.getTypesAnnotatedWith(Service.class));
        all.addAll(r.getTypesAnnotatedWith(Repository.class));

        for (Class<?> c : all) {
            String name = Introspector.decapitalize(c.getSimpleName());
            catalog.put(name, c);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T genBean(String beanName) {
        String key = Introspector.decapitalize(beanName);

        Object cached = singletons.get(key);
        if (cached != null) return (T) cached;

        Class<?> type = catalog.get(key);
        if (type == null) throw new NoSuchElementException("빈을 찾을 수 없음: " + key);

        Constructor<?> ctor = pickCtor(type);
        try {
            Class<?>[] pts = ctor.getParameterTypes();
            Object[] args = new Object[pts.length];

            for (int i = 0; i < pts.length; i++) {
                args[i] = genBean(resolveName(pts[i]));
            }

            ctor.setAccessible(true);
            Object instance = (pts.length == 0) ? ctor.newInstance() : ctor.newInstance(args);
            singletons.put(key, instance);
            return (T) instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("객체 생성 실패: " + type.getName(), e);
        }
    }

    private Constructor<?> pickCtor(Class<?> c) {
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        Constructor<?> best = ctors[0];
        for (Constructor<?> x : ctors) {
            if (x.getParameterCount() > best.getParameterCount()) best = x;
        }
        return best;
    }

    private String resolveName(Class<?> paramType) {
        String direct = Introspector.decapitalize(paramType.getSimpleName());
        if (catalog.containsKey(direct)) return direct;

        for (Map.Entry<String, Class<?>> e : catalog.entrySet()) {
            if (paramType.isAssignableFrom(e.getValue())) {
                return e.getKey();
            }
        }
        throw new NoSuchElementException("적절한 구현체를 찾을 수 없음: " + paramType.getName());
    }
}
