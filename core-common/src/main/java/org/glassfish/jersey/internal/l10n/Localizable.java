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

package org.glassfish.jersey.internal.l10n;

/**
 * Localizable message.
 *
 * @author WS Development Team
 */
public interface Localizable {

    /**
     * Special constant that represents a message that
     * is not localizable.
     *
     * <p>
     * Use of "new" is to create an unique instance.
     */
    public static final String NOT_LOCALIZABLE = "\u0000";

    /**
     * Gets the key in the resource bundle.
     *
     * @return if this method returns {@link #NOT_LOCALIZABLE}, that means the
     *     message is not localizable, and the first item of {@link #getArguments()}
     *     array holds a {@code String}.
     */
    public String getKey();

    /**
     * Returns the arguments for message formatting.
     *
     * @return can be an array of length 0 but never be {@code null}.
     */
    public Object[] getArguments();

    /**
     * Get the name of the localization messages resource bundle.
     *
     * @return the localization messages resource bundle name.
     */
    public String getResourceBundleName();
}
