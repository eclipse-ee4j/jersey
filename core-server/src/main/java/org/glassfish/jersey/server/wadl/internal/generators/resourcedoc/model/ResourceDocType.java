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

package org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The documentation type for resources.<br>
 * Created on: Jun 11, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resourceDoc", propOrder = {
        "classDoc"
})
@XmlRootElement(name = "resourceDoc")
public class ResourceDocType {

    @XmlElementWrapper(name = "classDocs")
    protected List<ClassDocType> classDoc;

    public List<ClassDocType> getDocs() {
        if (classDoc == null) {
            classDoc = new ArrayList<ClassDocType>();
        }
        return this.classDoc;
    }

}
