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

package org.glassfish.jersey.message.internal;

import java.text.ParseException;

/**
 * An acceptable language tag.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AcceptableLanguageTag extends LanguageTag implements Qualified {

    private final int quality;

    public AcceptableLanguageTag(String primaryTag, String subTags) {
        super(primaryTag, subTags);
        this.quality = Quality.DEFAULT;
    }

    public AcceptableLanguageTag(String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    public AcceptableLanguageTag(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        tag = reader.nextToken().toString();
        if (!tag.equals("*")) {
            parse(tag);
        } else {
            primaryTag = tag;
        }

        if (reader.hasNext()) {
            quality = HttpHeaderReader.readQualityFactorParameter(reader);
        } else {
            quality = Quality.DEFAULT;
        }
    }

    @Override
    public int getQuality() {
        return quality;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final AcceptableLanguageTag other = (AcceptableLanguageTag) obj;
        return this.quality == other.quality;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 47 * hash + this.quality;
        return hash;
    }
}
