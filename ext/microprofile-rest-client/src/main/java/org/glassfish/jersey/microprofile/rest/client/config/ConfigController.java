/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.rest.client.config;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public final class ConfigController {

    private ConfigController() {
    }

    private static Config getConfig() {
        Config config;
        try {
            config = ConfigProvider.getConfig();
        } catch (ExceptionInInitializerError | IllegalStateException | NoClassDefFoundError ex) {
            // if MicroProfile Config module not found
            config = null;
        }
        return config;
    }

    public static Optional<String> getOptionalValue(String propertyName) {
        return getOptionalValue(propertyName, String.class);
    }

    public static <T> Optional<T> getOptionalValue(String propertyName, Class<T> clazz) {
        Config config = getConfig();
        return config != null ? config.getOptionalValue(propertyName, clazz) : Optional.empty();
    }

    public static String getValue(String propertyName) {
        return getValue(propertyName, String.class);
    }

    public static <T> T getValue(String propertyName, Class<T> clazz) {
        Config config = getConfig();
        return config != null ? config.getValue(propertyName, clazz) : null;
    }

}
