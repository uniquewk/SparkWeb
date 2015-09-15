package com.sparkweb.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sparkweb.exception.UnexpectedException;

/**
 * Parameters map to POJO binder.
 */
public class BeanWrapper 
{
    final static int notwritableField = Modifier.FINAL | Modifier.NATIVE | Modifier.STATIC;
    final static int notaccessibleMethod = Modifier.NATIVE | Modifier.STATIC;

    private Class<?> beanClass;

    /** 
     * a cache for our properties and setters
     */
    private Map<String, Property> wrappers = new HashMap<String, Property>();

    public BeanWrapper(Class<?> forClass) {
        this.beanClass = forClass;

        registerSetters(forClass);
        registerFields(forClass);
    }

    public Collection<Property> getWrappers() {
        return wrappers.values();
    }

    public void set(String name, Object instance, Object value) {
        for (Property prop : wrappers.values()) {
            if (name.equals(prop.name)) {
                prop.setValue(instance, value);
                return;
            }
        }
        String message = String.format("Can't find property with name '%s' on class %s", name, instance.getClass().getName());
        throw new UnexpectedException(message);

    }

    private boolean isSetter(Method method) {
        return (method.getName().startsWith("set") && method.getName().length() > 3 
        		&& method.getParameterTypes().length == 1 
        		&& (method.getModifiers() & notaccessibleMethod) == 0);
    }

    protected Object newBeanInstance() 
    		throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Constructor<?> constructor = beanClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void registerFields(Class<?> clazz) {
        // recursive stop condition
        if (clazz == Object.class) {
            return;
        }
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (wrappers.containsKey(field.getName())) {
                continue;
            }
            if ((field.getModifiers() & notwritableField) != 0) {
                continue;
            }
            Property w = new Property(field);
            wrappers.put(field.getName(), w);
        }
        registerFields(clazz.getSuperclass());
    }

    private void registerSetters(Class<?> clazz) {
        if (clazz == Object.class) {
            return;
            // deep walk (superclass first)
        }
        registerSetters(clazz.getSuperclass());

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!isSetter(method)) {
                continue;
            }
            
            String propertyname = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
            Property wrapper = new Property(propertyname, method);
            wrappers.put(propertyname, wrapper);
        }
    }

    public static class Property {

        private Annotation[] annotations;
        private Method setter;
        private Field field;
        private Class<?> type;
        private Type genericType;
        private String name;
        private String[] profiles;

        Property(String propertyName, Method setterMethod) {
            name = propertyName;
            setter = setterMethod;
            type = setter.getParameterTypes()[0];
            annotations = setter.getAnnotations();
            genericType = setter.getGenericParameterTypes()[0];
            setProfiles(this.annotations);
        }

        Property(Field field) {
            this.field = field;
            this.field.setAccessible(true);
            name = field.getName();
            type = field.getType();
            annotations = field.getAnnotations();
            genericType = field.getGenericType();
            setProfiles(this.annotations);
        }

        public void setProfiles(Annotation[] annotations) {
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(NoBinding.class)) {
                        NoBinding as = ((NoBinding) annotation);
                        profiles = as.value();
                    }
                }
            }
        }

        public void setValue(Object instance, Object value) {
            try {
                if (setter != null) {
                    setter.invoke(instance, value);
                    return;
                } else {
                    field.set(instance, value);
                }

            } catch (Exception ex) {
                throw new UnexpectedException(ex);
            }
        }

        String getName() {
            return name;
        }

        Class<?> getType() {
            return type;
        }

        Type getGenericType() {
            return genericType;
        }

        Annotation[] getAnnotations() {
            return annotations;
        }
        
        String[] getProfiles() {
        	return profiles;
        }

        @Override
        public String toString() {
            return type + "." + name;
        }


    }
    /*
    public Object bind(String name, Type type, Map<String, String[]> params, String prefix, Annotation[] annotations) throws Exception {
        Object instance = newBeanInstance();
        return bind(name, type, params, prefix, instance, annotations);
    }

    public Object bind(String name, Type type, Map<String, String[]> params, String prefix, Object instance, Annotation[] annotations) throws Exception {
        RootParamNode paramNode = RootParamNode.convert( params);
        // when looking at the old code in BeanBinder and Binder.bindInternal, I
        // think it is correct to use 'name+prefix'
        Binder.bindBean( paramNode.getChild(name+prefix), instance, annotations);
        return instance;
    }
    */
}
