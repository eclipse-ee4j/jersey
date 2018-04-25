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

package org.glassfish.jersey.linking;

import org.glassfish.jersey.internal.l10n.Localizable;
import org.glassfish.jersey.internal.l10n.LocalizableMessageFactory;
import org.glassfish.jersey.internal.l10n.Localizer;

/**
 * Message for declarative linking
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class LinkMessages {

    private static final LocalizableMessageFactory messageFactory = new LocalizableMessageFactory(
            "org.glassfish.jersey.media.linking.internal");
    private static final Localizer localizer = new Localizer();

    private static Localizable localizableWARNING_LINKFILTER_PROCESSING(Object arg0) {
        return messageFactory.getMessage("warning.linkfilter.processing", arg0);
    }

    /**
     * LinkFilter cannot process class {0}, exception occurred during processing. Class will be ignored in the LinkFilter.
     *
     */
    static String WARNING_LINKFILTER_PROCESSING(Object arg0) {
        return localizer.localize(localizableWARNING_LINKFILTER_PROCESSING(arg0));
    }
}
