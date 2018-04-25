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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * The documentation type for params: method params, path params on a class etc.<br>
 * Created on: Jun 12, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paramDoc", propOrder = {

})
public class ParamDocType {

    private String paramName;
    private String commentText;

    public ParamDocType() {
    }

    public ParamDocType(String paramName, String commentText) {
        this.paramName = paramName;
        this.commentText = commentText;
    }

    @XmlElementWrapper(name = "annotationDocs")
    protected List<AnnotationDocType> annotationDoc;

    public List<AnnotationDocType> getAnnotationDocs() {
        if (annotationDoc == null) {
            annotationDoc = new ArrayList<>();
        }
        return this.annotationDoc;
    }

    @XmlAnyElement(lax = true)
    private List<Object> any;

    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<>();
        }
        return this.any;
    }

    /**
     * @return the commentText
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * @param commentText the commentText to set
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * @return the className
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @param paramName the className to set
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}
