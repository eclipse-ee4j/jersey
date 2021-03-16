/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class ProcessAllAnnotatedTypes implements Extension {

    public void processAnnotatedType(@Observes ProcessAnnotatedType<?> processAnnotatedType, BeanManager beanManager) {
        CdiComponentProvider cdiComponentProvider = beanManager.getExtension(CdiComponentProvider.class);
        cdiComponentProvider.processAnnotatedType(processAnnotatedType);
    }

}
