/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.wadl.doclet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

class Loader extends URLClassLoader {

    public Loader(final String[] paths, final ClassLoader parent) {
        super(getURLs(paths), parent);
    }

    Loader(final String[] paths) {
        super(getURLs(paths));
    }

    private static URL[] getURLs(final String[] paths) {
        final List<URL> urls = new ArrayList<>();
        for (final String path : paths) {
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

}
