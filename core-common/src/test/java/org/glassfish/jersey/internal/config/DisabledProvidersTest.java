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

package org.glassfish.jersey.internal.config;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.message.internal.RenderedImageProvider;
import org.glassfish.jersey.message.internal.SourceProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.RuntimeType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DisabledProvidersTest {
    private static class DisabledProvidersChecker extends MessagingBinders.MessageBodyProviders {
        private HashSet<Class<?>> bindSet = new HashSet<>();
        public DisabledProvidersChecker(Map<String, Object> applicationProperties, RuntimeType runtimeType) {
            super(applicationProperties, runtimeType);
        }

        @Override
        public <T> ClassBinding<T> bind(Class<T> serviceType) {
            bindSet.add(serviceType);
            return super.bind(serviceType);
        }

        @Override
        public void configure() {
            super.configure();
        }
    }

    @Test
    public void testNoRenderedImageProviderNoSourceProvider() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CommonProperties.PROVIDER_DEFAULT_DISABLE, "RENDEREDIMAGE, SOURCE");

        DisabledProvidersChecker checker = new DisabledProvidersChecker(properties, RuntimeType.CLIENT);
        checker.configure();
        Assert.assertFalse(checker.bindSet.contains(RenderedImageProvider.class));
        Assert.assertFalse(checker.bindSet.contains(SourceProvider.SourceWriter.class));
        Assert.assertTrue(checker.bindSet.contains(SourceProvider.StreamSourceReader.class));
        Assert.assertTrue(checker.bindSet.contains(SourceProvider.SaxSourceReader.class));
        Assert.assertTrue(checker.bindSet.contains(SourceProvider.DomSourceReader.class));
    }

    @Test
    public void testNoDisabledProvider() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CommonProperties.PROVIDER_DEFAULT_DISABLE, "ALL");

        DisabledProvidersChecker checker = new DisabledProvidersChecker(properties, RuntimeType.CLIENT);
        checker.configure();
        Assert.assertFalse(checker.bindSet.contains(RenderedImageProvider.class));
        Assert.assertFalse(checker.bindSet.contains(SourceProvider.StreamSourceReader.class));
        Assert.assertFalse(checker.bindSet.contains(SourceProvider.SourceWriter.class));
        Assert.assertFalse(checker.bindSet.contains(SourceProvider.SaxSourceReader.class));
        Assert.assertFalse(checker.bindSet.contains(SourceProvider.DomSourceReader.class));
    }


}
