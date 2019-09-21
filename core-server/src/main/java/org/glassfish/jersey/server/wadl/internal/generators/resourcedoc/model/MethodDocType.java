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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * The documentation type for methods.<br>
 * Created on: Jun 12, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "methodDoc", propOrder = {

})
public class MethodDocType {

    private String methodName;

    @XmlElement(required = false, nillable = false)
    protected String methodSignature;

    protected String commentText;

    private String returnDoc;
    private String returnTypeExample;

    private RequestDocType requestDoc;
    private ResponseDocType responseDoc;

    @XmlElementWrapper(name = "paramDocs")
    protected List<ParamDocType> paramDoc;

    public List<ParamDocType> getParamDocs() {
        if (paramDoc == null) {
            paramDoc = new ArrayList<>();
        }
        return this.paramDoc;
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
     * Get the method name.
     *
     * @return the method name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Set the method name.
     *
     * @param methodName the method name to set.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Get the method signature, or {@code null} if no method signature has been set for the method.
     * <p>
     * This string uniquely identifies the method in case there are multiple methods in the class with the same name.
     * </p>
     *
     * @return unique method signature within a class.
     * @since 2.20
     */
    public String getMethodSignature() {
        return methodSignature;
    }

    /**
     * Set the unique method signature, including method parameters, if any.
     *
     * @param methodSignature method signature string to set.
     * @since 2.20
     */
    public void setMethodSignature(final String methodSignature) {
        this.methodSignature = methodSignature;
    }

    /**
     * Gets the value of the commentText property.
     *
     * @return the commentText
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * Sets the value of the commentText property.
     *
     * @param value the commentText
     */
    public void setCommentText(String value) {
        this.commentText = value;
    }

    /**
     * @return the returnDoc
     */
    public String getReturnDoc() {
        return returnDoc;
    }

    /**
     * @param returnDoc the returnDoc to set
     */
    public void setReturnDoc(String returnDoc) {
        this.returnDoc = returnDoc;
    }

    /**
     * @return the returnTypeExample
     */
    public String getReturnTypeExample() {
        return returnTypeExample;
    }

    /**
     * @param returnTypeExample the returnTypeExample to set
     */
    public void setReturnTypeExample(String returnTypeExample) {
        this.returnTypeExample = returnTypeExample;
    }

    /**
     * @return the requestDoc
     */
    public RequestDocType getRequestDoc() {
        return requestDoc;
    }

    /**
     * @param requestDoc the requestDoc to set
     */
    public void setRequestDoc(RequestDocType requestDoc) {
        this.requestDoc = requestDoc;
    }

    /**
     * @return the responseDoc
     */
    public ResponseDocType getResponseDoc() {
        return responseDoc;
    }

    /**
     * @param responseDoc the responseDoc to set
     */
    public void setResponseDoc(ResponseDocType responseDoc) {
        this.responseDoc = responseDoc;
    }

}
