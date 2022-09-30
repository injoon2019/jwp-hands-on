package nextstep.study.di.stage4.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContext {

    private final Set<Object> beans;

    public DIContext(final Set<Class<?>> classes) {
        Set<Object> beans = classes.stream()
                .map(this::create)
                .collect(Collectors.toSet());
        beans.forEach(bean -> injectDependency(bean, beans));
        this.beans = beans;
    }

    private void injectDependency(Object bean, Set<Object> beans) {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                inject(bean, beans, field);
            }
        }
    }

    private void inject(Object bean, Set<Object> beans, Field field) {
        for (Object injectedBean : beans) {
            if (field.getType().isInstance(injectedBean)) {
                field.setAccessible(true);
                try {
                    field.set(bean, injectedBean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }
    }

    private Object create(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static DIContext createContextForPackage(final String rootPackageName) {
        Set<Class<?>> classes = ClassPathScanner.getAllClassesInPackage(rootPackageName);
        return new DIContext(classes);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> bean.getClass().equals(aClass))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
