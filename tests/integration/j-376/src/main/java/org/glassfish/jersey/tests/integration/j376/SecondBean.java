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

package org.glassfish.jersey.tests.integration.j376;

import javax.enterprise.context.RequestScoped;

/**
 * Bean to be injected into another bean by CDI.
 *
 * The purpose is to test, that CDI and hk2 injections are working together so that one class be injected by
 * both CDI and Jersey/hk2.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@RequestScoped
public class SecondBean {
    private String message = "Hello";

    public String getMessage() {
        return message;
    }

}
