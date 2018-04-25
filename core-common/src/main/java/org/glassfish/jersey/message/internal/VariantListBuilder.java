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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

/**
 * An implementation of {@link VariantListBuilder}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class VariantListBuilder extends Variant.VariantListBuilder {

    private List<Variant> variants;
    private final List<MediaType> mediaTypes = new ArrayList<MediaType>();
    private final List<Locale> languages = new ArrayList<Locale>();
    private final List<String> encodings = new ArrayList<String>();

    @Override
    public List<Variant> build() {
        if (!mediaTypes.isEmpty() || !languages.isEmpty() || !encodings.isEmpty()) {
            // if current state is not empty, add combinations to the variant list
            add();
        }
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }

        return variants;
    }

    @Override
    public VariantListBuilder add() {
        if (variants == null) {
            variants = new ArrayList<Variant>();
        }

        addMediaTypes();

        languages.clear();
        encodings.clear();
        mediaTypes.clear();

        return this;
    }

    private void addMediaTypes() {
        if (mediaTypes.isEmpty()) {
            addLanguages(null);
        } else {
            for (MediaType mediaType : mediaTypes) {
                addLanguages(mediaType);
            }
        }
    }

    private void addLanguages(MediaType mediaType) {
        if (languages.isEmpty()) {
            addEncodings(mediaType, null);
        } else {
            for (Locale language : languages) {
                addEncodings(mediaType, language);
            }
        }
    }

    private void addEncodings(MediaType mediaType, Locale language) {
        if (encodings.isEmpty()) {
            addVariant(mediaType, language, null);
        } else {
            for (String encoding : encodings) {
                addVariant(mediaType, language, encoding);
            }
        }
    }

    private void addVariant(MediaType mediaType, Locale language, String encoding) {
        variants.add(new Variant(mediaType, language, encoding));
    }

    @Override
    public VariantListBuilder languages(Locale... languages) {
        this.languages.addAll(Arrays.asList(languages));
        return this;
    }

    @Override
    public VariantListBuilder encodings(String... encodings) {
        this.encodings.addAll(Arrays.asList(encodings));
        return this;
    }

    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        this.mediaTypes.addAll(Arrays.asList(mediaTypes));
        return this;
    }
}
