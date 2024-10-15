/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.artifacts;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.util.List;

/**
 *
 * @author lukas
 */
final class DependencyResolver {
    static Artifact resolveArtifact(org.apache.maven.model.Dependency d, List<RemoteRepository> remoteRepos,
                                           RepositorySystem repoSystem, RepositorySystemSession repoSession)
            throws ArtifactResolutionException {
        DefaultArtifact artifact = new DefaultArtifact(
                d.getGroupId(), d.getArtifactId(), d.getClassifier(), d.getType(), d.getVersion()
        );
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);
        return repoSystem.resolveArtifact(repoSession, request).getArtifact();
    }

    static Artifact resolveSource(org.apache.maven.model.Dependency d, List<RemoteRepository> remoteRepos,
                                    RepositorySystem repoSystem, RepositorySystemSession repoSession)
            throws ArtifactResolutionException {
        DefaultArtifact artifact = new DefaultArtifact(
                d.getGroupId(), d.getArtifactId(), "sources", d.getType(), d.getVersion()
        );
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);
        return repoSystem.resolveArtifact(repoSession, request).getArtifact();
    }

    static Artifact resolveJavadoc(org.apache.maven.model.Dependency d, List<RemoteRepository> remoteRepos,
                                  RepositorySystem repoSystem, RepositorySystemSession repoSession)
            throws ArtifactResolutionException {
        DefaultArtifact artifact = new DefaultArtifact(
                d.getGroupId(), d.getArtifactId(), "javadoc", d.getType(), d.getVersion()
        );
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);
        return repoSystem.resolveArtifact(repoSession, request).getArtifact();
    }
}