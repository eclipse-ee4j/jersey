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

package org.glassfish.jersey.wadl.doclet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;

/**
 * This {@link DocProcessor} wraps multiple {@code DocProcessor}s.
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class DocProcessorWrapper implements DocProcessor {

    private final List<DocProcessor> _docProcessors;

    /**
     * Create new {@code DocProcessorWrapper} instance.
     */
    public DocProcessorWrapper() {
        _docProcessors = new ArrayList<>();
    }

    /**
     * Add a new  {@code DocProcessor} instance to the list of wrapped instances.
     *
     * @param docProcessor  {@code DocProcessor} instance to wrap.
     */
    void add(DocProcessor docProcessor) {
        _docProcessors.add(docProcessor);
    }

    @Override
    public Class<?>[] getRequiredJaxbContextClasses() {
        final List<Class<?>> result = new ArrayList<>();
        for (DocProcessor docProcessor : _docProcessors) {
            final Class<?>[] requiredJaxbContextClasses = docProcessor.getRequiredJaxbContextClasses();
            if (requiredJaxbContextClasses != null && requiredJaxbContextClasses.length > 0) {
                result.addAll(Arrays.asList(requiredJaxbContextClasses));
            }
        }
        return result.toArray(new Class<?>[result.size()]);
    }

    @Override
    public String[] getCDataElements() {
        final List<String> result = new ArrayList<>();
        for (DocProcessor docProcessor : _docProcessors) {
            final String[] cdataElements = docProcessor.getCDataElements();
            if (cdataElements != null && cdataElements.length > 0) {
                result.addAll(Arrays.asList(cdataElements));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public void processClassDoc(ClassDoc classDoc, ClassDocType classDocType) {
        for (DocProcessor docProcessor : _docProcessors) {
            docProcessor.processClassDoc(classDoc, classDocType);
        }
    }

    @Override
    public void processMethodDoc(MethodDoc methodDoc,
                                 MethodDocType methodDocType) {
        for (DocProcessor docProcessor : _docProcessors) {
            docProcessor.processMethodDoc(methodDoc, methodDocType);
        }
    }

    @Override
    public void processParamTag(ParamTag paramTag, Parameter parameter,
                                ParamDocType paramDocType) {
        for (DocProcessor docProcessor : _docProcessors) {
            docProcessor.processParamTag(paramTag, parameter, paramDocType);
        }
    }

}
