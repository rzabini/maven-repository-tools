/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Configuration
{
    @Parameter( names = { "-h", "-help" }, help = true, description = "Display the help information." )
    private boolean help;

    @Parameter( names = { "-s", "-sourceUrl" }, description = "URL for the source repository from which artifacts "
        + "are resolved, defaults to https://repo1.maven.org/maven2, example for a Nexus isntall is "
        + "http://localhost:8081/content/groups/public  " )
    private String sourceUrl = "https://repo1.maven.org/maven2";

    @Parameter( names = { "-t", "-targetUrl" }, description = "Folder or URL for the target repository e.g. dist-repo "
        + "or http://localhost:8081/content/repositories/release", required = true )
    private String targetUrl;

    @Parameter( names = { "-a", "-artifactCoordinates" }, description = "GAV coordinates of the desired artifacts in "
        + "the syntax groupId:artifactId[:extension][:classifier]:version|groupId:artifactId[:extension][:classifier]"
        + ":version e.g. org.apache.commons:commons-lang3:3.3.2|com.simpligility.maven:progressive-organization-pom:"
        + "pom:2.3.0|com.google.inject:guice:jar:no_aop:3.0"
        , required = true )
    private String artifactCoordinate;

    @Parameter( names = { "-u", "-username" }, description = "Username for the deployment, if required." )
    private String username;

    @Parameter( names = { "-p", "-password" }, description = "Password for the deployment, if required." )
    private String password;

    @Parameter( names = { "-is", "-includeSources" }, 
                description = "Flag to enable/disable download of sources artifacts. Defaults to true.", 
                arity = 1 )
    private Boolean includeSources = true;

    @Parameter( names = { "-ij", "-includeJavadoc" }, 
                description = "Flag to enable/disable download of javadoc artifacts. Defaults to true.", 
                arity = 1 )
    private Boolean includeJavadoc = true;

    @Parameter( names = { "-cd", "-cacheDirectory" },
                description = "Local directory used as a cache between resolving and deploying, default is local-cache,"
                )
    private String cacheDirectory = "local-cache";

    public boolean getHelp()
    {
        return help;
    }

    public String getSourceUrl()
    {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl)
    {
        this.sourceUrl = sourceUrl;
    }

    public String getTargetUrl() {
        if (targetUrl == null)
            return null;
        if (targetUrl.startsWith("scp") || targetUrl.startsWith("http"))
            return targetUrl;
        else {
            //if url does not start with http (or https..) we assume it is a file path and convert it with
            return new File(targetUrl).toURI().toString();
        }
    }

    public void setTargetUrl(String targetUrl)
    {
        this.targetUrl = targetUrl;
    }

    public String getArtifactCoordinate()
    {
        return artifactCoordinate;
    }

    public void setArtifactCoordinate(String artifactCoordinate)
    {
        this.artifactCoordinate = artifactCoordinate;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Boolean getIncludeSources()
    {
        return includeSources;
    }

    public void setIncludeSources(Boolean includeSources)
    {
        this.includeSources = includeSources;
    }

    public Boolean getIncludeJavadoc()
    {
        return includeJavadoc;
    }

    public void setIncludeJavadoc(Boolean includeJavadoc)
    {
        this.includeJavadoc = includeJavadoc;
    }

    public String getCacheDirectory()
    {
        return cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory)
    {
        this.cacheDirectory = cacheDirectory;
    }

    public List<String> getArtifactCoordinates()
    {
        List<String> coords = Arrays.asList( artifactCoordinate.split( "\\|" ) );
        return coords;
    }
}
