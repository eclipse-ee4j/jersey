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

import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * {@link Stack} of {@link ResourceFinder} instances.
 * <p/>
 * Used to combine various finders into one instance.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public final class CompositeResourceFinder extends AbstractResourceFinderAdapter {

    private static final Logger LOGGER = Logger.getLogger(CompositeResourceFinder.class.getName());

    private final Deque<ResourceFinder> stack = new LinkedList<>();
    private ResourceFinder current = null;

    @Override
    public boolean hasNext() {
        if (current == null) {
            if (!stack.isEmpty()) {
                current = stack.pop();
            } else {
                return false;
            }
        }

        if (current.hasNext()) {
            return true;
        } else {
            if (!stack.isEmpty()) {
                current = stack.pop();
                return hasNext();
            } else {
                return false;
            }
        }
    }

    @Override
    public String next() {
        if (hasNext()) {
            return current.next();
        }

        throw new NoSuchElementException();
    }

    @Override
    public InputStream open() {
        return current.open();
    }

    @Override
    public void close() {
        if (current != null) {
            // Insert the currently processed resource finder at the top of the stack.
            stack.addFirst(current);
            current = null;
        }
        for (final ResourceFinder finder : stack) {
            try {
                finder.close();
            } catch (final RuntimeException e) {
                LOGGER.log(Level.CONFIG, LocalizationMessages.ERROR_CLOSING_FINDER(finder.getClass()), e);
            }
        }
        stack.clear();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void push(final ResourceFinder iterator) {
        stack.push(iterator);
    }
}
