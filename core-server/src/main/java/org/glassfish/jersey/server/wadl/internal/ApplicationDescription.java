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

package org.glassfish.jersey.server.wadl.internal;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import javax.xml.namespace.QName;

import org.glassfish.jersey.server.wadl.WadlGenerator;

import com.sun.research.ws.wadl.Application;

/**
 * This class is designed to combine the Application instance with any other
 * external metadata that might be required to describe the application.
 *
 * @author Gerard Davison
 */
public class ApplicationDescription {

    private Application _application;
    private WadlGenerator.ExternalGrammarDefinition _externalGrammarDefiniton;

    ApplicationDescription(Application application, WadlGenerator.ExternalGrammarDefinition externalGrammarDefiniton) {
        super();
        this._application = application;
        this._externalGrammarDefiniton = externalGrammarDefiniton;
    }

    /**
     * @return The instance of the application object
     */
    public Application getApplication() {
        return _application;
    }

    /**
     * @param type java class to be resolved.
     * @return the QName for the given Class in the grammar.
     */
    public QName resolve(Class type) {
        return _externalGrammarDefiniton.resolve(type);
    }

    /**
     * @param path path to external metadata.
     * @return the external metadata for a given URL, generally provided as a sub resource
     *         or the root application.wadl.
     */
    public ExternalGrammar getExternalGrammar(String path) {
        return _externalGrammarDefiniton.map.get(path);
    }

    /**
     * @return A set of all the external metadata keys
     */
    public Set<String> getExternalMetadataKeys() {
        return _externalGrammarDefiniton.map.keySet();
    }

    /**
     * A simple holder class that stores a type and binary content
     * to be used to return extra metadata with
     */
    public static class ExternalGrammar {

        private MediaType _type;
        private byte[] _content;

        public ExternalGrammar(MediaType type, byte[] content) {
            super();
            this._type = type;
            this._content = content.clone();
        }

        public MediaType getType() {
            return _type;
        }

        public byte[] getContent() {
            // Defensive copy
            return _content.clone();
        }
    }
}
