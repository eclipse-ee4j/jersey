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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Link;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * Provider for Link Headers.
 *
 * @author Santiago Pericas-Geertsen
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class LinkProvider implements HeaderDelegateProvider<Link> {

    private static final Logger LOGGER = Logger.getLogger(LinkProvider.class.getName());

    @Override
    public boolean supports(final Class<?> type) {
        return Link.class.isAssignableFrom(type);
    }

    @Override
    public Link fromString(final String value) throws IllegalArgumentException {
        return initBuilder(new JerseyLink.Builder(), value).build();
    }

    /**
     * Initialize an existing Jersey link builder with the link data provided in a form of a string.
     *
     * @param lb    link builder to be initialized.
     * @param value link data as a string.
     * @return initialized link builder.
     */
    static JerseyLink.Builder initBuilder(JerseyLink.Builder lb, String value) {
        throwIllegalArgumentExceptionIfNull(value, LocalizationMessages.LINK_IS_NULL());
        try {
            value = value.trim();
            final String params;
            if (value.startsWith("<")) {
                final int gtIndex = value.indexOf('>');
                if (gtIndex != -1) {
                    lb.uri(value.substring(1, gtIndex).trim());
                    params = value.substring(gtIndex + 1).trim();
                } else {
                    throw new IllegalArgumentException("Missing token > in " + value);
                }
            } else {
                throw new IllegalArgumentException("Missing starting token < in " + value);
            }

            final StringTokenizer st = new StringTokenizer(params, ";=\"", true);
            while (st.hasMoreTokens()) {
                checkToken(st, ";");
                final String n = st.nextToken().trim();
                checkToken(st, "=");

                String v = nextNonEmptyToken(st);
                if (v.equals("\"")) {
                    v = st.nextToken();
                    checkToken(st, "\"");
                }

                lb.param(n, v);
            }
        } catch (final Throwable e) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Error parsing link value '" + value + "'", e);
            }
            lb = null;
        }
        if (lb == null) {
            throw new IllegalArgumentException("Unable to parse link " + value);
        }
        return lb;
    }

    private static String nextNonEmptyToken(final StringTokenizer st) throws IllegalArgumentException {
        String token;
        do {
            token = st.nextToken().trim();
        } while (token.length() == 0);

        return token;
    }

    private static void checkToken(final StringTokenizer st, final String expected) throws IllegalArgumentException {
        String token;
        do {
            token = st.nextToken().trim();
        } while (token.length() == 0);
        if (!token.equals(expected)) {
            throw new IllegalArgumentException("Expected token " + expected + " but found " + token);
        }
    }

    @Override
    public String toString(final Link value) {
        return stringfy(value);
    }

    /**
     * Convert {@link Link} instance to a string version.
     *
     * @param value link instance to be stringified.
     * @return string version of a given link instance.
     */
    static String stringfy(final Link value) {
        throwIllegalArgumentExceptionIfNull(value, LocalizationMessages.LINK_IS_NULL());

        final Map<String, String> map = value.getParams();
        final StringBuilder sb = new StringBuilder();
        sb.append('<').append(value.getUri()).append('>');

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("; ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        return sb.toString();
    }

    /**
     * Extract the list of link relations from the string value of a {@link Link#REL} attribute.
     *
     * @param rel string value of the link {@code "rel"} attribute.
     * @return list of relations in the {@code "rel"} attribute string value.
     */
    static List<String> getLinkRelations(final String rel) {
        return (rel == null) ? null : Arrays.asList(Tokenizer.tokenize(rel, "\" "));
    }
}
