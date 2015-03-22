/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ArtifactRetriever can resolve a depenedencies and all transitive dependencies and upstream parent pom's 
 * for a given GAV coordinate and fill a directory with the respective Maven repository containing those components.
 * 
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class ArtifactRetriever
{

    private static final String JAVADOC = "javadoc";

    private static final String SOURCES = "sources";

    private static final String POM = "pom";

    private static final String JAR = "jar";

    private static Logger logger = LoggerFactory.getLogger( "ArtifactRetriever" );

    private RepositorySystem system;

    private DefaultRepositorySystemSession session;

    private File localRepo;

    //private RemoteRepository sourceRepository;
    private Collection<RemoteRepository> sourceRepositories = new ArrayList<RemoteRepository>();

    public ArtifactRetriever( File localRepo )
    {
        this.localRepo = localRepo;
        initialize();
    }

    private void initialize()
    {
        system = Booter.newRepositorySystem();
        session = Booter.newRepositorySystemSession( system, localRepo );
    }

    public void retrieve( List<String> artifactCoordinates, String sourceUrl, boolean includeSources,
                          boolean includeJavadoc) {
        Collection<String> sourceUrls = new ArrayList<String>();
        sourceUrls.add(sourceUrl);
        retrieve(artifactCoordinates, sourceUrls, includeSources, includeJavadoc);
    }


    public void retrieve(List<String> artifactCoordinates, Collection<String> sourceUrl, boolean includeSources,
                          boolean includeJavadoc )
    {

        for (String url : sourceUrl) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(url, "default", url);
            builder.setProxy(ProxyHelper.getProxy(url));
            sourceRepositories.add(builder.build());
        }

        List<ArtifactResult> artifactResults = getArtifactResults( artifactCoordinates );

        if ( includeSources )
        {
            getSources( artifactResults );
        }
        if ( includeJavadoc )
        {
            getJavadoc( artifactResults );
        }

    }

    private void getSources( List<ArtifactResult> artifactResults )
    {
        getArtifactsWithClassifier( artifactResults, SOURCES );
    }

    private void getJavadoc( List<ArtifactResult> artifactResults )
    {
        getArtifactsWithClassifier( artifactResults, JAVADOC );
    }

    private void getArtifactsWithClassifier( List<ArtifactResult> artifactResults, String classifier )
    {
        if ( artifactResults != null && StringUtils.isNotBlank( classifier ) )
        {
            for ( ArtifactResult artifactResult : artifactResults )
            {
                Artifact mainArtifact = artifactResult.getArtifact();
                if ( isValidRequest( mainArtifact, classifier ) )
                {
                    Artifact classifierArtifact =
                        new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), classifier, JAR,
                                             mainArtifact.getVersion() );

                    ArtifactRequest classifierRequest = new ArtifactRequest();
                    classifierRequest.setArtifact( classifierArtifact );
                    for (RemoteRepository sourceRepository : sourceRepositories)
                        classifierRequest.addRepository(sourceRepository);

                    try
                    {
                        ArtifactResult classifierResult = system.resolveArtifact( session, classifierRequest );
                        logger.info( "Retrieved " + classifierResult.getArtifact().getFile() );
                    }
                    catch ( ArtifactResolutionException e )
                    {
                        logger.info( "ArtifactResolutionException when retrieving " + classifier );
                    }
                }
            }
        }
    }

    /**
     * Determine if a request for a classifier artifact is valid. E.g. javadoc and source for extension pom is deemed 
     * not valid, but default is valid.
     * @param mainArtifact
     * @param classifier
     * @return
     */
    private boolean isValidRequest( Artifact mainArtifact, String classifier )
    {
        boolean isValidRequest = true;
        String extension = mainArtifact.getExtension();
        
        if ( POM.equalsIgnoreCase( extension ) && classifier.endsWith( JAVADOC ) )
        {
            isValidRequest = false;
            logger.info( "Skipping retrieval of javadoc for pom extension" );
        } 
        else if ( POM.equalsIgnoreCase( extension ) && classifier.endsWith( SOURCES ) )
        {
            isValidRequest = false;
            logger.info( "Skipping retrieval of sources for pom extension" );
        }
        return isValidRequest;
    }

    private List<ArtifactResult> getArtifactResults( List<String> artifactCoordinates )
    {

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( String artifactCoordinate : artifactCoordinates )
        {
            artifacts.add( new DefaultArtifact( artifactCoordinate ) );
        }

        List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();
        for ( Artifact artifact : artifacts )
        {
            DependencyFilter depFilter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
            for (RemoteRepository sourceRepository : sourceRepositories)
                collectRequest.addRepository(sourceRepository);

            DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, depFilter );

            try
            {
                DependencyResult resolvedDependencies = system.resolveDependencies( session, dependencyRequest );
                artifactResults.addAll( resolvedDependencies.getArtifactResults() );
            }
            catch ( DependencyResolutionException e )
            {
                logger.info( "DependencyResolutionException " );
            }
        }

        return artifactResults;
    }

}
