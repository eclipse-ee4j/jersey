/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.maven.runner

import org.apache.maven.plugins.annotations.Parameter

/**
 * Common functionality of download Mojos.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
trait CommonDownload implements RunnerMojo {

    /**
     * From where to download the distribution archive file.
     */
    @Parameter(required = true, name = "distUrl", property = "jersey.runner.distUrl")
    String distUrl

    /**
     * Where to download (or look for) the distribution archive file.
     */
    @Parameter(defaultValue = "\${project.build.directory}/distArchive.jar", name = "distTargetLocation")
    String distTargetLocation

    /**
     * Whether to download the distribution archive if it already exists in {@link #distTargetLocation} path.
     */
    @Parameter(defaultValue = "false", property = "jersey.runner.downloadIfExists", name = "downloadIfExists")
    boolean downloadIfExists

    /**
     * Whether to overwrite unpacked distribution in {@link #distDir}; i.e., the {@code <distDir>/<distSubdir>} is removed and the
     * distribution is unpacked.
     */
    @Parameter(defaultValue = "true", property = "jersey.runner.overwrite", name = "overwrite")
    boolean overwrite

    /**
     * The http/https proxy to use
     */
    @Parameter(property = "jersey.runner.proxy", name = "proxy")
    String proxy

    Map commonEnvironment() {
        return [
                "DIST_URL"          : distUrl,
                "DIST_TGT_LOCATION" : distTargetLocation,

                "DOWNLOAD_IF_EXISTS": downloadIfExists as String,
                "OVERWRITE"         : overwrite as String,

                "all_proxy"         : proxy ?: "",
        ]
    }

    void setDistUrl(final String distUrl) {
        this.distUrl = distUrl
    }

    void setDistTargetLocation(final String distTargetLocation) {
        this.distTargetLocation = distTargetLocation
    }

    void setDownloadIfExists(final boolean downloadIfExists) {
        this.downloadIfExists = downloadIfExists
    }

    void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite
    }

    void setProxy(final String proxy) {
        this.proxy = proxy
    }
}
