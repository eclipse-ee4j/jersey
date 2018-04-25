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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ext.ContextResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

/**
 * {@link ContextResolver} implementation which creates MOXy {@link JAXBContext}.
 *
 * TODO: deal with classes NOT annotated with @XmlRootElement/@XmlType
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class MoxyContextResolver implements ContextResolver<JAXBContext> {

    private static final Logger LOGGER = Logger.getLogger(MoxyContextResolver.class.getName());
    private static final String MOXY_OXM_MAPPING_FILE_NAME = "eclipselink-oxm.xml";

    private final boolean oxmMappingLookup;
    private final Map<String, Object> properties;
    private final ClassLoader classLoader;
    private final Class[] classes;


    /**
     * Default constructor creates standard {@link JAXBContext} without any activated features
     * and properties. Current context {@link ClassLoader} will be used.
     */
    public MoxyContextResolver() {
        this(Collections.<String, Object>emptyMap(), Thread.currentThread().getContextClassLoader(), false);
    }

    /**
     * Constructor which allows MOXy {@link JAXBContext} customization.
     *
     * @param properties       properties to be passed to
     *                         {@link JAXBContextFactory#createContext(Class[], java.util.Map, ClassLoader)}. May be {@code null}.
     * @param classLoader      will be used to load classes. If {@code null}, current context {@link ClassLoader} will be used.
     * @param oxmMappingLookup if {@code true}, lookup for file with custom mappings will be performed.
     * @param classes          additional classes used for creating {@link org.eclipse.persistence.jaxb.JAXBContext}.
     */
    public MoxyContextResolver(
            Map<String, Object> properties,
            ClassLoader classLoader,
            boolean oxmMappingLookup,
            Class... classes) {
        this.properties = properties == null ? Collections.<String, Object>emptyMap() : properties;
        this.classLoader = (classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader);
        this.oxmMappingLookup = oxmMappingLookup;
        this.classes = classes;
    }

    @Override
    public JAXBContext getContext(Class<?> type) {
        Map<String, Object> propertiesCopy = new HashMap<String, Object>(properties);

        if (oxmMappingLookup) {
            final InputStream eclipseLinkOxm = type.getResourceAsStream(MOXY_OXM_MAPPING_FILE_NAME);
            if (eclipseLinkOxm != null && !propertiesCopy.containsKey(JAXBContextProperties.OXM_METADATA_SOURCE)) {
                propertiesCopy.put(JAXBContextProperties.OXM_METADATA_SOURCE, eclipseLinkOxm);
            }
        }

        final Class[] typeArray;
        if (classes != null && classes.length > 0) {
            typeArray = new Class[1 + classes.length];
            System.arraycopy(classes, 0, typeArray, 0, classes.length);
            typeArray[typeArray.length - 1] = type;
        } else {
            typeArray = new Class[]{type};
        }

        try {
            final JAXBContext context = JAXBContextFactory.createContext(typeArray, propertiesCopy, classLoader);
            LOGGER.log(Level.FINE, "Using JAXB context " + context);
            return context;
        } catch (JAXBException e) {
            LOGGER.fine("Unable to create JAXB context.");
            return null;
        }
    }
}
