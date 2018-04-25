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

package org.glassfish.jersey.servlet.async;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.servlet.init.internal.LocalizationMessages;
import org.glassfish.jersey.servlet.spi.AsyncContextDelegate;
import org.glassfish.jersey.servlet.spi.AsyncContextDelegateProvider;

/**
 * Servlet 3.x container response writer async extension and related extension factory implementation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AsyncContextDelegateProviderImpl implements AsyncContextDelegateProvider {

    private static final Logger LOGGER = Logger.getLogger(AsyncContextDelegateProviderImpl.class.getName());

    @Override
    public final AsyncContextDelegate createDelegate(final HttpServletRequest request, final HttpServletResponse response) {
        return new ExtensionImpl(request, response);
    }

    private static final class ExtensionImpl implements AsyncContextDelegate {

        private static final int NEVER_TIMEOUT_VALUE = -1;

        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final AtomicReference<AsyncContext> asyncContextRef;
        private final AtomicBoolean completed;

        /**
         * Create a Servlet 3.x {@link AsyncContextDelegate} with given {@code request} and {@code response}.
         *
         * @param request  request to create {@link AsyncContext} for.
         * @param response response to create {@link AsyncContext} for.
         */
        private ExtensionImpl(final HttpServletRequest request, final HttpServletResponse response) {
            this.request = request;
            this.response = response;
            this.asyncContextRef = new AtomicReference<>();
            this.completed = new AtomicBoolean(false);
        }

        @Override
        public void suspend() throws IllegalStateException {
            // Suspend only if not completed and not suspended before.
            if (!completed.get() && asyncContextRef.get() == null) {
                asyncContextRef.set(getAsyncContext());
            }
        }

        private AsyncContext getAsyncContext() {
            final AsyncContext asyncContext;
            if (request.isAsyncStarted()) {
                asyncContext = request.getAsyncContext();
                try {
                    asyncContext.setTimeout(NEVER_TIMEOUT_VALUE);
                } catch (IllegalStateException ex) {
                    // Let's hope the time out is set properly, otherwise JAX-RS AsyncResponse time-out support
                    // may not work as expected... At least we can log this at fine level...
                    LOGGER.log(Level.FINE, LocalizationMessages.SERVLET_ASYNC_CONTEXT_ALREADY_STARTED(), ex);
                }
            } else {
                asyncContext = request.startAsync(request, response);
                // Tell underlying asyncContext to never time out.
                asyncContext.setTimeout(NEVER_TIMEOUT_VALUE);
            }
            return asyncContext;
        }

        @Override
        public void complete() {
            completed.set(true);

            final AsyncContext asyncContext = asyncContextRef.getAndSet(null);
            if (asyncContext != null) {
                asyncContext.complete();
            }
        }
    }
}
