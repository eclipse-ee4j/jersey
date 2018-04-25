/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.extendedwadl.util;

import org.glassfish.jersey.examples.extendedwadl.Item;

/**
 * This class provides example representations that can be used for wadl docs.<br>
 * Created on: Jul 20, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke@freiheit.com)
 */
public class Examples {

    public static final Item SAMPLE_ITEM = new Item();

    static {
        SAMPLE_ITEM.setValue("foo");
    }

}
