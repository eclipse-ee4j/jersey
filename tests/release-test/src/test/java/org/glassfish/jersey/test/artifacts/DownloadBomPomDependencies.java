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

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
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
        MavenEnvironment mavenEnvironment = new MavenEnvironment();
        List<Dependency> memberDeps = MavenUtil.streamJerseyJars().collect(Collectors.toList());
        for (Dependency member : memberDeps) {
            Artifact m = mavenEnvironment.resolveArtifact(member);
            System.out.append("Resolved ").append(member.getGroupId()).append(":").append(member.getArtifactId()).append(":")
                    .append(member.getVersion()).append(" to ").println(m.getFile().getName());
            m = mavenEnvironment.resolveSource(member);
            System.out.append("Resolved sources ").append(member.getGroupId()).append(":").append(member.getArtifactId())
                    .append(":").append(member.getVersion()).append(" to ").println(m.getFile().getName());
            m = mavenEnvironment.resolveJavadoc(member);
            System.out.append("Resolved javadoc ").append(member.getGroupId()).append(":").append(member.getArtifactId())
                    .append(":").append(member.getVersion()).append(" to ").println(m.getFile().getName());
        }
    }

    @Test
    public void testDownloadNonBomPomDependencies() throws Exception {
        MavenEnvironment mavenEnvironment = new MavenEnvironment();
        MavenProject project = mavenEnvironment.getMavenProjectForResourceFile("/non-bom-pom-deps.xml");
        for (Dependency dependency : project.getDependencies()) {
            if (dependency.getArtifactId().contains("jackson1") && mavenEnvironment.jerseyVersion.startsWith("3")) {
                continue;
            }

            Artifact m = mavenEnvironment.resolveArtifact(dependency);
            System.out.append("Resolved ").append(dependency.getGroupId()).append(":")
                    .append(dependency.getArtifactId()).append(":")
                    .append(dependency.getVersion()).append(" to ").println(m.getFile().getName());
            m = mavenEnvironment.resolveSource(dependency);
            System.out.append("Resolved source ").append(dependency.getGroupId()).append(":")
                    .append(dependency.getArtifactId()).append(":")
                    .append(dependency.getVersion()).append(" to ").println(m.getFile().getName());
            m = mavenEnvironment.resolveJavadoc(dependency);
            System.out.append("Resolved javadoc ").append(dependency.getGroupId()).append(":")
                    .append(dependency.getArtifactId()).append(":")
                    .append(dependency.getVersion()).append(" to ").println(m.getFile().getName());
        }
    }

    private class MavenEnvironment {
        private final RepositorySystem repositorySystem;
        private final RepositorySystemSession repoSession;
        private final List<RemoteRepository> remoteRepos;
        private final String jerseyVersion;

        MavenEnvironment() throws Exception {
            DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
            locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
            locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

            repositorySystem = locator.getService(RepositorySystem.class);
            repoSession = getRepoSession();

            remoteRepos = getRemoteRepositories();

            Properties properties = MavenUtil.getMavenProperties();
            jerseyVersion = MavenUtil.getJerseyVersion(properties);
        }

        Artifact resolveArtifact(Dependency dependency) throws ArtifactResolutionException {
            dependency.setVersion(jerseyVersion);
            return DependencyResolver.resolveArtifact(dependency, remoteRepos, repositorySystem, repoSession);
        }

        Artifact resolveSource(Dependency dependency) throws ArtifactResolutionException {
            dependency.setVersion(jerseyVersion);
            return DependencyResolver.resolveSource(dependency, remoteRepos, repositorySystem, repoSession);
        }

        Artifact resolveJavadoc(Dependency dependency) throws ArtifactResolutionException {
            dependency.setVersion(jerseyVersion);
            return DependencyResolver.resolveJavadoc(dependency, remoteRepos, repositorySystem, repoSession);
        }

        private List<RemoteRepository> getRemoteRepositories() throws Exception {
            MavenProject project = getMavenProjectForResourceFile("/release-test-pom.xml");
            List<RemoteRepository> remoteArtifactRepositories = project.getRemoteProjectRepositories();
            return remoteArtifactRepositories;
        }

        private MavenProject getMavenProjectForResourceFile(String resourceFile)
                throws Exception {
            File pom = lookupResourcesPom(resourceFile);
            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setPom(pom);
            request.addActiveProfile("staging");
            ProjectBuildingRequest buildingRequest = request
                    .getProjectBuildingRequest()
                    .setRepositorySession(repoSession)
                    .setResolveDependencies(true);

            ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
            ProjectBuildingResult projectBuildingResult = projectBuilder.build(pom, buildingRequest);
            MavenProject project = projectBuildingResult.getProject();

            return project;
        }

        private RepositorySystemSession getRepoSession() {
            DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
            LocalRepository localRepo = new LocalRepository(MavenUtil.getLocalMavenRepository().getAbsolutePath());
            session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
            return session;
        }

        private File lookupResourcesPom(String pomFile) throws URISyntaxException {
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
}
