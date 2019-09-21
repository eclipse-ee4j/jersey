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

package org.glassfish.jersey.uri;

import org.glassfish.jersey.uri.internal.UriTemplateParser;

/**
 * A URI template for a URI path.
 *
 * @author Paul Sandoz
 * @author Yegor Bugayenko (yegor256 at java.net)
 */
public final class PathTemplate extends UriTemplate {

    /**
     * Internal parser of this PathTemplate.
     * @see #PathTemplate(String)
     */
    private static final class PathTemplateParser extends UriTemplateParser {

        /**
         * Public constructor.
         *
         * @param path the URI path template
         */
        public PathTemplateParser(final String path) {
            super(path);
        }

        @Override
        protected String encodeLiteralCharacters(final String literalCharacters) {

            return UriComponent.contextualEncode(
                    literalCharacters,
                    UriComponent.Type.PATH);
        }
    }

    /**
     * Create a URI path template and encode (percent escape) any characters of
     * the template that are not valid URI characters. Paths that don't start with
     * a slash ({@code '/'}) will be automatically prefixed with one.
     *
     * @param path the URI path template.
     */
    public PathTemplate(final String path) {
        super(new PathTemplateParser(PathTemplate.prefixWithSlash(path)));
    }

    /**
     * Converts the path provided to a slash-leading form, no matter what is provided.
     *
     * @param path the URI path template.
     * @return slash-prefixed path.
     * @see #PathTemplate(String)
     */
    private static String prefixWithSlash(final String path) {
        return !path.isEmpty() && path.charAt(0) == '/' ? path : "/" + path;
    }
}
