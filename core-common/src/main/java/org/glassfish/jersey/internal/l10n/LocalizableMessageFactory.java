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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author WS Development Team
 */
public class LocalizableMessageFactory {

    private final String _bundlename;
    private final ResourceBundleSupplier _rbSupplier;

    @Deprecated
    public LocalizableMessageFactory(String bundlename) {
        _bundlename = bundlename;
        _rbSupplier = null;
    }

    public LocalizableMessageFactory(String bundlename, ResourceBundleSupplier rbSupplier) {
        _bundlename = bundlename;
        _rbSupplier = rbSupplier;
    }

    public Localizable getMessage(String key, Object... args) {
        return new LocalizableMessage(_bundlename, _rbSupplier, key, args);
    }

    public interface ResourceBundleSupplier {
        /**
         * Gets the ResourceBundle.
         * @param locale the requested bundle's locale
         * @return ResourceBundle
         */
        ResourceBundle getResourceBundle(Locale locale);
    }

}
