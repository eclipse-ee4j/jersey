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

package org.glassfish.jersey.spi;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A provider that supports the conversion of an HTTP header, of type T, to and
 * from a {@link String}.
 * <p>
 * An implementation (a service-provider) identifies itself by placing a
 * provider-configuration file (if not already present),
 * "org.glassfish.jersey.header.spi.HeaderDelegateProvider" in the
 * resource directory <tt>META-INF/services</tt>, and including the fully qualified
 * service-provider-class of the implementation in the file.

 * @param <T> the type of the header.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Contract
public interface HeaderDelegateProvider<T> extends HeaderDelegate<T> {

    /**
     * Ascertain if the Provider supports a particular type.
     *
     * @param type the type that is to be supported.
     * @return true if the type is supported, otherwise false.
     */
    boolean supports(Class<?> type);
}
