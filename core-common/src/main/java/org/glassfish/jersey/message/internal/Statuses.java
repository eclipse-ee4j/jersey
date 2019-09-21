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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

/**
 * Factory for producing custom JAX-RS {@link StatusType response status type}
 * instances.
 *
 * @author Paul Sandoz
 */
public final class Statuses {

    private static final class StatusImpl implements StatusType {

        private final int code;
        private final String reason;
        private final Family family;

        private StatusImpl(int code, String reason) {
            this.code = code;
            this.reason = reason;
            this.family = Family.familyOf(code);
        }

        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public String getReasonPhrase() {
            return reason;
        }

        @Override
        public String toString() {
            return reason;
        }

        @Override
        public Family getFamily() {
            return family;
        }

        @Override
        @SuppressWarnings("RedundantIfStatement")
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StatusType)) {
                return false;
            }

            final StatusType status = (StatusType) o;

            if (code != status.getStatusCode()) {
                return false;
            }
            if (family != status.getFamily()) {
                return false;
            }
            if (reason != null ? !reason.equals(status.getReasonPhrase()) : status.getReasonPhrase() != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = code;
            result = 31 * result + (reason != null ? reason.hashCode() : 0);
            result = 31 * result + family.hashCode();
            return result;
        }
    }

    /**
     * Create a new status type instance.
     * <p>
     * For standard status codes listed in {@link javax.ws.rs.core.Response.Status} enum, the default reason phrase
     * is used. For any other status code an empty string is used as a reason phrase.
     * </p>
     *
     * @param code response status code.
     * @return new status type instance representing a given response status code.
     */
    public static StatusType from(int code) {
        StatusType result = Response.Status.fromStatusCode(code);
        return (result != null) ? result : new StatusImpl(code, "");
    }

    /**
     * Create a new status type instance with a custom reason phrase.
     *
     * @param code   response status code.
     * @param reason custom response status reason phrase.
     * @return new status type instance representing a given response status code and custom reason phrase.
     */
    public static StatusType from(int code, String reason) {
        return new StatusImpl(code, reason);
    }

    /**
     * Create a new status type instance with a custom reason phrase.
     *
     * @param status response status type.
     * @param reason custom response status reason phrase.
     * @return new status type instance representing a given response status code and custom reason phrase.
     */
    public static StatusType from(StatusType status, String reason) {
        return new StatusImpl(status.getStatusCode(), reason);
    }

    /**
     * Prevents instantiation.
     */
    private Statuses() {
        throw new AssertionError("Instantiation not allowed.");
    }
}
