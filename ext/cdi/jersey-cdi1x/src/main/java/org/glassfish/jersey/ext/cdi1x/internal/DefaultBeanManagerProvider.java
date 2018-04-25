/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.jersey.ext.cdi1x.internal.spi.BeanManagerProvider;

/**
 * Default implementation of {@link BeanManagerProvider} that works on most environments.
 * At first the implementation tries to lookup the bean manager in JNDI, then via CDI 1.1 API. If not found {@code null} is
 * returned.
 *
 * @author Michal Gajdos
 * @since 2.17
 */
final class DefaultBeanManagerProvider implements BeanManagerProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultBeanManagerProvider.class.getName());

    @Override
    public BeanManager getBeanManager() {
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
            return (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (final Exception ex) {
            try {
                return CDI.current().getBeanManager();
            } catch (final Exception e) {
                LOGGER.config(LocalizationMessages.CDI_BEAN_MANAGER_JNDI_LOOKUP_FAILED());
                return null;
            }
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (final NamingException ignored) {
                    // no-op
                }
            }
        }
    }
}
