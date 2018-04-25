/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

/**
 * Data aggregator for listening for events aggregated based on give keywords.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface DataAggregator {
    void start(String keywords, DataListener msgListener);

    void stop();
}
