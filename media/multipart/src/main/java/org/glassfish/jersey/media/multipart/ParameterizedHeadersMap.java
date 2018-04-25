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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.glassfish.jersey.message.internal.ParameterizedHeader;

/**
 * A map of MIME headers with parametrized values.
 * <p/>
 * An implementation of {@link MultivaluedMap} where keys are instances of String and are compared ignoring case and values are
 * instances of {@link ParameterizedHeader}.
 *
 * @author Craig McClanahan
 * @author Michal Gajdos
 */
/* package */ class ParameterizedHeadersMap extends StringKeyIgnoreCaseMultivaluedMap<ParameterizedHeader> {

    /**
     * Create new parameterized headers map.
     */
    public ParameterizedHeadersMap() {
    }

    /**
     * Create new parameterized headers map from given headers.
     *
     * @param headers headers to initialize this map from.
     * @throws ParseException if an un-expected/in-correct value is found during parsing the headers.
     */
    public ParameterizedHeadersMap(final MultivaluedMap<String, String> headers) throws ParseException {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            List<ParameterizedHeader> list = new ArrayList<ParameterizedHeader>(entry.getValue().size());
            for (String value : entry.getValue()) {
                list.add(new ParameterizedHeader(value));
            }
            this.put(entry.getKey(), list);
        }
    }

}
