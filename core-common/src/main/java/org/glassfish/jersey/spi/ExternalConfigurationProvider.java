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


package org.glassfish.jersey.spi;

import java.util.Map;

/**
 * Provider interface for external (SPI) providers to provide
 * their configuration properties implementations
 *
 * Priority of providers can be adjusted by Priority annotation
 * or just alphabetically (if no Provider annotation is found)
 */
public interface ExternalConfigurationProvider {

    /**
     * Map of properties from the model (external config file)
     *
     * @return Map of properties loaded by a model from config file
     */
    Map<String, Object> getProperties();

    /**
     * obrain model object which has direct access to external configuration
     *
     * @return model of external properties
     */
    ExternalConfigurationModel getConfiguration();

    /**
     * Merge properties from other provider/model
     *
     * @param input those properties will be merged into ours
     * @return current instance of provider
     */
    ExternalConfigurationModel merge(ExternalConfigurationModel input);
}
