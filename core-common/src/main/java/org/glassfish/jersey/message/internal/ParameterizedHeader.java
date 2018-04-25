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

package org.glassfish.jersey.message.internal;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

/**
 * A general parameterized header.
 * <p/>
 * The header consists of a value and zero or more parameters. A value consists of zero or more tokens and separators up to but
 * not including a ';' separator if present. The tokens and separators of a value may be separated by zero or more white space,
 * which is ignored and is not considered part of the value. The value is separated from the parameters with a ';'. Each
 * parameter is separated with a ';'.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ParameterizedHeader {

    private String value;
    private Map<String, String> parameters;

    /**
     * Create a parameterized header from given string value.
     *
     * @param header header to create parameterized header from.
     * @throws ParseException if an un-expected/in-correct value is found during parsing the header.
     */
    public ParameterizedHeader(final String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    /**
     * Create a parameterized header from given {@link HttpHeaderReader http header reader}.
     *
     * @param reader reader to initialize new parameterized header from.
     * @throws ParseException if an un-expected/in-correct value is found during parsing the header.
     */
    public ParameterizedHeader(final HttpHeaderReader reader) throws ParseException {
        reader.hasNext();

        value = "";
        while (reader.hasNext() && !reader.hasNextSeparator(';', false)) {
            reader.next();
            value += reader.getEventValue();
        }

        if (reader.hasNext()) {
            parameters = HttpHeaderReader.readParameters(reader);
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        } else {
            parameters = Collections.unmodifiableMap(parameters);
        }
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

}
