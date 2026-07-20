/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.glassfish.jersey.servlet.spi.AsyncContextDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsyncContextDelegateProviderImplTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AsyncContext asyncContext;

    private AsyncContextDelegateProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new AsyncContextDelegateProviderImpl();
    }

    // -----------------------------------------------------------------------
    // Factory method
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("createDelegate()")
    class CreateDelegate {

        @Test
        void returnsNonNull() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            assertNotNull(delegate);
            assertFalse(delegate.isCompleted());
        }
    }

    // -----------------------------------------------------------------------
    // suspend()
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("suspend()")
    class Suspend {

        @Test
        @DisplayName("starts async on a non-async request")
        void startsAsyncWhenNotAlreadyStarted() {
            when(request.isAsyncStarted()).thenReturn(false);
            when(request.startAsync(request, response)).thenReturn(asyncContext);

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.suspend();

            verify(request).startAsync(request, response);
            verify(asyncContext).setTimeout(-1L);
        }


        @Test
        @DisplayName("reuses existing async context when async already started")
        void reusesExistingAsyncContext() {
            when(request.isAsyncStarted()).thenReturn(true);
            when(request.getAsyncContext()).thenReturn(asyncContext);

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.suspend();

            verify(request, never()).startAsync(any(), any());
            verify(request).getAsyncContext();
            verify(asyncContext).setTimeout(-1L);
        }


        @Test
        @DisplayName("sets timeout to -1 (never time out) when starting async")
        void setsNeverTimeoutOnNewAsyncContext() {
            when(request.isAsyncStarted()).thenReturn(false);
            when(request.startAsync(request, response)).thenReturn(asyncContext);

            provider.createDelegate(request, response).suspend();

            verify(asyncContext).setTimeout(-1L);
        }


        @Test
        @DisplayName("does not start async twice on repeated suspend() calls")
        void idempotentSuspend() {
            when(request.isAsyncStarted()).thenReturn(false);
            when(request.startAsync(request, response)).thenReturn(asyncContext);

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.suspend();
            // second call must be a no-op
            delegate.suspend();

            verify(request, times(1)).startAsync(request, response);
        }


        @Test
        @DisplayName("does not start async after complete() was already called")
        void doesNotSuspendAfterComplete() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            // mark as completed first
            delegate.complete();
            // should be ignored
            delegate.suspend();

            verify(request, never()).isAsyncStarted();
            verify(request, never()).startAsync(any(), any());
        }


        @Test
        @DisplayName("handles IllegalStateException from asyncContext.setTimeout() gracefully")
        void handlesSetTimeoutException() {
            when(request.isAsyncStarted()).thenReturn(true);
            when(request.getAsyncContext()).thenReturn(asyncContext);
            doThrow(new IllegalStateException("already committed")).when(asyncContext).setTimeout(anyLong());

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            // Should not propagate the exception
            assertDoesNotThrow(delegate::suspend);
        }
    }

    // -----------------------------------------------------------------------
    // complete()
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("marks delegate as completed")
        void marksAsCompleted() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.complete();
            assertTrue(delegate.isCompleted());
        }


        @Test
        @DisplayName("calls asyncContext.complete() when suspended")
        void completesAsyncContextWhenSuspended() {
            when(request.isAsyncStarted()).thenReturn(false);
            when(request.startAsync(request, response)).thenReturn(asyncContext);

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.suspend();
            delegate.complete();

            verify(asyncContext).complete();
        }


        @Test
        @DisplayName("does not call asyncContext.complete() when never suspended")
        void doesNotCompleteAsyncContextWhenNeverSuspended() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.complete();

            verify(asyncContext, never()).complete();
        }


        @Test
        @DisplayName("second complete() call is a no-op (idempotent guard)")
        void secondCompleteIsNoOp() {
            when(request.isAsyncStarted()).thenReturn(false);
            when(request.startAsync(request, response)).thenReturn(asyncContext);

            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.suspend();
            delegate.complete();
            delegate.complete(); // must not throw and must not double-complete

            verify(asyncContext, times(1)).complete();
        }
    }

    // -----------------------------------------------------------------------
    // isCompleted()
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("isCompleted()")
    class IsCompleted {

        @Test
        @DisplayName("returns false before complete() is called")
        void falseBeforeComplete() {
            assertFalse(provider.createDelegate(request, response).isCompleted());
        }


        @Test
        @DisplayName("returns true after complete() is called")
        void trueAfterComplete() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.complete();
            assertTrue(delegate.isCompleted());
        }


        @Test
        @DisplayName("returns true after complete(), even without a prior suspend()")
        void trueAfterCompleteWithoutSuspend() {
            AsyncContextDelegate delegate = provider.createDelegate(request, response);
            delegate.complete();
            assertTrue(delegate.isCompleted());
        }
    }

}
