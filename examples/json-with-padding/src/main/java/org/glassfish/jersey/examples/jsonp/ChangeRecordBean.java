/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@XmlRootElement(name = "change")
public class ChangeRecordBean {

    @XmlAttribute
    public boolean madeByAdmin;
    public int linesChanged;
    public String logMessage;

    /**
     * No-arg constructor for JAXB
     */
    public ChangeRecordBean() {}

    public ChangeRecordBean(boolean madeByAdmin, int linesChanged, String logMessage) {
        this.madeByAdmin = madeByAdmin;
        this.linesChanged = linesChanged;
        this.logMessage = logMessage;
    }
}
