/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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


import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * {@code String} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
@Singleton
public class StringHeaderProvider implements HeaderDelegateProvider<String> {

    @Override
    public boolean supports(Class<?> type) {
        return type == String.class;
    }

    @Override
    public String toString(String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.STRING_IS_NULL());

        return header;
    }

    @Override
    public String fromString(String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.STRING_IS_NULL());

        return header;
    }
}
