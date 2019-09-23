/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jettison;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak
 * @author Marek Potociar
 */
@XmlRootElement
public class AircraftType {
    public String type;
    public double length;
    public int seatingCapacity;

    public AircraftType(){}

    public AircraftType(String type, double length, int seatingCapacity) {
        this.type = type;
        this.length = length;
        this.seatingCapacity = seatingCapacity;
    }
}
