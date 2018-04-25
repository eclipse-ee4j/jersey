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
 * {@link FreemarkerConfigurationFactory} that supplies an unchanged
 * {@link freemarker.template.Configuration Configuration} as passed-in to
 * the constructor.
 * <p/>
 * Used to support backwards-compatibility in {@link FreemarkerViewProcessor}
 * to wrap directly-configured {@link freemarker.template.Configuration Configuration}
 * objects instead of the recommended {@link FreemarkerDefaultConfigurationFactory}
 * or a sub-class thereof.
 *
 * @author Jeff Wilde (jeff.wilde at complicatedrobot.com)
 */
final class FreemarkerSuppliedConfigurationFactory implements FreemarkerConfigurationFactory {

    private final Configuration configuration;

    public FreemarkerSuppliedConfigurationFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
