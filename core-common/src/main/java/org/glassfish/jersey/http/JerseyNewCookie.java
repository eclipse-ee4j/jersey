/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.http;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import java.util.Objects;

/**
 * Jersey subclass of {@link NewCookie} bearing information about optional version.
 * Complies with RFC 6265.
 */
public final class JerseyNewCookie extends NewCookie implements VersionOptional {
    private final Integer version;
    private JerseyNewCookie(JerseyNewCookie.Builder builder) {
        super(builder);
        this.version = builder.version;
    }

    @Override
    public boolean hasVersion() {
        return version != null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != NewCookie.class && obj.getClass() != JerseyNewCookie.class) {
            return false;
        }
        final NewCookie other = (NewCookie) obj;
        if (!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        if (this.getVersion() != other.getVersion()) {
            return false;
        }
        if (!Objects.equals(this.getPath(), other.getPath())) {
            return false;
        }
        if (!Objects.equals(this.getDomain(), other.getDomain())) {
            return false;
        }
        if (!Objects.equals(this.getComment(), other.getComment())) {
            return false;
        }
        if (this.getMaxAge() != other.getMaxAge()) {
            return false;
        }
        if (!Objects.equals(this.getExpiry(), other.getExpiry())) {
            return false;
        }
        if (this.isSecure() != other.isSecure()) {
            return false;
        }
        if (this.isHttpOnly() != other.isHttpOnly()) {
            return false;
        }
        if (this.getSameSite() != other.getSameSite()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version);
    }

    /**
     * Builder class building the Jersey NewCookie subclass.
     */
    public static final class Builder extends AbstractNewCookieBuilder<JerseyNewCookie.Builder> {
        private Integer version;


        /**
         * Create a new instance.
         *
         * @param name the name of the cookie.
         */
        public Builder(String name) {
            super(name);
        }

        /**
         * Create a new instance supplementing the information in the supplied cookie.
         *
         * @param cookie the cookie to copy.
         */
        public Builder(Cookie cookie) {
            super(cookie);
            if (cookie instanceof VersionOptional) {
                this.version = ((VersionOptional) cookie).hasVersion() ? cookie.getVersion() : null;
            }
        }

        @Override
        public Builder version(int version) {
            super.version(version);
            this.version = version;
            return this;
        }

        /**
         * Set version. Can be {@code null} if RFC 6265.
         * @param version the cookie version.
         * @return updated builder.
         */
        public Builder version(Integer version) {
            // Version 0 should refer to NETSCAPE, but NewCookie should be for RFC 2109 & 2965 - version 1.
            super.version(version == null ? DEFAULT_VERSION : version);
            this.version = version;
            return this;
        }

        @Override
        public NewCookie build() {
            return new JerseyNewCookie(this);
        }
    }
}
