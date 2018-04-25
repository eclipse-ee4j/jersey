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

package org.glassfish.jersey.server.mvc.freemarker;

import freemarker.template.Configuration;


/**
 * Provides lookup of {@link freemarker.template.Configuration Configuration}
 * instance for Freemarker templating.
 * </p>
 * Instantiation of Configuration objects is relatively heavy-weight, and
 * Freemarker best-practices dictate that they be reused if possible.
 * Therefore, most implementations of this interface will only create a
 * singleton Configuration instance, and return it for every call to
 * {@link #getConfiguration()}. Although this will usually be the case, it is
 * not a guarantee of this interface's contract.
 *
 * @author Jeff Wilde (jeff.wilde at complicatedrobot.com)
 */
public interface FreemarkerConfigurationFactory {

    public Configuration getConfiguration();

}
