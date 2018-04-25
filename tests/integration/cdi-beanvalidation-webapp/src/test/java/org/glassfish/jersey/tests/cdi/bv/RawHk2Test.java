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

package org.glassfish.jersey.tests.cdi.bv;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Validation result test for raw HK2 environment.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class RawHk2Test extends BaseValidationTest {

    @Override
    protected Application configure() {
        return ResourceConfig.forApplicationClass(Hk2Application.class);
    }

    @Override
    public String getAppPath() {
        return "hk2";
    }
}
