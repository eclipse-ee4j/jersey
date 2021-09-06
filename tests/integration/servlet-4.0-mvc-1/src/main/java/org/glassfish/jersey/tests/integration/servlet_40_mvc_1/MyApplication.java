/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_40_mvc_1;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.tests.integration.servlet_40_mvc_1.resource.ExampleResource;

public class MyApplication extends ResourceConfig {

    public MyApplication() {
        property("jersey.config.server.mvc.templateBasePath.jsp", "/WEB-INF/jsp");
        property("jersey.config.servlet.filter.forwardOn404", "true");
        property("jersey.config.servlet.filter.staticContentRegex", "/WEB-INF/.*\\.jsp");
        packages(ExampleResource.class.getPackage().getName());
        EncodingFilter.enableFor(this, new Class[] {GZipEncoder.class});
        register(JspMvcFeature.class);
      }
}
