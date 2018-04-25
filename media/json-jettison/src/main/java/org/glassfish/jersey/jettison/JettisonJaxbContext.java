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

package org.glassfish.jersey.jettison;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.glassfish.jersey.jettison.internal.BaseJsonMarshaller;
import org.glassfish.jersey.jettison.internal.BaseJsonUnmarshaller;
import org.glassfish.jersey.jettison.internal.JettisonJaxbMarshaller;
import org.glassfish.jersey.jettison.internal.JettisonJaxbUnmarshaller;

/**
 * An adaption of {@link javax.xml.bind.JAXBContext} that supports marshalling
 * and unmarshalling of JAXB beans using the JSON format.
 * <p>
 * The JSON format may be configured by using a {@link JettisonConfig} object
 * as a constructor parameter of this class.
 */
public final class JettisonJaxbContext extends JAXBContext implements JettisonConfigured {

    private JettisonConfig jsonConfiguration;
    private final JAXBContext jaxbContext;

    /**
     * Constructs a new instance with default {@link JettisonConfig}.
     *
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JsonJaxbContext. Can be empty, in which case a JsonJaxbContext
     *        that only knows about spec-defined classes will be returned.
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(Class... classesToBeBound) throws JAXBException {
        this(JettisonConfig.DEFAULT, classesToBeBound);
    }

    /**
     * Constructs a new instance with given {@link JettisonConfig}.
     *
     * @param config {@link JettisonConfig}, can not be null
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JsonJaxbContext. Can be empty, in which case a JsonJaxbContext
     *        that only knows about spec-defined classes will be returned.
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(final JettisonConfig config, final Class... classesToBeBound) throws JAXBException {
        if (config == null) {
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        }

        jsonConfiguration = config;
        jaxbContext = JAXBContext.newInstance(classesToBeBound);
    }

    /**
     * Constructs a new instance with a custom set of properties.
     * The default {@link JettisonConfig} is used if no (now deprecated)
     * JSON related properties are specified
     *
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JsonJaxbContext. Can be empty, in which case a JsonJaxbContext
     *        that only knows about spec-defined classes will be returned.
     * @param properties the custom set of properties. If it contains(now deprecated) JSON related properties,
     *                  then a non-default {@link JettisonConfig} is used reflecting the JSON properties
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(Class[] classesToBeBound, Map<String, Object> properties)
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(classesToBeBound, properties);
        if (jsonConfiguration == null) {
            jsonConfiguration = JettisonConfig.DEFAULT;
        }
    }

    /**
     * Constructs a new instance with a custom set of properties.
     * If no (now deprecated) JSON related properties are specified,
     * the {@link JettisonConfig#DEFAULT} is used as
     * {@link JettisonConfig}
     *
     * @param config {@link JettisonConfig}, can not be null
     * @param classesToBeBound list of java classes to be recognized by the
     *        new JsonJaxbContext. Can be empty, in which case a JsonJaxbContext
     *        that only knows about spec-defined classes will be returned.
     * @param properties the custom set of properties.
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(final JettisonConfig config, final Class[] classesToBeBound, final Map<String,
            Object> properties)
            throws JAXBException {
        if (config == null) {
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        }

        jsonConfiguration = config;
        jaxbContext = JAXBContext.newInstance(classesToBeBound, properties);
    }

    /**
     * Construct a new instance of using context class loader of the thread
     * with default {@link JettisonConfig}.
     *
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(String contextPath)
            throws JAXBException {
        this(JettisonConfig.DEFAULT, contextPath);
    }

    /**
     * Construct a new instance of using context class loader of the thread
     * with given {@link JettisonConfig}.
     *
     * @param config {@link JettisonConfig}, can not be null
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(JettisonConfig config, String contextPath)
            throws JAXBException {
        if (config == null) {
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        }

        jaxbContext = JAXBContext.newInstance(contextPath, Thread.currentThread().getContextClassLoader());
        jsonConfiguration = config;
    }

    /**
     * Construct a new instance using a specified class loader with
     * default  {@link JettisonConfig}.
     *
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(String contextPath, ClassLoader classLoader)
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(contextPath, classLoader);
        jsonConfiguration = JettisonConfig.DEFAULT;
    }

    /**
     * Construct a new instance using a specified class loader and
     * a custom set of properties. {@link JettisonConfig} is set to default,
     * if user does not specify any (now deprecated) JSON related properties
     *
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader
     * @param properties the custom set of properties.
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(String contextPath, ClassLoader classLoader, Map<String, Object> properties)
            throws JAXBException {
        jaxbContext = JAXBContext.newInstance(contextPath, classLoader, properties);
        if (jsonConfiguration == null) {
            jsonConfiguration = JettisonConfig.DEFAULT;
        }
    }

    /**
     * Construct a new instance using a specified class loader,
     * set of properties and {@link JettisonConfig} .
     *
     * @param config {@link JettisonConfig}, can not be null
     * @param contextPath list of java package names that contain schema
     *        derived class and/or java to schema (JAXB-annotated) mapped
     *        classes
     * @param classLoader
     * @param properties the custom set of properties.
     * @throws javax.xml.bind.JAXBException if an error was encountered while creating the
     *         underlying JAXBContext.
     */
    public JettisonJaxbContext(JettisonConfig config, String contextPath, ClassLoader classLoader, Map<String, Object> properties)
            throws JAXBException {
        if (config == null) {
            throw new IllegalArgumentException("JSONConfiguration MUST not be null");
        }

        jaxbContext = JAXBContext.newInstance(contextPath, classLoader, properties);
        jsonConfiguration = config;
    }

    /**
     * Get a {@link org.glassfish.jersey.jettison.JettisonMarshaller} from a {@link javax.xml.bind.Marshaller}.
     *
     * @param marshaller the JAXB marshaller.
     * @return the JSON marshaller.
     */
    public static org.glassfish.jersey.jettison.JettisonMarshaller getJSONMarshaller(Marshaller marshaller) {
        if (marshaller instanceof org.glassfish.jersey.jettison.JettisonMarshaller) {
            return (org.glassfish.jersey.jettison.JettisonMarshaller) marshaller;
        } else {
            return new BaseJsonMarshaller(marshaller, JettisonConfig.DEFAULT);
        }

    }

    /**
     * Get a {@link org.glassfish.jersey.jettison.JettisonUnmarshaller} from a {@link javax.xml.bind.Unmarshaller}.
     *
     * @param unmarshaller the JAXB unmarshaller.
     * @return the JSON unmarshaller.
     */
    public static org.glassfish.jersey.jettison.JettisonUnmarshaller getJSONUnmarshaller(Unmarshaller unmarshaller) {
        if (unmarshaller instanceof org.glassfish.jersey.jettison.JettisonUnmarshaller) {
            return (org.glassfish.jersey.jettison.JettisonUnmarshaller) unmarshaller;
        } else {
            return new BaseJsonUnmarshaller(unmarshaller, JettisonConfig.DEFAULT);
        }

    }

    /**
     * Get the JSON configuration.
     *
     * @return the JSON configuration.
     */
    public JettisonConfig getJSONConfiguration() {
        return jsonConfiguration;
    }

    /**
     * Create a JSON unmarshaller.
     *
     * @return the JSON unmarshaller
     *
     * @throws javax.xml.bind.JAXBException if there is an error creating the unmarshaller.
     */
    public org.glassfish.jersey.jettison.JettisonUnmarshaller createJsonUnmarshaller() throws JAXBException {
        return new JettisonJaxbUnmarshaller(this, getJSONConfiguration());
    }

    /**
     * Create a JSON marshaller.
     *
     * @return the JSON marshaller.
     *
     * @throws javax.xml.bind.JAXBException if there is an error creating the marshaller.
     */
    public org.glassfish.jersey.jettison.JettisonMarshaller createJsonMarshaller() throws JAXBException {
        return new JettisonJaxbMarshaller(this, getJSONConfiguration());
    }

    /**
     * Overrides underlying createUnmarshaller method and returns
     * an unmarshaller which is capable of JSON deserialization.
     *
     * @return unmarshaller instance with JSON capabilities
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        return new JettisonJaxbUnmarshaller(jaxbContext, getJSONConfiguration());
    }

    /**
     * Overrides underlaying createMarshaller method and returns
     * a marshaller which is capable of JSON serialization.
     *
     * @return marshaller instance with JSON capabilities
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        return new JettisonJaxbMarshaller(jaxbContext, getJSONConfiguration());
    }

    /**
     * Simply delegates to underlying JAXBContext implementation.
     *
     * @return what underlying JAXBContext returns
     * @throws javax.xml.bind.JAXBException
     */
    @Override
    public Validator createValidator() throws JAXBException {
        return jaxbContext.createValidator();
    }
}
