package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
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
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Object injectedBean : beans) {
            for (Field field : fields) {
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
    }

    private Object create(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> bean.getClass().equals(aClass))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }
}
