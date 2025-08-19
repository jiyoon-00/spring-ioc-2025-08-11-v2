package com.ll.framework.ioc;

public class ApplicationContext {
    private final String basePackage;

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
    }

    @SuppressWarnings("unchecked")
    public <T> T genBean(String beanName) {
        return null;
    }
}