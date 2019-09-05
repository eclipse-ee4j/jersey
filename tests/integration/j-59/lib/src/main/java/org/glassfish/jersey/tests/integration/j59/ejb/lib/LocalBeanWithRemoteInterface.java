/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j59.ejb.lib;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

/**
 * Local stateless session bean implementing a remote interface.
 * Part of CDI extension lookup issue reproducer.
 *
 * @author Jakub Podlesak
 */
@Stateless
@LocalBean
@Remote(MyRemoteInterface.class)
public class LocalBeanWithRemoteInterface {

    /**
     * Simple getter to be invoked from a CDI backed JAX-RS resource.
     *
     * @return Josh string literal.
     */
    public String getName() {
        return "Josh";
    }
}
