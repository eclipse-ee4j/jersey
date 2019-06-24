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

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author WS Development Team
 */
public final class LocalizableMessage implements Localizable {

    private final String _bundlename;
    private final LocalizableMessageFactory.ResourceBundleSupplier _rbSupplier;

    private final String _key;
    private final Object[] _args;

    @Deprecated
    public LocalizableMessage(String bundlename, String key, Object... args) {
        this(bundlename, null, key, args);
    }

    public LocalizableMessage(String bundlename, LocalizableMessageFactory.ResourceBundleSupplier rbSupplier,
                              String key, Object... args) {
        _bundlename = bundlename;
        _rbSupplier = rbSupplier;
        _key = key;
        if (args == null) {
            args = new Object[0];
        }
        _args = args;
    }

    @Override
    public String getKey() {
        return _key;
    }

    @Override
    public Object[] getArguments() {
        return Arrays.copyOf(_args, _args.length);
    }

    @Override
    public String getResourceBundleName() {
        return _bundlename;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        if (_rbSupplier == null) {
            return null;
        }

        return _rbSupplier.getResourceBundle(locale);
    }
}