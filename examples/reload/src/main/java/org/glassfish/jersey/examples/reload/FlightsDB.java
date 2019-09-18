/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.reload;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jakub Podlesak
 */
public class FlightsDB {

    static AtomicInteger departuresReqCount = new AtomicInteger();
    static AtomicInteger arrivalsReqCount = new AtomicInteger();
}
