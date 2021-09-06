/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTrees;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.xml.namespace.QName;

import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.AnnotationDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.NamedValueType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.RepresentationDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.RequestDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResourceDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResponseDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.WadlParamType;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Creates a resourcedoc XML file.
 * <p/>
 * <p>
 * The ResourceDoc file contains the javadoc documentation of resource classes,
 * so that this can be used for extending generated wadl with useful
 * documentation.
 * </p>
 *
 * @author <a href="mailto:jorge.bescos.gascon@oracle.com">Jorge Bescos
 *         Gascon</a>
 */
public class ResourceDoclet implements Doclet {

    private static final Logger LOG = Logger.getLogger(ResourceDoclet.class.getName());
    private static final Pattern PATTERN_RESPONSE_REPRESENTATION = Pattern.compile("@response\\.representation\\.([\\d]+)\\..*");
    private static final Pattern PATTERN_INLINE_TAG = Pattern
            .compile("(?!\\{)[\\w\\?\\!\\#\\<\\>\\.\\ \\/\\:\\\\-\\{\\}]+(?=\\})");
    private static final String COMA = ", ";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private final OptionOutput optionOutput = new OptionOutput();
    private final OptionClasspath optionClasspath = new OptionClasspath();
    private final OptionDocprocessor optionDocprocessor = new OptionDocprocessor();

    @Override
    public void init(Locale locale, Reporter reporter) {
        reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
    }

    private String getComments(DocCommentTree docCommentTree) {
        if (docCommentTree != null) {
            StringBuilder body = new StringBuilder();
            docCommentTree.getFullBody().forEach(doc -> body.append(doc.toString()));
            return body.toString();
        } else {
            return EMPTY;
        }
    }

    private Map<DocTree.Kind, Map<String, String>> getTags(DocCommentTree docCommentTree) {
        Map<DocTree.Kind, Map<String, String>> tags = new HashMap<>();
        if (docCommentTree != null) {
            for (DocTree tag : docCommentTree.getBlockTags()) {
                Map<String, String> tagsInKind = tags.get(tag.getKind());
                if (tagsInKind == null) {
                    tagsInKind = new HashMap<>();
                    tags.put(tag.getKind(), tagsInKind);
                }
                String[] kindValuePair = getTagPair(tag.toString());
                if (tag.getKind() == DocTree.Kind.PARAM) {
                    // Adds the parameter name and description
                    String[] paramValuePair = getTagPair(kindValuePair[1]);
                    tagsInKind.put(paramValuePair[0], paramValuePair[1]);
                } else {
                    // Adds the @tag name and description
                    tagsInKind.put(kindValuePair[0], kindValuePair[1]);
                }
            }
        }
        return tags;
    }

    private String[] getTagPair(String tag) {
        String[] pair = tag.split(SPACE, 2);
        if (pair.length != 2) {
            pair = new String[]{pair[0], null};
        }
        return pair;
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        boolean success = true;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader ncl = new Loader(optionClasspath.getClasspathElements(), ResourceDoclet.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(ncl);
            DocProcessorWrapper docProcessor = new DocProcessorWrapper();
            if (optionDocprocessor.getDocProcessors().length != 0) {
                try {
                    Class<?> clazz = Class.forName(optionDocprocessor.getDocProcessors()[0], true,
                            Thread.currentThread().getContextClassLoader());
                    Class<? extends DocProcessor> dpClazz = clazz.asSubclass(DocProcessor.class);
                    docProcessor.add((DocProcessor) dpClazz.getDeclaredConstructors()[0].newInstance());
                } catch (Exception e) {
                    LOG.log(Level.SEVERE,
                            "Could not load docProcessors " + Arrays.asList(optionDocprocessor.getDocProcessors()), e);
                }
            }
            ResourceDocType result = new ResourceDocType();
            for (TypeElement element : ElementFilter.typesIn(docEnv.getIncludedElements())) {
                DocTrees docTrees = docEnv.getDocTrees();
                DocCommentTree docCommentTree = docTrees.getDocCommentTree(element);
                if (docCommentTree != null) {
                    ClassDocType classDocType = new ClassDocType();
                    classDocType.setClassName(element.getQualifiedName().toString());
                    classDocType.setCommentText(getComments(docCommentTree));
                    docProcessor.processClassDoc(element, classDocType);
                    for (ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
                        Map<DocTree.Kind, Map<String, String>> tags = getTags(docTrees.getDocCommentTree(method));
                        MethodTree methodTree = docTrees.getTree(method);
                        MethodDocType methodDocType = new MethodDocType();
                        methodDocType.setMethodName(methodTree.getName().toString());
                        methodDocType.setCommentText(getComments(docTrees.getDocCommentTree(method)));
                        getTags(docTrees.getDocCommentTree(method));
                        StringBuilder arguments = new StringBuilder("(");
                        Map<String, String> paramTags = tags.get(DocTree.Kind.PARAM);
                        for (VariableElement parameter : method.getParameters()) {
                            ParamDocType paramDocType = buildParamDocType(parameter, paramTags);
                            arguments.append(parameter.asType()).append(COMA);
                            if (paramDocType != null) {
                                methodDocType.getParamDocs().add(paramDocType);
                                docProcessor.processParamTag(parameter, paramDocType);
                            }
                        }
                        // Remove last comma if there are parameters
                        if (arguments.length() != 1) {
                            arguments.delete(arguments.length() - COMA.length(), arguments.length());
                        }
                        arguments.append(")");
                        methodDocType.setMethodSignature(arguments.toString());
                        docProcessor.processMethodDoc(method, methodDocType);
                        methodDocType.setRequestDoc(buildRequestDocType(tags));
                        methodDocType.setResponseDoc(buildResponseDocType(tags));
                        classDocType.getMethodDocs().add(methodDocType);
                    }
                    result.getDocs().add(classDocType);
                    success = DocletUtils.createOutputFile(optionOutput.getValue(), docProcessor, result);
                    
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return success;
    }

    private ParamDocType buildParamDocType(VariableElement parameter, Map<String, String> paramTags) {
        if (paramTags != null) {
            ParamDocType paramDocType = new ParamDocType();
            paramDocType.setParamName(parameter.getSimpleName().toString());
            paramDocType.setCommentText(paramTags.get(paramDocType.getParamName()));
            for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
                AnnotationDocType annotationDocType = new AnnotationDocType();
                annotationDocType.setAnnotationTypeName(annotation.getAnnotationType().toString());
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> pair : annotation.getElementValues()
                        .entrySet()) {
                    NamedValueType namedValueType = new NamedValueType();
                    namedValueType.setName(pair.getKey().getSimpleName().toString());
                    namedValueType.setValue(pair.getValue().getValue().toString());
                    annotationDocType.getAttributeDocs().add(namedValueType);
                }
                paramDocType.getAnnotationDocs().add(annotationDocType);
            }
            return paramDocType;
        }
        return null;
    }

    private RequestDocType buildRequestDocType(Map<DocTree.Kind, Map<String, String>> tags) {
        Map<String, String> customTags = tags.get(DocTree.Kind.UNKNOWN_BLOCK_TAG);
        if (customTags != null) {
            RequestDocType requestDoc = new RequestDocType();
            RepresentationDocType representationDoc = new RepresentationDocType();
            String qname = customTags.get("@request.representation.qname");
            String example = customTags.get("@request.representation.example");
            if (qname != null) {
                representationDoc.setElement(QName.valueOf(qname));
            }
            if (example != null) {
                representationDoc.setExample(getSerializedExample(example));
            }
            if (qname != null || example != null) {
                requestDoc.setRepresentationDoc(representationDoc);
                return requestDoc;
            }
        }
        return null;
    }

    private ResponseDocType buildResponseDocType(Map<DocTree.Kind, Map<String, String>> tags) {
        ResponseDocType responseDoc = new ResponseDocType();
        Map<String, String> returnDoc = tags.get(DocTree.Kind.RETURN);
        if (returnDoc != null) {
            responseDoc.setReturnDoc(returnDoc.get("@return"));
        }
        Map<String, String> customTags = tags.get(DocTree.Kind.UNKNOWN_BLOCK_TAG);
        if (customTags != null) {
            String responseParam = customTags.remove("@response.param");
            if (responseParam != null) {
                Matcher matcher = PATTERN_INLINE_TAG.matcher(responseParam);
                WadlParamType wadlParam = new WadlParamType();
                while (matcher.find()) {
                    String group = matcher.group();
                    String[] pair = getTagPair(group);
                    switch (pair[0]) {
                    case "name":
                        wadlParam.setName(pair[1]);
                        break;
                    case "style":
                        wadlParam.setStyle(pair[1]);
                        break;
                    case "type":
                        wadlParam.setType(QName.valueOf(pair[1]));
                        break;
                    case "doc":
                        wadlParam.setDoc(pair[1]);
                        break;
                    default:
                        LOG.warning("Unknown inline tag of @response.param: @" + pair[0] + " (value: " + pair[1] + ")");
                        break;
                    }
                }
                responseDoc.getWadlParams().add(wadlParam);
            }
            Map<Long, RepresentationDocType> groupedRepresentationDocType = new HashMap<>();
            for (Entry<String, String> entry : customTags.entrySet()) {
                if (entry.getKey().startsWith("@response.representation")) {
                    String[] keySplit = entry.getKey().split("\\.");
                    long httpCode = Long.parseLong(keySplit[2]);
                    RepresentationDocType representationDoc = groupedRepresentationDocType.get(httpCode);
                    if (representationDoc == null) {
                        representationDoc = new RepresentationDocType();
                        representationDoc.setStatus(httpCode);
                        groupedRepresentationDocType.put(httpCode, representationDoc);
                    }
                    if ("qname".equals(keySplit[3])) {
                        representationDoc.setElement(QName.valueOf(entry.getValue()));
                    } else if ("mediaType".equals(keySplit[3])) {
                        representationDoc.setMediaType(entry.getValue());
                    } else if ("example".equals(getSerializedExample(keySplit[3]))) {
                        representationDoc.setExample(getSerializedExample(entry.getValue()));
                    } else if ("doc".equals(keySplit[3])) {
                        representationDoc.setDoc(entry.getValue());
                    } else {
                        LOG.warning("Unknown response representation tag " + entry.getKey());
                    }
                }
            }
            responseDoc.getRepresentations().addAll(groupedRepresentationDocType.values());
        }
        return responseDoc;
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return new HashSet<>(Arrays.asList(optionOutput, optionClasspath, optionDocprocessor));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private String getSerializedExample(String tag) {
        if (tag != null) {
            Matcher matcher = PATTERN_INLINE_TAG.matcher(tag);
            while (matcher.find()) {
                String group = matcher.group();
                String[] pair = getTagPair(group);
                if ("link".equals(pair[0])) {
                    String[] classAndField = pair[1].split("#");
                    return DocletUtils.getLinkClass(classAndField[0], classAndField[1]);
                } else {
                    return pair[1];
                }
            }
        }
        return tag;
    }

}
