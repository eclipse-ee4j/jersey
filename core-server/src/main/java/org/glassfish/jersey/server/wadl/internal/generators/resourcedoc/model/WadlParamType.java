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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * The documentation type for wadl params.<br>
 * Created on: Jun 12, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wadlParam", propOrder = {

})
public class WadlParamType {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String style;
    @XmlAttribute
    private QName type;
    private String doc;

    /**
     * @return the commentText
     */
    public String getDoc() {
        return doc;
    }

    /**
     * @param commentText the commentText to set
     */
    public void setDoc(String commentText) {
        this.doc = commentText;
    }

    /**
     * @return the className
     */
    public String getName() {
        return name;
    }

    /**
     * @param paramName the className to set
     */
    public void setName(String paramName) {
        this.name = paramName;
    }

    /**
     * @return the style
     */
    public String getStyle() {
        return style;
    }

    /**
     * @param style the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * @return the type
     */
    public QName getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(QName type) {
        this.type = type;
    }
}
