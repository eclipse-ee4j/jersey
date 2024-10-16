/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.skippinganalyzer;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.ext.cdi1x.internal.InjecteeSkippingAnalyzer;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class SkippingAnalyzerTest {
    private Weld weld;

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @BeforeEach
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld = new Weld();
            weld.initialize();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        weld.shutdown();
    }

    @Test
    public void testInjecteeSkippingAnalyzerWithZeroFieldsToSkip() throws Exception {
        BeanManager beanManager = CDI.current().getBeanManager();
        CdiComponentProvider provider = beanManager.getExtension(CdiComponentProvider.class);
        Method method = provider.getClass().getDeclaredMethod("getFieldsToSkip");
        method.setAccessible(true);
        Map fieldMap = (Map) method.invoke(provider);
        MatcherAssert.assertThat(0, Matchers.is(fieldMap.size()));

        InjectionManager injectionManager = Injections.createInjectionManager();
        provider.initialize(injectionManager);
        injectionManager.completeRegistration();
        ClassAnalyzer analyzer = injectionManager.getInstance(ClassAnalyzer.class, CdiComponentProvider.CDI_CLASS_ANALYZER);
        MatcherAssert.assertThat(InjecteeSkippingAnalyzer.class, Matchers.is(analyzer.getClass()));

        Set<Field> fieldSet = analyzer.getFields(CdiServiceImpl.class);
        MatcherAssert.assertThat(0, Matchers.is(fieldSet.size()));
    }
}
