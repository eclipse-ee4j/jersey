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

package org.glassfish.jersey.examples.linking.representation;

import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *     Value type for {@link jakarta.ws.rs.core.Link} that can be marshalled and
 *     unmarshalled by JAXB.
 * </p>
 * <p>
 *     Note that usage of this class requires the Jakarta XML Binding API and an implementation. The Jakarta RESTful Web
 *     Services implementation is not required to provide these dependencies.
 * </p>
 * <p>
 *     The class used to be a part Jakarta REST API 3.1.
 * </p>
 *
 * @see JaxbAdapter
 * @since 4.0
 */
public class JaxbLink {

    private URI uri;
    private Map<QName, Object> params;

    /**
     * Default constructor needed during unmarshalling.
     */
    public JaxbLink() {
    }

    /**
     * Construct an instance from a URI and no parameters.
     *
     * @param uri underlying URI.
     */
    public JaxbLink(final URI uri) {
        this.uri = uri;
    }

    /**
     * Construct an instance from a URI and some parameters.
     *
     * @param uri underlying URI.
     * @param params parameters of this link.
     */
    public JaxbLink(final URI uri, final Map<QName, Object> params) {
        this.uri = uri;
        this.params = params;
    }

    /**
     * Get the underlying URI for this link.
     *
     * @return underlying URI.
     */
    @XmlAttribute(name = "href")
    public URI getUri() {
        return uri;
    }

    /**
     * Get the parameter map for this link.
     *
     * @return parameter map.
     */
    @XmlAnyAttribute
    public Map<QName, Object> getParams() {
        if (params == null) {
            params = new HashMap<QName, Object>();
        }
        return params;
    }

    /**
     * Set the underlying URI for this link.
     *
     * This setter is needed for JAXB unmarshalling.
     */
    void setUri(final URI uri) {
        this.uri = uri;
    }

    /**
     * Set the parameter map for this link.
     *
     * This setter is needed for JAXB unmarshalling.
     */
    void setParams(final Map<QName, Object> params) {
        this.params = params;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JaxbLink)) {
            return false;
        }

        JaxbLink jaxbLink = (JaxbLink) o;

        if (uri != null ? !uri.equals(jaxbLink.uri) : jaxbLink.uri != null) {
            return false;
        }

        if (params == jaxbLink.params) {
            return true;
        }
        if (params == null) {
            // if this.params is 'null', consider other.params equal to empty
            return jaxbLink.params.isEmpty();
        }
        if (jaxbLink.params == null) {
            // if other.params is 'null', consider this.params equal to empty
            return params.isEmpty();
        }

        return params.equals(jaxbLink.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, params);
    }

}
