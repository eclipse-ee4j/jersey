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

import java.util.Objects;

/**
 * Jersey subclass of {@link Cookie} bearing information about optional version.
 * Complies with RFC 6265.
 */
public final class JerseyCookie extends Cookie implements VersionOptional {
    private final Integer version;
    private JerseyCookie(Builder builder) throws IllegalArgumentException {
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
        if (obj.getClass() != Cookie.class && obj.getClass() != JerseyCookie.class) {
            return false;
        }
        final Cookie other = (Cookie) obj;
        if (!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        if (!Objects.equals(this.getVersion(), other.getVersion())) {
            return false;
        }
        if (!Objects.equals(this.getPath(), other.getPath())) {
            return false;
        }
        if (!Objects.equals(this.getDomain(), other.getDomain())) {
            return false;
        }
        return true;
    }


    /**
     * Builder for building Cookie.
     */
    public static final class Builder extends AbstractCookieBuilder<JerseyCookie.Builder> {
        private Integer version;
        /**
         * Create a new instance.
         *
         * @param name the name of the cookie.
         */
        public Builder(String name) {
            super(name);
        }

        @Override
        public Builder version(int version) {
            super.version(version);
            this.version = version;
            return this;
        }

        /**
         * Set version as nullable Integer. {@code null} refers to RFC 6265 Cookie without a version.
         * @param version the version of the HTTP Cookie.
         * @return updated builder.
         */
        public Builder version(Integer version) {
            super.version(version == null ? VersionOptional.NETSCAPE_VERSION : version);
            this.version = version;
            return this;
        }

        @Override
        public Cookie build() {
            return new JerseyCookie(this);
        }

    }

}
