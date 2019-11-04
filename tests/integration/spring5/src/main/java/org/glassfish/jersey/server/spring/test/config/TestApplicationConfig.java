/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.test.config;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.test.AccountService;
import org.glassfish.jersey.server.spring.test.AccountServiceImpl;
import org.glassfish.jersey.server.spring.test.HK2ServicePerLookup;
import org.glassfish.jersey.server.spring.test.HK2ServiceRequestScoped;
import org.glassfish.jersey.server.spring.test.HK2ServiceSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CustomAutowireConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestScope;

import javax.inject.Singleton;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Configuration
@ComponentScan(basePackages = {"org.glassfish.jersey.server.spring.test.resource"})
public class TestApplicationConfig extends ResourceConfig {

    public TestApplicationConfig() {
        packages(TestApplicationConfig.class.getAnnotation(ComponentScan.class).basePackages());
        Binder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                //HK2 Bean registration
                bindAsContract(HK2ServiceSingleton.class).in(Singleton.class);
                bindAsContract(HK2ServiceRequestScoped.class).in(RequestScoped.class);
                bindAsContract(HK2ServicePerLookup.class).in(PerLookup.class);
            }
        };
        register(binder);
    }

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer bean = new CustomScopeConfigurer();
        bean.addScope(WebApplicationContext.SCOPE_REQUEST, new RequestScope());
        return bean;
    }

    @Bean
    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
        AutowiredAnnotationBeanPostProcessor bean = new AutowiredAnnotationBeanPostProcessor();
        bean.setAutowiredAnnotationTypes(new HashSet<>(Arrays.asList(Autowired.class, Value.class)));
        return bean;
    }

    @Bean
    public static CustomAutowireConfigurer customAutowireConfigurer() {
        CustomAutowireConfigurer bean = new CustomAutowireConfigurer();
        bean.setCustomQualifierTypes(Collections.singleton(Qualifier.class));
        return bean;
    }

    @Bean(name = "AccountService-singleton")
    public AccountService accountService() {
        AccountService bean = new AccountServiceImpl();
        return bean;
    }

    @Bean(name = "AccountService-request-1")
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccountService accountService1() {
        AccountService bean = new AccountServiceImpl();
        return bean;
    }

    @Bean(name = "AccountService-request-2")
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccountService accountService2() {
        AccountService bean = new AccountServiceImpl();
        return bean;
    }

    @Bean(name = "AccountService-prototype-1")
    @Scope("prototype")
    public AccountService accountServicePrototype() {
        AccountServiceImpl bean = new AccountServiceImpl();
        bean.setDefaultAccountBalance("987.65");
        return bean;
    }

    @Bean(name = "AccountService-prototype-2")
    @Scope("prototype")
    public AccountService accountServicePrototype2() {
        AccountServiceImpl bean = new AccountServiceImpl();
        return bean;
    }


}
