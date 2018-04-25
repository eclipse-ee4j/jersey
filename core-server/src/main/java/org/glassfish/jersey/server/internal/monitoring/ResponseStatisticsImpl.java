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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.monitoring.ResponseStatistics;

/**
 * Immutable response statistics.
 *
 * @author Miroslav Fuksa
 */
final class ResponseStatisticsImpl implements ResponseStatistics {

    private final Map<Integer, Long> responseCodes;
    private final Integer lastResponseCode;

    /**
     * This builder does not need to be threadsafe since it's called only from the jersey-background-task-scheduler.
     */
    static class Builder {

        private final Map<Integer, Long> responseCodesMap = new HashMap<>();
        private Integer lastResponseCode = null;

        private ResponseStatisticsImpl cached = null;

        void addResponseCode(final int responseCode) {
            cached = null;

            lastResponseCode = responseCode;
            Long currentValue = responseCodesMap.get(responseCode);
            if (currentValue == null) {
                currentValue = 0L;
            }
            responseCodesMap.put(responseCode, currentValue + 1);
        }

        ResponseStatisticsImpl build() {
            if (cached == null) {
                cached = new ResponseStatisticsImpl(lastResponseCode, new HashMap<>(this.responseCodesMap));
            }

            return cached;
        }

    }

    private ResponseStatisticsImpl(final Integer lastResponseCode, final Map<Integer, Long> responseCodes) {
        this.lastResponseCode = lastResponseCode;
        this.responseCodes = Collections.unmodifiableMap(responseCodes);
    }

    @Override
    public Integer getLastResponseCode() {
        return lastResponseCode;
    }

    @Override
    public Map<Integer, Long> getResponseCodes() {
        return responseCodes;
    }

    @Override
    public ResponseStatistics snapshot() {
        // this object is immutable
        return this;
    }
}
