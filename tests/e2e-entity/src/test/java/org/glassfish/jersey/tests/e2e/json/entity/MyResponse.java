/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.annotation.JsonbVisibility;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://test.jaxb.com}myMessage" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://test.jaxb.com}myError"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "myMessage", "myError"
})
@XmlRootElement(name = "myResponse")
@JsonbVisibility(CustomJsonbVisibilityStrategy.class)
public class MyResponse {

    @XmlElement(namespace = "http://test.jaxb.com")
    protected List<MyMessage> myMessage;
    @XmlElement(namespace = "http://test.jaxb.com", required = true)
    protected MyError myError;

    /**
     * Gets the value of the myMessage property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the myMessage property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMyMessage().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MyMessage }
     */
    public List<MyMessage> getMyMessage() {
        if (myMessage == null) {
            myMessage = new ArrayList<>();
        }
        return this.myMessage;
    }

    /**
     * Gets the value of the myError property.
     *
     * @return possible object is
     *         {@link MyError }
     */
    public MyError getMyError() {
        return myError;
    }

    /**
     * Sets the value of the myError property.
     *
     * @param value allowed object is
     * {@link MyError }
     */
    public void setMyError(MyError value) {
        this.myError = value;
    }

    public static Object createTestInstance() {

        MyResponse myResponse = new MyResponse();

        MyMessage msg = new MyMessage();
        msg.setId("0");
        msg.setText("ok");
        MyMessage msg2 = new MyMessage();
        msg2.setId("1");
        msg2.setText("ok");
        myResponse.getMyMessage().add(msg);
        myResponse.getMyMessage().add(msg2);

        MyError err = new MyError();
        err.setId("-1");
        err.setDesc("error");
        myResponse.setMyError(err);
        return myResponse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyResponse other = (MyResponse) obj;
        if (this.myMessage != other.myMessage && (this.myMessage == null || !this.myMessage.equals(other.myMessage))) {
            return false;
        }
        if (this.myError != other.myError && (this.myError == null || !this.myError.equals(other.myError))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.myMessage != null ? this.myMessage.hashCode() : 0);
        hash = 97 * hash + (this.myError != null ? this.myError.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(getMyMessage());
        sb.append(",");
        sb.append(getMyError());
        sb.append("}");
        return sb.toString();
    }
}
