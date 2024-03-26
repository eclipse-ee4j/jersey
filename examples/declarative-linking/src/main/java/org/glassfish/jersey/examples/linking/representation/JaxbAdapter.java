/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.representation;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * An implementation of JAXB {@link XmlAdapter} that maps the JAX-RS
 * {@link Link} type to a value that can be marshalled and unmarshalled by JAXB. The following example
 * shows how to use this adapter on a JAXB bean class:
 *
 * <pre>
 * &#64;XmlRootElement
 * public class MyModel {
 *
 *   private Link link;
 *
 *   &#64;XmlElement(name="link")
 *   &#64;XmlJavaTypeAdapter(JaxbAdapter.class)
 *   public Link getLink() {
 *     return link;
 *   }
 *   ...
 * }
 * </pre>
 *
 * <p>
 *     Note that usage of this class requires the Jakarta XML Binding API and an implementation. The Jakarta RESTful Web
 *     Services implementation is not required to provide these dependencies.
 * </p>
 * <p>
 *     The class used to be a part Jakarta REST 3.1
 * </p>
 *
 * @see JaxbLink
 * @since 4.0
 */
public class JaxbAdapter extends XmlAdapter<JaxbLink, Link> {

    /**
     * Convert a {@link JaxbLink} into a {@link Link}.
     *
     * @param v instance of type {@link JaxbLink}.
     * @return mapped instance of type {@link JaxbLink}
     */
    @Override
    public Link unmarshal(final JaxbLink v) {
        Link.Builder lb = Link.fromUri(v.getUri());
        for (Map.Entry<QName, Object> e : v.getParams().entrySet()) {
            lb.param(e.getKey().getLocalPart(), e.getValue().toString());
        }
        return lb.build();
    }

    /**
     * Convert a {@link Link} into a {@link JaxbLink}.
     *
     * @param v instance of type {@link Link}.
     * @return mapped instance of type {@link JaxbLink}.
     */
    @Override
    public JaxbLink marshal(final Link v) {
        JaxbLink jl = new JaxbLink(v.getUri());
        for (Map.Entry<String, String> e : v.getParams().entrySet()) {
            final String name = e.getKey();
            jl.getParams().put(new QName("", name), e.getValue());
        }
        return jl;
    }
}
