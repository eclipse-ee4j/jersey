/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2031;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

/**
 * JAX-RS application for the JERSEY-2031 reproducer test.
 *
 * @author Michal Gajdos
 */
public class Jersey2031 extends ResourceConfig {

    public Jersey2031() {
        register(Issue2031Resource.class);
        register(JspMvcFeature.class);
    }
}
