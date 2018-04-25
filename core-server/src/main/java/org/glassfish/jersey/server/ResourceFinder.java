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

package org.glassfish.jersey.server;

import java.io.InputStream;
import java.util.Iterator;

/**
 * An interface used for finding and opening (loading) new resources.
 * <p/>
 * {@link ResourceConfig} will use all registered finders to obtain classes
 * to be used as resource classes and/or providers. Method {@link #open()} doesn't
 * need to be called on all returned resource names, {@link ResourceConfig} can ignore
 * some of them.
 * <p/>
 * Currently, all resource names ending with ".class" will be accepted and processed (opened).
 * <p/>
 * Extends {@link AutoCloseable} since version 2.19. The {@link #close()} method is used to release
 * allocated/opened resources (such as streams). When a resource finder is closed no other method should be
 * invoked on it.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public interface ResourceFinder extends Iterator<String>, AutoCloseable {

    /**
     * Open current resource.
     *
     * @return input stream from which current resource can be loaded.
     */
    public InputStream open();

    /**
     * {@inheritDoc}
     * <p/>
     * Release allocated/opened resources (such as streams). When the resource finder is closed
     * no other method should be invoked on it.
     *
     * @since 2.19
     */
    public void close();

    /**
     * Reset the {@link ResourceFinder} instance.
     * <p/>
     * Upon calling this method the implementing class MUST reset its internal state to the initial state.
     */
    public void reset();

    /**
     * {@inheritDoc}
     * <p/>
     * This operation is not supported by {@link ResourceFinder} & throws {@link UnsupportedOperationException}
     * when invoked.
     */
    @Override
    public void remove();
}
