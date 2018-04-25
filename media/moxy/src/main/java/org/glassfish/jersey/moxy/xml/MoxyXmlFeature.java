/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.moxy.xml;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Feature used to register MOXy XML providers.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class MoxyXmlFeature implements Feature {

    private final Map<String, Object> properties;
    private final ClassLoader classLoader;
    private final boolean oxmMappingLookup;
    private final Class[] classes;

    /**
     * Default constructor creates standard {@link org.eclipse.persistence.jaxb.JAXBContext} without any activated features
     * and properties. Current context {@link ClassLoader} will be used.
     */
    public MoxyXmlFeature() {
        this(Collections.<String, Object>emptyMap(), Thread.currentThread().getContextClassLoader(), false);
    }

    /**
     * Constructor which allows MOXy {@link org.eclipse.persistence.jaxb.JAXBContext} customization.
     *
     * @param classes additional classes used for creating {@link org.eclipse.persistence.jaxb.JAXBContext}.
     */
    public MoxyXmlFeature(Class<?>... classes) {
        this(Collections.<String, Object>emptyMap(), Thread.currentThread().getContextClassLoader(), false, classes);
    }

    /**
     * Constructor which allows MOXy {@link org.eclipse.persistence.jaxb.JAXBContext} customization.
     *
     * @param properties       properties to be passed to
     *                         {@link org.eclipse.persistence.jaxb.JAXBContextFactory#createContext(Class[], java.util.Map,
     *                         ClassLoader)}. May be {@code null}.
     * @param classLoader      will be used to load classes. If {@code null}, current context {@link ClassLoader} will be used.
     * @param oxmMappingLookup if {@code true}, lookup for file with custom mappings will be performed.
     * @param classes          additional classes used for creating {@link org.eclipse.persistence.jaxb.JAXBContext}.
     */
    public MoxyXmlFeature(Map<String, Object> properties, ClassLoader classLoader, boolean oxmMappingLookup, Class... classes) {
        this.properties = (properties == null ? Collections.<String, Object>emptyMap() : properties);
        this.classLoader = (classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader);
        this.oxmMappingLookup = oxmMappingLookup;
        this.classes = classes;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new MoxyContextResolver(properties, classLoader, oxmMappingLookup, classes));
        return true;
    }
}
