package com.github.rzabini.maven.gradle

import com.simpligility.maven.provisioner.MavenRepositoryHelper
import com.simpligility.maven.provisioner.ProxyHelper
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResolutionException
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.slf4j.Logger

public class ArtifactHelper {

    Project project

    File localRepo
    def sourceRepositories = new ArrayList<RemoteRepository>()
    Logger logger

    String targetUrl
    String username
    String password

    //RepositorySystem system
    //def session

    void setProject(Project project) {
        this.project = project
        this.logger = project.logger
        localRepo = new File(project.buildDir, "repo")

        project.repositories.each {
            ArtifactRepository repository ->
                logger.info("adding repository $repository.name , url: $repository.url")
                RemoteRepository.Builder builder = new RemoteRepository.Builder(repository.name, "default", repository.url.toString());
                builder.setProxy(ProxyHelper.getProxy(repository.url.toString()));
                sourceRepositories.add(builder.build());
        }

        //system = Booter.newRepositorySystem();
        //session = Booter.newRepositorySystemSession( system, localRepo );
    }

    public void retrieve() {
        getArtifactResults(
                project.configurations*.dependencies*.collect { dep -> "$dep.group:$dep.name:$dep.version" }.flatten().toSet()
        )
    }

    public void deploy() {
        MavenRepositoryHelper helper = new MavenRepositoryHelper(localRepo);
        helper.deployToRemote(targetUrl, username, password);

    }

    private List<ArtifactResult> getArtifactResults(Collection<String> artifactCoordinates) {

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (String artifactCoordinate : artifactCoordinates) {
            logger.info("resolving: $artifactCoordinate")
            artifacts.add(new DefaultArtifact(artifactCoordinate));
        }

        List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();
        for (Artifact artifact : artifacts) {
            DependencyFilter depFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
            sourceRepositories.each { sourceRepository ->
                collectRequest.addRepository(sourceRepository);
            }

            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, depFilter);

            try {
                DependencyResult resolvedDependencies = system.resolveDependencies(session, dependencyRequest);
                artifactResults.addAll(resolvedDependencies.getArtifactResults());
            }
            catch (DependencyResolutionException e) {
                logger.error("DependencyResolutionException ");
            }
        }

        return artifactResults;
    }

}
