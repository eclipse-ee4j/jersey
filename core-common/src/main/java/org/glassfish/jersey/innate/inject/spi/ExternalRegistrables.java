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

package org.glassfish.jersey.innate.inject.spi;

import jakarta.ws.rs.RuntimeType;
import java.util.List;

/**
 * Provide a list of classes or interfaces the InjectionManager can support.
 */
public interface ExternalRegistrables {

    /**
     * Contract - RuntimeType pair. For a contract applicable on both client and server, use {@code null} as RuntimeType.
     */
    public static final class ClassRuntimeTypePair {
        private final Class<?> contract;
        private final RuntimeType runtimeType;

        public ClassRuntimeTypePair(Class<?> contract, RuntimeType runtimeType) {
            this.contract = contract;
            this.runtimeType = runtimeType;
        }

        public Class<?> getContract() {
            return contract;
        }

        public RuntimeType getRuntimeType() {
            return runtimeType;
        }
    }

    /**
     * List of contracts that can be registered into Jersey to be passed by the external injection framework.
     * @return list of contracts allowed to be registered in Jersey.
     */
    List<ClassRuntimeTypePair> registrableContracts();
}
