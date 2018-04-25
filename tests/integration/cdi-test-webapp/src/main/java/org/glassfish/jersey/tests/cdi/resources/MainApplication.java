/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application to configure resources.
 *
 * @author Jonathan Benoit (jonathan.benoit at oracle.com)
 */
@ApplicationPath("main")
@ApplicationScoped
public class MainApplication extends Application {

    static AtomicInteger postConstructCounter = new AtomicInteger();

    @Inject BeanManager bm;

    private static final Logger LOGGER = Logger.getLogger(MainApplication.class.getName());

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(JCDIBeanDependentResource.class);
        classes.add(JDCIBeanException.class);
        classes.add(JDCIBeanDependentException.class);
        classes.add(JCDIBeanSingletonResource.class);
        classes.add(JCDIBeanPerRequestResource.class);
        classes.add(JCDIBeanExceptionMapper.class);
        classes.add(JCDIBeanDependentSingletonResource.class);
        classes.add(JCDIBeanDependentPerRequestResource.class);
        classes.add(JCDIBeanDependentExceptionMapper.class);
        classes.add(StutteringEchoResource.class);
        classes.add(StutteringEcho.class);
        classes.add(ReversingEchoResource.class);
        classes.add(CounterResource.class);
        classes.add(ConstructorInjectedResource.class);
        classes.add(ProducerResource.class);
        classes.add(FirstNonJaxRsBeanInjectedResource.class);
        return classes;
    }

    // JERSEY-2531: make sure this type gets managed by CDI
    @PostConstruct
    public void postConstruct() {
        LOGGER.info(String.format("%s: POST CONSTRUCT.", this.getClass().getName()));
        postConstructCounter.incrementAndGet();
        if (bm == null) {
            throw new IllegalStateException("BeanManager should have been injected into a CDI managed bean.");
        }
        if (postConstructCounter.intValue() > 1) {
            throw new IllegalStateException("postConstruct should have been invoked only once on app scoped bean.");
        }
    }

    @PreDestroy
    public void preDestroy() {
        LOGGER.info(String.format("%s: PRE DESTROY.", this.getClass().getName()));
    }
}
