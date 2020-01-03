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

import org.glassfish.jersey.ExtendedConfig;

import java.util.Map;
import java.util.Optional;

/**
 * Model of configuration for external properties. Requires certain utilities methods to be implemented
 * @param <CONFIG> type of an external config
 */
public interface ExternalConfigurationModel<CONFIG> extends ExtendedConfig {

    /**
     * Get value of a property as a definite type
     *
     * property shall exist in order for this method to be used. Otherwise exception is thrown
     *
     * @param name property name
     * @param clazz class type of an expected value
     * @param <T> type of an expected value
     * @return value of an expected type
     */
    <T> T as(String name, Class<T> clazz);

    /**
     * Get value of a property as a definite type
     *
     * property may not exist, an empty Optional object is returned in case of an empty property
     *
     * @param name property name
     * @param clazz class type of an expected value
     * @param <T> type of an expected value
     * @return Optional object filled by a value of an expected type or by the NULL value (
     */
    <T> Optional<T> getOptionalProperty(String name, Class<T> clazz);

    /**
     * Merge properties from other (found) external configuration.
     *
     * @param inputProperties those properties will be merged into ours
     * @return current instance of the model
     */
    ExternalConfigurationModel mergeProperties(Map<String, Object> inputProperties);

    /**
     * Obtain config object
     *
     * @return external config provider
     */
    CONFIG getConfig();
}
