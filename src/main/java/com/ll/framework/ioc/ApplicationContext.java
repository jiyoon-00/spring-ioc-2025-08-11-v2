package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.framework.ioc.annotations.Repository;
import com.ll.framework.ioc.annotations.Service;
import org.reflections.Reflections;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Class<?>> catalog = new HashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections r = new Reflections(basePackage);
        Set<Class<?>> comps = r.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> svcs = r.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> repos = r.getTypesAnnotatedWith(Repository.class);

        for (Class<?> c : comps) catalog.put(Introspector.decapitalize(c.getSimpleName()), c);
        for (Class<?> c : svcs) catalog.put(Introspector.decapitalize(c.getSimpleName()), c);
        for (Class<?> c : repos) catalog.put(Introspector.decapitalize(c.getSimpleName()), c);
    }

    @SuppressWarnings("unchecked")
    public <T> T genBean(String beanName) {
        String key = Introspector.decapitalize(beanName);

        Object cached = singletons.get(key);
        if (cached != null) return (T) cached;

        Class<?> type = catalog.get(key);
        if (type == null) throw new RuntimeException("빈을 찾을 수 없음: " + key);

        return null;
    }
}