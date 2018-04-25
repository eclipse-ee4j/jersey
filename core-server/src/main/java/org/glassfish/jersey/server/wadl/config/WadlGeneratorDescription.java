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

package org.glassfish.jersey.server.wadl.config;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.glassfish.jersey.server.wadl.WadlGenerator;

/**
 * This is the model for the definition of wadl generators via configuration properties.<br />
 * The properties refer to the properties of the {@link WadlGenerator} implementation with
 * the specified {@link WadlGeneratorDescription#getGeneratorClass()}. The {@link WadlGenerator} properties
 * are populated with the provided properties like this:
 * <ul>
 * <li>The types match exactly:<br/>if the WadlGenerator property is of type <code>org.example.Foo</code> and the
 * provided property value is of type <code>org.example.Foo</code></li>
 * <li>Types that provide a constructor for the provided type (mostly java.lang.String)</li>
 * <li>The WadlGenerator property is of type {@link InputStream}: The stream is loaded from the
 * property value (provided by the {@link WadlGeneratorDescription}) via
 * {@link ClassLoader#getResourceAsStream(String)}. It will be closed after {@link WadlGenerator#init()} was called.
 * </li>
 *
 * <li><strong>Deprecated, will be removed in future versions:</strong><br/>
 * The WadlGenerator property is of type {@link File} and the provided property value is a {@link String}:<br/>
 * the provided property value can contain the prefix <em>classpath:</em> to denote, that the
 * path to the file is relative to the classpath. In this case, the property value is stripped by
 * the prefix <em>classpath:</em> and the {@link File} is created via
 * <pre><code>new File( generator.getClass().getResource( strippedFilename ).toURI() )</code></pre>
 * Notice that the filename is loaded from the classpath in this case, e.g. <em>classpath:test.xml</em>
 * refers to a file in the package of the class ({@link WadlGeneratorDescription#getGeneratorClass()}). The
 * file reference <em>classpath:/test.xml</em> refers to a file that is in the root of the classpath.
 * </li>
 *
 * </ul>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class WadlGeneratorDescription {

    private Class<? extends WadlGenerator> generatorClass;
    private Properties properties;
    private Class<?> configuratorClass;

    public WadlGeneratorDescription() {
    }

    public WadlGeneratorDescription(Class<? extends WadlGenerator> generatorClass, Properties properties) {
        this.generatorClass = generatorClass;
        this.properties = properties;
    }

    /**
     * @return the generatorClass
     */
    public Class<? extends WadlGenerator> getGeneratorClass() {
        return generatorClass;
    }
    /**
     * @param generatorClass the generatorClass to set
     */
    public void setGeneratorClass(Class<? extends WadlGenerator> generatorClass) {
        this.generatorClass = generatorClass;
    }
    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }
    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return {@link WadlGeneratorConfig} that was used to produce current description instance.
     * The result could be null if the config was not set on this instance.
     *
     * @return config
     */
    public Class<?> getConfiguratorClass() {
        return configuratorClass;
    }

    /**
     * Set {@link WadlGeneratorConfig} class to be associated with current instance.
     *
     * @param configuratorClass
     */
    void setConfiguratorClass(Class<?> configuratorClass) {
        this.configuratorClass = configuratorClass;
    }
}
