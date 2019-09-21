/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.wadl.internal.generators;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sun.research.ws.wadl.Doc;

/**
 * The wrapper root document for multiple {@link Doc} elements.
 * Created on: Jun 10, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "applicationDocs", propOrder = {
        "docs"
})
@XmlRootElement(name = "applicationDocs")
public class ApplicationDocs {

    @XmlElement(name = "doc")
    protected List<Doc> docs;

    public List<Doc> getDocs() {
        if (docs == null) {
            docs = new ArrayList<>();
        }
        return this.docs;
    }

}
