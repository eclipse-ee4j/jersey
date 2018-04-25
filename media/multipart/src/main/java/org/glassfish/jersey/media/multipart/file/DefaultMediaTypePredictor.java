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

package org.glassfish.jersey.media.multipart.file;

import java.io.File;

import javax.ws.rs.core.MediaType;

/**
 * Default implementation of {@link MediaTypePredictor} that uses
 * {@link CommonMediaTypes}.
 *
 * @author Imran M Yousuf (imran at smartitengineering.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class DefaultMediaTypePredictor implements MediaTypePredictor {

    /**
     * This enum represents file extension and MIME types of commonly used file. It
     * is to be noted that all file extension and MIME types are specified in lower
     * case, when checking the extension this should be kept in mind.
     * Currently supported file extension and MIME Types are -
     * <ul>
     *   <li>".xml" - application/xml</li>
     *   <li>".txt" - text/plain</li>
     *   <li>".pdf" - application/pdf</li>
     *   <li>".htm" - text/html</li>
     *   <li>".html" - text/html</li>
     *   <li>".jpg" - image/jpeg</li>
     *   <li>".png" - image/png</li>
     *   <li>".gif" - image/gif</li>
     *   <li>".bmp" - image/bmp</li>
     *   <li>".tar" - application/x-tar</li>
     *   <li>".zip" - application/zip</li>
     *   <li>".gz" - application/x-gzip</li>
     *   <li>".rar" - application/x-rar</li>
     *   <li>".mp3" - audio/mpeg</li>
     *   <li>".wav" - audio/x-wave</li>
     *   <li>".avi" - video/x-msvideo</li>
     *   <li>".mpeg" - video/mpeg</li>
     * </ul>
     */
    public enum CommonMediaTypes {

        XML(".xml", MediaType.APPLICATION_XML_TYPE),
        TXT(".txt", MediaType.TEXT_PLAIN_TYPE),
        HTM(".htm", MediaType.TEXT_HTML_TYPE),
        HTML(".html", MediaType.TEXT_HTML_TYPE),
        PDF(".pdf", new MediaType("application", "pdf")),
        JPG(".jpg", new MediaType("image", "jpeg")),
        PNG(".png", new MediaType("image", "png")),
        GIF(".gif", new MediaType("image", "gif")),
        BMP(".bmp", new MediaType("image", "pdf")),
        TAR(".tar", new MediaType("application", "x-tar")),
        ZIP(".zip", new MediaType("application", "zip")),
        GZ(".gz", new MediaType("application", "x-gzip")),
        RAR(".rar", new MediaType("application", "x-rar")),
        MP3(".mp3", new MediaType("audio", "mpeg")),
        WAV(".wav", new MediaType("audio", "x-wave")),
        AVI(".avi", new MediaType("video", "x-msvideo")),
        MPEG(".mpeg", new MediaType("video", "mpeg"));

        private final String extension;

        private final MediaType mediaType;

        private CommonMediaTypes(final String extension, final MediaType mediaType) {
            if (extension == null || !extension.startsWith(".") || mediaType == null) {
                throw new IllegalArgumentException();
            }
            this.extension = extension;
            this.mediaType = mediaType;
        }

        /**
         * Gets the file extension.
         *
         * @return the file extension.
         */
        public String getExtension() {
            return extension;
        }

        /**
         * Gets the media type.
         *
         * @return the media type.
         */
        public MediaType getMediaType() {
            return mediaType;
        }

        /**
         * A utility method for predicting media type from a file name.
         *
         * @param file the file from which to predict the {@link MediaType}
         * @return the {@link MediaType} for the give file; {@code null} - if file
         *         is null; "application/octet-stream" if extension not recognized.
         *
         * @see CommonMediaTypes#getMediaTypeFromFileName(java.lang.String)
         */
        public static MediaType getMediaTypeFromFile(final File file) {
            if (file == null) {
                return null;
            }
            String fileName = file.getName();
            return getMediaTypeFromFileName(fileName);
        }

        /**
         * A utility method for predicting media type from a file name. If the file
         * name extension is not recognised it will return {@link MediaType} for
         * "*\/*", it will also return the same if the file is {@code null}.
         * Currently supported file extensions can be found at {@link CommonMediaTypes}.
         *
         * @param fileName the file name from which to predict the {@link MediaType}
         * @return the {@link MediaType} for the give file; {@code null} - if file
         *         is null; "application/octet-stream" if extension not recognized.
         */
        public static MediaType getMediaTypeFromFileName(final String fileName) {
            if (fileName == null) {
                return null;
            }
            CommonMediaTypes[] types = CommonMediaTypes.values();
            if (types != null && types.length > 0) {
                for (CommonMediaTypes type : types) {
                    if (fileName.toLowerCase().endsWith(type.getExtension())) {
                        return type.getMediaType();
                    }
                }
            }
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }
    }

    private static final DefaultMediaTypePredictor MEDIA_TYPE_PREDICTOR =
            new DefaultMediaTypePredictor();

    public MediaType getMediaTypeFromFile(File file) {
        return CommonMediaTypes.getMediaTypeFromFile(file);
    }

    public MediaType getMediaTypeFromFileName(String fileName) {
        return CommonMediaTypes.getMediaTypeFromFileName(fileName);
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return the singleton instance.
     */
    public static DefaultMediaTypePredictor getInstance() {
        return MEDIA_TYPE_PREDICTOR;
    }

}
