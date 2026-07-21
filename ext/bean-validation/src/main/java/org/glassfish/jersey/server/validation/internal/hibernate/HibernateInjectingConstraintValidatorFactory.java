/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

package org.glassfish.jersey.server.validation.internal.hibernate;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.glassfish.jersey.ext.cdi1x.internal.CdiUtil;
import org.hibernate.validator.constraintvalidation.spi.DefaultConstraintValidatorFactory;

/**
 * A {@link ConstraintValidatorFactory} which enables CDI based dependency injection for the created
 * {@link ConstraintValidator}s.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class HibernateInjectingConstraintValidatorFactory extends DefaultConstraintValidatorFactory {

    // TODO look for something with better performance (HF)
    private final Map<Object, DestructibleBeanInstance<?>> constraintValidatorMap = Collections
            .synchronizedMap(new IdentityHashMap<Object, DestructibleBeanInstance<?>>());

    private BeanManager beanManager;

    @PostConstruct
    void postConstruct() {
        this.beanManager = CdiUtil.getBeanManager();
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        if (isBuiltInConstraintValidator(key)) {
            return super.getInstance(key);
        }
        DestructibleBeanInstance<T> destructibleBeanInstance = new DestructibleBeanInstance<T>(beanManager, key);
        constraintValidatorMap.put(destructibleBeanInstance.getInstance(), destructibleBeanInstance);
        return destructibleBeanInstance.getInstance();
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        DestructibleBeanInstance<?> destructibleBeanInstance = constraintValidatorMap.remove(instance);
        // HV-865 (Cleanup is multi threaded and instances can be removed by multiple threads. Explicit null check is needed)
        if (destructibleBeanInstance != null) {
            destructibleBeanInstance.destroy();
        }
    }

    public static boolean isBuiltInConstraintValidator(Class<?> klass) {
        return klass.getPackageName().startsWith("org.hibernate.validator.internal.constraintvalidators.");
    }
}
