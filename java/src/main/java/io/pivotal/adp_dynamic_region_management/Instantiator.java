package io.pivotal.adp_dynamic_region_management;

import org.apache.commons.lang3.reflect.ConstructorUtils;

public class Instantiator<T> {
    private String className;

    public Instantiator(String className) {
        this.className = className;
    }

    public T instantiate() throws RegionOptionsInvalidException {
        return construct(getTheClass());
    }

    private T construct(Class<T> theClass) throws RegionOptionsInvalidException {
        try {
            return ConstructorUtils.invokeConstructor(theClass);
        } catch (Exception e) {
            throw new RegionOptionsInvalidException(
                    String.format("Unable to instantiate: `%s`.", className), e);
        }
    }

    private Class<T> getTheClass() throws RegionOptionsInvalidException {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RegionOptionsInvalidException(
                    String.format("Invalid class name: `%s`.", className), e);
        }
    }
}
