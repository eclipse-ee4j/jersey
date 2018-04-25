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

package org.glassfish.jersey.media.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Injectable JavaBean containing the configuration parameters for
 * {@code jersey-multipart} as used in this particular application.
 *
 * @author Craig McClanahan
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
@PropertiesClass
public class MultiPartProperties {

    /**
     * Default threshold size for buffer.
     */
    public static final int DEFAULT_BUFFER_THRESHOLD = 4096;

    /**
     * Name of a properties resource that (if found in the classpath
     * for this application) will be used to configure the settings returned
     * by our getter methods.
     */
    public static final String MULTI_PART_CONFIG_RESOURCE = "jersey-multipart-config.properties";

    /**
     * Name of the resource property for the threshold size (in bytes) above which a body part entity will be
     * buffered to disk instead of being held in memory.
     *
     * The default value is {@value #DEFAULT_BUFFER_THRESHOLD}.
     */
    public static final String BUFFER_THRESHOLD = "jersey.config.multipart.bufferThreshold";

    /**
     * The {@link #BUFFER_THRESHOLD} property value to keep a body part entity in memory only.
     */
    public static final int BUFFER_THRESHOLD_MEMORY_ONLY = -1;

    /**
     * Name of the resource property for the directory to store temporary files containing body parts of multipart message that
     * extends allowed memory threshold..
     *
     * The default value is not set (will be taken from {@code java.io.tmpdir} system property).
     */
    public static final String TEMP_DIRECTORY = "jersey.config.multipart.tempDir";

    /**
     * The threshold size (in bytes) above which a body part entity will be
     * buffered to disk instead of being held in memory.
     */
    private int bufferThreshold = DEFAULT_BUFFER_THRESHOLD;

    /**
     * Directory to store temporary files containing body parts of multipart message that extends allowed memory threshold.
     */
    private String tempDir = null;

    /**
     * Load and customize (if necessary) the configuration values for the
     * {@code jersey-multipart} injection binder.
     *
     * @throws IllegalArgumentException if the configuration resource
     *                                  exists, but there are problems reading it
     */
    public MultiPartProperties() {
        configure();
    }

    /**
     * Get the size (in bytes) of the entity of an incoming
     * {@link BodyPart} before it will be buffered to disk.  If not
     * customized, the default value is 4096.
     *
     * @return return threshold size for starting to buffer the incoming entity
     *         to disk.
     */
    public int getBufferThreshold() {
        return bufferThreshold;
    }

    /**
     * Get the directory to store temporary files containing body parts of multipart message that extends allowed memory
     * threshold.
     *
     * @return path to the temporary directory.
     * @since 2.4.1
     */
    public String getTempDir() {
        return tempDir;
    }

    /**
     * Set the size (in bytes) of the entity of an incoming {@link BodyPart} before it will be buffered to disk.
     *
     * @param threshold size of body part.
     * @return {@code MultiPartProperties} instance.
     * @since 2.4.1
     */
    public MultiPartProperties bufferThreshold(final int threshold) {
        this.bufferThreshold = threshold < BUFFER_THRESHOLD_MEMORY_ONLY ? BUFFER_THRESHOLD_MEMORY_ONLY : threshold;
        return this;
    }

    /**
     * Set the path to the directory to store temporary files containing body parts of multipart message that extends allowed
     * memory threshold.
     *
     * @param path path to the temporary directory.
     * @return {@code MultiPartProperties} instance.
     * @since 2.4.1
     */
    public MultiPartProperties tempDir(final String path) {
        this.tempDir = path;
        return this;
    }

    /**
     * Configure the values returned by this instance's getters based on
     * the contents of a properties resource, if it exists on the classpath
     * for this application.
     *
     * @throws IllegalArgumentException if the configuration resource
     *                                  exists, but there are problems reading it
     */
    private void configure() {
        // Identify the class loader we will use
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = this.getClass().getClassLoader();
        }

        // Attempt to find our properties resource
        InputStream stream = null;
        try {
            stream = loader.getResourceAsStream(MULTI_PART_CONFIG_RESOURCE);
            if (stream == null) {
                return;
            }
            final Properties props = new Properties();
            props.load(stream);

            if (props.containsKey(BUFFER_THRESHOLD)) {
                this.bufferThreshold = Integer.parseInt(props.getProperty(BUFFER_THRESHOLD));
            }
            if (props.containsKey(TEMP_DIRECTORY)) {
                this.tempDir = props.getProperty(TEMP_DIRECTORY);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (final IOException e) {
                    // Pass through
                }
            }
        }
    }

    /**
     * Create a {@link ContextResolver context resolver} for a current state of this {@code MultiPartProperties}.
     *
     * @return context resolver for this config.
     * @since 2.4.1
     */
    public ContextResolver<MultiPartProperties> resolver() {
        return new ContextResolver<MultiPartProperties>() {

            @Override
            public MultiPartProperties getContext(final Class<?> type) {
                return MultiPartProperties.this;
            }
        };
    }
}
