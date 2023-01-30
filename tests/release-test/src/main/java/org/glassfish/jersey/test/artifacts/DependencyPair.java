/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.glassfish.jersey.test.artifacts;

import java.util.Arrays;

class DependencyPair {
    private final String groupId;
    private final String artifactId;

    DependencyPair(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    String artifactId() {
        return artifactId;
    }

    String groupId() {
        return groupId;
    }

    static DependencyPair[] concat(DependencyPair[] first, DependencyPair[] second) {
        DependencyPair[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId;
    }
}
