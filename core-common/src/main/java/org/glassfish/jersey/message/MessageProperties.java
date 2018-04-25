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

package org.glassfish.jersey.message;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Jersey configuration properties for message & entity processing.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@PropertiesClass
public final class MessageProperties {

    /**
     * If set to {@code true} then XML root element tag name for collections will
     * be derived from {@link javax.xml.bind.annotation.XmlRootElement @XmlRootElement}
     * annotation value and won't be de-capitalized.
     * <p />
     * The default value is {@code false}.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String JAXB_PROCESS_XML_ROOT_ELEMENT = "jersey.config.jaxb.collections.processXmlRootElement";

    /**
     * If set to {@code true} XML security features when parsing XML documents will be
     * disabled.
     * <p />
     * The default value is {@code false}.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String XML_SECURITY_DISABLE = "jersey.config.xml.security.disable";

    /**
     * If set to {@code true} indicates that produced XML output should be formatted
     * if possible (see below).
     * <p />
     * A XML message entity written by a {@link javax.ws.rs.ext.MessageBodyWriter}
     * may be formatted for the purposes of human readability provided the respective
     * {@code MessageBodyWriter} supports XML output formatting. All JAXB-based message
     * body writers support this property.
     * <p />
     * The default value is {@code false}.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String XML_FORMAT_OUTPUT = "jersey.config.xml.formatOutput";

    /**
     * Value of the property indicates the buffer size to be used for I/O operations
     * on byte and character streams. The property value is expected to be a positive
     * integer otherwise it will be ignored.
     * <p />
     * The default value is <code>{@value #IO_DEFAULT_BUFFER_SIZE}</code>.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String IO_BUFFER_SIZE = "jersey.config.io.bufferSize";

    /**
     * The default buffer size ({@value}) for I/O operations on byte and character
     * streams.
     */
    public static final int IO_DEFAULT_BUFFER_SIZE = 8192;

    /**
     * If set to {@code true}, {@code DeflateEncoder deflate encoding interceptor} will use non-standard version
     * of the deflate content encoding, skipping the zlib wrapper. Unfortunately, deflate encoding
     * implementations in some products use this non-compliant version, hence the switch.
     * <p />
     * The default value is {@code false}.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String DEFLATE_WITHOUT_ZLIB = "jersey.config.deflate.nozlib";

    /**
     * If set to {@code true}, {@link javax.ws.rs.ext.MessageBodyReader MessageBodyReaders} and
     * {@link javax.ws.rs.ext.MessageBodyWriter MessageBodyWriters} will be ordered by rules from JAX-RS 1.x, where custom
     * providers have always precedence; providers are sorted by {@link javax.ws.rs.core.MediaType} and afterwards by
     * declaration distance - see {@link org.glassfish.jersey.message.internal.MessageBodyFactory.DeclarationDistanceComparator}.
     * Otherwise JAX-RS 2.x ordering will be used, which sorts providers firstly by declaration distance, then by
     * {@link javax.ws.rs.core.MediaType} and by origin (custom/provided).
     * <p />
     * The default value is {@code false}.
     * <p />
     * The name of the configuration property is <code>{@value}</code>.
     */
    public static final String LEGACY_WORKERS_ORDERING = "jersey.config.workers.legacyOrdering";

    /**
     * Prevents instantiation.
     */
    private MessageProperties() {
    }
}
