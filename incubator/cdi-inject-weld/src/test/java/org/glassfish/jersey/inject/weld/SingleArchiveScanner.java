/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.deployment.discovery.DefaultBeanArchiveScanner;
import org.jboss.weld.resources.spi.ResourceLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

class SingleArchiveScanner extends DefaultBeanArchiveScanner {
    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param emptyBeansXmlDiscoveryMode
     */
    public SingleArchiveScanner(ResourceLoader resourceLoader,
                                Bootstrap bootstrap,
                                BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        super(resourceLoader, bootstrap, emptyBeansXmlDiscoveryMode);
    }

    @Override
    public List<ScanResult> scan() {
        HashSet<String> beanArchives = new HashSet<>();
        return super.scan().stream()
                .filter(scanResult -> !beanArchives.contains(scanResult.getBeanArchiveRef().toLowerCase(Locale.ROOT)))
                .peek(scanResult -> beanArchives.add(scanResult.getBeanArchiveRef().toLowerCase(Locale.ROOT)))
                .toList();
    }
}
