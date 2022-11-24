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

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class DownloadBomPomDependencies extends AbstractMojoTestCase {

    @Test
    public void testDownloadBomPomDependencies() throws Exception {
//        RepositorySystem repositorySystem = (RepositorySystem) lookup(RepositorySystem.class.getName());
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);

        RepositorySystemSession repoSession = getRepoSession(repositorySystem);
        List<RemoteRepository> remoteRepos = getRemoteRepositories(repoSession);

        Properties properties = MavenUtil.getMavenProperties();
        String jerseyVersion = MavenUtil.getJerseyVersion(properties);
        List<Dependency> memberDeps = MavenUtil.streamJerseyJars().collect(Collectors.toList());
        for (Dependency member : memberDeps) {
            member.setVersion(jerseyVersion);
            Artifact m = DependencyResolver.resolveArtifact(member, remoteRepos, repositorySystem, repoSession);
            System.out.append("Resolved ").append(member.getGroupId()).append(":").append(member.getArtifactId()).append(":")
                    .append(member.getVersion()).append(" to ").println(m.getFile().getName());
        }
    }

    private List<RemoteRepository> getRemoteRepositories(RepositorySystemSession session) throws Exception {
        File pom = lookupResourcesPom("/release-test-pom.xml");
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setPom(pom);
        request.addActiveProfile("staging");
        ProjectBuildingRequest buildingRequest = request
                .getProjectBuildingRequest()
                .setRepositorySession(session)
                .setResolveDependencies(true);

        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
        ProjectBuildingResult projectBuildingResult = projectBuilder.build(pom, buildingRequest);
        MavenProject project = projectBuildingResult.getProject();

        List<RemoteRepository> remoteArtifactRepositories = project.getRemoteProjectRepositories();
        return remoteArtifactRepositories;
    }

    private static RepositorySystemSession getRepoSession(RepositorySystem repositorySystem) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(MavenUtil.getLocalMavenRepository().getAbsolutePath());
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    private static File lookupResourcesPom(String pomFile) throws URISyntaxException {
        URL resource = DownloadBomPomDependencies.class.getResource(pomFile);
        if (resource == null) {
            throw new IllegalStateException("Pom file " + pomFile + " was not located on classpath!");
        }
        File file = new File(resource.toURI());
        if (!file.exists()) {
            throw new IllegalStateException("Cannot locate test pom xml file!");
        }
        return file;
    }
}
