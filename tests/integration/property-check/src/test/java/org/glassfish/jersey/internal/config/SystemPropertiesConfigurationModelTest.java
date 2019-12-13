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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.Property;
import org.glassfish.jersey.jetty.connector.JettyClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartProperties;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.InternalServerProperties;
import org.glassfish.jersey.server.oauth1.OAuth1ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class SystemPropertiesConfigurationModelTest {

    private static final String ROOT_PACKAGE = "org.glassfish.jersey";
    private static final List<String> BLACK_LIST_CLASSES = Arrays.asList("org.glassfish.jersey.internal.OsgiRegistry",
        "org.glassfish.jersey.internal.jsr166.SubmissionPublisher", "org.glassfish.jersey.internal.jsr166.Flow");

    @Test
    public void allPropertyClassLoaded() throws IOException {
        /*
         *  It doesn't work for higher JDKs because it is using different classloader
         *  (jdk.internal.loader.ClassLoaders$AppClassLoader) that current Guava version doesn't support.
         */
        if (JdkVersion.getJdkVersion().getMajor() == 8) {
            SystemPropertiesConfigurationModel model = new SystemPropertiesConfigurationModel();
            Predicate<Class<?>> containsAnnotation = clazz -> clazz.getAnnotation(PropertiesClass.class) != null
                    || clazz.getAnnotation(Property.class) != null;
            Predicate<Class<?>> notCommon = clazz -> clazz != CommonProperties.class;
            Predicate<Class<?>> notTestProperties = clazz -> clazz != TestProperties.class;
            List<String> propertyClasses = getClassesWithPredicate(ROOT_PACKAGE, notCommon, notTestProperties,
                    containsAnnotation).stream().map(Class::getName).collect(Collectors.toList());
            assertFalse(propertyClasses.isEmpty());
            propertyClasses.removeAll(SystemPropertiesConfigurationModel.PROPERTY_CLASSES);
            assertEquals("New properties have been found. "
                    + "Make sure you add next classes in SystemPropertiesConfigurationModel.PROPERTY_CLASSES: "
                    + propertyClasses, 0, propertyClasses.size());
        }
    }

    @Test
    public void propertyLoadedWhenSecurityException() {
        final String APP_NAME = "propertyLoadedWhenSecurityException";
        SecurityManager sm = System.getSecurityManager();
        String policy = System.getProperty("java.security.policy");
        try {
            System.setProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.TRUE.toString());
            System.setProperty(ServerProperties.APPLICATION_NAME, APP_NAME);
            SystemPropertiesConfigurationModel model = new SystemPropertiesConfigurationModel();
            assertTrue(model.as(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.class));
            String securityPolicy = SystemPropertiesConfigurationModelTest.class.getResource("/server.policy").getFile();
            System.setProperty("java.security.policy", securityPolicy);
            SecurityManager manager = new SecurityManager();
            System.setSecurityManager(manager);
            Map<String, Object> properties = model.getProperties();
            assertEquals(APP_NAME, properties.get(ServerProperties.APPLICATION_NAME));
            assertEquals(Boolean.TRUE.toString(), properties.get(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER));
            assertFalse(properties.containsKey(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE));
            assertFalse(properties.containsKey(ClientProperties.ASYNC_THREADPOOL_SIZE));
            assertFalse(properties.containsKey(ServletProperties.FILTER_CONTEXT_PATH));
            assertFalse(properties.containsKey(InternalProperties.JSON_FEATURE));
            assertFalse(properties.containsKey(MessageProperties.DEFLATE_WITHOUT_ZLIB));
            assertFalse(properties.containsKey(ApacheClientProperties.CONNECTION_MANAGER));
            assertFalse(properties.containsKey(JettyClientProperties.DISABLE_COOKIES));
            assertFalse(properties.containsKey(MultiPartProperties.BUFFER_THRESHOLD));
            assertFalse(properties.containsKey(OAuth1ServerProperties.ACCESS_TOKEN_URI));
        } finally {
            if (policy != null) {
                System.setProperty("java.security.policy", policy);
            }
            System.setSecurityManager(sm);
        }
    }

    private List<Class<?>> getClassesWithPredicate(String packageRoot, Predicate<Class<?>>... predicates)
            throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassPath classpath = ClassPath.from(loader);
        Stream<Class<?>> steam = classpath.getTopLevelClassesRecursive(packageRoot).stream()
                .filter(classInfo -> !BLACK_LIST_CLASSES.contains(classInfo.getName()))
                .map(classInfo -> {
                    try {
                        return Class.forName(classInfo.getName());
                    } catch (ClassNotFoundException e) {
                        return Void.class;
                    }
                });
        steam = steam.filter(Arrays.stream(predicates).reduce(x->true, Predicate::and));
        return steam.collect(Collectors.toList());
    }

}
