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

package org.glassfish.jersey.server.internal.scanning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;

/**
 * Preparations for OSGi support.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
final class BundleSchemeResourceFinderFactory implements UriSchemeResourceFinderFactory {

    private static final Set<String> SCHEMES = Collections.singleton("bundle");

    @Override
    public Set<String> getSchemes() {
        return SCHEMES;
    }

    /**
     * Create new bundle scheme resource finder factory.
     */
    BundleSchemeResourceFinderFactory() {
    }

    @Override
    public BundleSchemeScanner create(final URI uri, final boolean recursive) {
        return new BundleSchemeScanner(uri);
    }

    private class BundleSchemeScanner extends AbstractResourceFinderAdapter {

        private BundleSchemeScanner(final URI uri) {
            this.uri = uri;
        }

        private final URI uri;

        /**
         * Marks this iterator as iterated after execution of {@link #open()} method.
         * Together with {@link #iterated}, this field determines a returned value of {@link #hasNext()}.
         */
        private boolean accessed = false;

        /**
         * Marks this iterator as iterated after execution of {@link #next()} method.
         * Together with {@link #accessed}, this field determines a returned value of {@link #hasNext()}.
         */
        private boolean iterated = false;

        @Override
        public boolean hasNext() {
            return !accessed && !iterated;
        }

        @Override
        public String next() {
            if (hasNext()) {
                iterated = true;
                return uri.getPath();
            }

            throw new NoSuchElementException();
        }

        @Override
        public InputStream open() {
            if (!accessed) {
                try {
                    accessed = true;
                    return uri.toURL().openStream();
                } catch (final IOException e) {
                    throw new ResourceFinderException(e);
                }
            }

            return null;
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }
    }

}
