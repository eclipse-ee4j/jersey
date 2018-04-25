/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.spring;

import java.util.Date;

/**
 * Simple date service that provides actual time and date info.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class DateTimeService {

    /**
     * Get current date and time.
     *
     * @return current date.
     */
    public Date getDateTime() {
        return new Date();
    }
}
