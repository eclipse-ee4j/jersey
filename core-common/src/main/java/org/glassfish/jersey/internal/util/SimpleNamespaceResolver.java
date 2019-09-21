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

package org.glassfish.jersey.internal.util;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Simple namespace resolver which resolves one predefined namespace.
 *
 * @author Gerard Davison
 * @author Miroslav Fuksa
 */
public class SimpleNamespaceResolver implements NamespaceContext {
    private final String prefix;
    private final String nsURI;

    /**
     * Create a new instance of the namespace resolver initialized with the
     * fixed {@code prefix} and {@code URI} that the resolver will be capable to resolve.
     *
     * @param prefix Namespace prefix.
     * @param nsURI Namespace URI.
     */
    public SimpleNamespaceResolver(String prefix, String nsURI) {
        this.prefix = prefix;
        this.nsURI = nsURI;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix.equals(this.prefix)) {
            return this.nsURI;
        } else {
            return XMLConstants.NULL_NS_URI;
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI.equals(this.nsURI)) {
            return this.prefix;
        } else {
            return null;
        }
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
