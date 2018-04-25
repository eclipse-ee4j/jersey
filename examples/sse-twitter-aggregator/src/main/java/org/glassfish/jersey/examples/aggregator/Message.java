/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

import javax.xml.bind.annotation.XmlElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Message bean.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Message {

    @XmlElement
    private String text;

    @XmlPath("user/profile_image_url/text()")
    private String profileImg;

    @XmlElement(nillable = true)
    private String rgbColor;

    public Message() {
    }

    public Message(final String text, final String rgbColor, final String profileImg) {
        this.text = text;
        this.rgbColor = rgbColor;
        this.profileImg = profileImg;
    }

    public String getText() {
        return text;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public String getRgbColor() {
        return rgbColor;
    }

    public void setRgbColor(String rgbColor) {
        this.rgbColor = rgbColor;
    }

    @Override
    public String toString() {
        return "Message{"
                + "text='" + text + '\''
                + ", profileImg='" + profileImg + '\''
                + ", rgpColor='" + rgbColor + '\''
                + '}';
    }
}
