/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.thymeleaf;

import org.thymeleaf.TemplateEngine;

/**
 * {@link ThymeleafConfigurationFactory} that supplies an unchanged
 * {@link ThymeleafConfigurationFactory Configuration} as passed-in to
 * the constructor.
 * <p/>
 * Used to support backwards-compatibility in {@link ThymeleafViewProcessor}
 * to wrap directly-configured {@link org.thymeleaf.TemplateEngine}
 * objects instead of the recommended {@link ThymeleafDefaultConfigurationFactory}
 * or a sub-class thereof.
 *
 * @author Dmytro Dovnar (dimonmc@gmail.com)
 */
public class ThymeleafSuppliedConfigurationFactory implements ThymeleafConfigurationFactory {
    private final ThymeleafConfigurationFactory configurationFactory;

    public ThymeleafSuppliedConfigurationFactory(ThymeleafConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    @Override
    public TemplateEngine getTemplateEngine() {
        return configurationFactory.getTemplateEngine();
    }

}
