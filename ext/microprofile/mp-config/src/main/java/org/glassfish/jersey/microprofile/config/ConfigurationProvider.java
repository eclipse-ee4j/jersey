/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.glassfish.jersey.spi.ExternalConfigurationModel;
import org.glassfish.jersey.spi.ExternalConfigurationProvider;

import java.util.Map;

public class ConfigurationProvider implements ExternalConfigurationProvider {


    private ConfigurationModel<Config> configModel;

    public ConfigurationProvider() {

        configModel = new ConfigurationModel(ConfigProvider.getConfig());
    }

    @Override
    public Map<String, Object> getProperties() {
        return configModel.getProperties();
    }

    @Override
    public ExternalConfigurationModel getConfiguration() {
        return configModel;
    }

    @Override
    public ExternalConfigurationModel merge(ExternalConfigurationModel input) {
        return input == null ? this.configModel : this.configModel.mergeProperties(input.getProperties());
    }
}
