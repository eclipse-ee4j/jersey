/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server.spring;

import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.spi.ComponentProvider;

import org.jvnet.hk2.spring.bridge.api.SpringBridge;
import org.jvnet.hk2.spring.bridge.api.SpringIntoHK2Bridge;

import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Custom ComponentProvider class.
 * Responsible for 1) bootstrapping Jersey 2 Spring integration and
 * 2) making Jersey skip JAX-RS Spring component life-cycle management and leave it to us.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class SpringComponentProvider implements ComponentProvider {

    private static final Logger LOGGER = Logger.getLogger(SpringComponentProvider.class.getName());
    private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "applicationContext.xml";
    private static final String PARAM_CONTEXT_CONFIG_LOCATION = "contextConfigLocation";
    private static final String PARAM_SPRING_CONTEXT = "contextConfig";

    private volatile InjectionManager injectionManager;
    private volatile ApplicationContext ctx;

    @Override
    public void initialize(InjectionManager injectionManager) {
        this.injectionManager = injectionManager;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(LocalizationMessages.CTX_LOOKUP_STARTED());
        }

        ServletContext sc = injectionManager.getInstance(ServletContext.class);

        if (sc != null) {
            // servlet container
            ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
        } else {
            // non-servlet container
            ctx = createSpringContext();
        }
        if (ctx == null) {
            LOGGER.severe(LocalizationMessages.CTX_LOOKUP_FAILED());
            return;
        }
        LOGGER.config(LocalizationMessages.CTX_LOOKUP_SUCESSFUL());

        // initialize HK2 spring-bridge

        ImmediateHk2InjectionManager hk2InjectionManager = (ImmediateHk2InjectionManager) injectionManager;
        SpringBridge.getSpringBridge().initializeSpringBridge(hk2InjectionManager.getServiceLocator());
        SpringIntoHK2Bridge springBridge = injectionManager.getInstance(SpringIntoHK2Bridge.class);
        springBridge.bridgeSpringBeanFactory(ctx);

        injectionManager.register(Bindings.injectionResolver(new AutowiredInjectResolver(ctx)));
        injectionManager.register(Bindings.service(ctx).to(ApplicationContext.class).named("SpringContext"));
        LOGGER.config(LocalizationMessages.SPRING_COMPONENT_PROVIDER_INITIALIZED());
    }

    // detect JAX-RS classes that are also Spring @Components.
    // register these with HK2 ServiceLocator to manage their lifecycle using Spring.
    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {

        if (ctx == null) {
            return false;
        }

        if (AnnotationUtils.findAnnotation(component, Component.class) != null) {
            String[] beanNames = ctx.getBeanNamesForType(component);
            if (beanNames == null || beanNames.length != 1) {
                LOGGER.severe(LocalizationMessages.NONE_OR_MULTIPLE_BEANS_AVAILABLE(component));
                return false;
            }
            String beanName = beanNames[0];

            Binding binding = Bindings.supplier(new SpringManagedBeanFactory(ctx, injectionManager, beanName))
                    .to(component)
                    .to(providerContracts);
            injectionManager.register(binding);

            LOGGER.config(LocalizationMessages.BEAN_REGISTERED(beanNames[0]));
            return true;
        }
        return false;
    }

    @Override
    public void done() {
    }

    private ApplicationContext createSpringContext() {
        ApplicationHandler applicationHandler = injectionManager.getInstance(ApplicationHandler.class);
        ApplicationContext springContext = (ApplicationContext) applicationHandler.getConfiguration()
                .getProperty(PARAM_SPRING_CONTEXT);
        if (springContext == null) {
            String contextConfigLocation = (String) applicationHandler.getConfiguration()
                    .getProperty(PARAM_CONTEXT_CONFIG_LOCATION);
            springContext = createXmlSpringConfiguration(contextConfigLocation);
        }
        return springContext;
    }

    private ApplicationContext createXmlSpringConfiguration(String contextConfigLocation) {
        if (contextConfigLocation == null) {
            contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
        }
        return ctx = new ClassPathXmlApplicationContext(contextConfigLocation, "jersey-spring-applicationContext.xml");
    }

    private static class SpringManagedBeanFactory implements Supplier {

        private final ApplicationContext ctx;
        private final InjectionManager injectionManager;
        private final String beanName;

        private SpringManagedBeanFactory(ApplicationContext ctx, InjectionManager injectionManager, String beanName) {
            this.ctx = ctx;
            this.injectionManager = injectionManager;
            this.beanName = beanName;
        }

        @Override
        public Object get() {
            Object bean = ctx.getBean(beanName);
            if (bean instanceof Advised) {
                try {
                    // Unwrap the bean and inject the values inside of it
                    Object localBean = ((Advised) bean).getTargetSource().getTarget();
                    injectionManager.inject(localBean);
                } catch (Exception e) {
                    // Ignore and let the injection happen as it normally would.
                    injectionManager.inject(bean);
                }
            } else {
                injectionManager.inject(bean);
            }
            return bean;
        }
    }
}
