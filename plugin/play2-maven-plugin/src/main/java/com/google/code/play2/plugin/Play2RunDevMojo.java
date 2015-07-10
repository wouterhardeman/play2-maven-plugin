/*
 * Copyright 2013-2015 Grzegorz Slowikowski (gslowikowski at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.play2.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

//import org.codehaus.plexus.util.DirectoryScanner;



import com.google.code.play2.provider.api.BuildLink;
import com.google.code.play2.provider.api.Play2Provider;
import com.google.code.play2.provider.api.Play2Runner;
import com.google.code.sbt.compiler.api.Analysis;
import com.google.code.sbt.compiler.api.AnalysisProcessor;
import com.google.code.sbt.compiler.api.Compiler;

/**
 * Run Play&#33; server ("play run" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "rundev", requiresDependencyResolution = ResolutionScope.RUNTIME )
public class Play2RunDevMojo
    extends /*AbstractPlay2Mojo*/AbstractPlay2EnhanceMojo/*AbstractPlay2ServerMojo*/
{

    /**
     * Forced Scala version.<br>
     * <br>
     * If specified, this version of Scala compiler is used for compilation.<br>
     * If not specified, version of project's {@code org.scala-lang:scala-library} dependency is used.<br>
     * If there is no {@code org.scala-lang:scala-library} dependency in the project (in Java only projects),
     * selected compiler's {@link Compiler#getDefaultScalaVersion()} is used.
     * FIXME
     * 
     * @since 1.0.0
     */
    @Parameter( property = "scala.version" )
    protected String scalaVersion;

    /**
     * Allows the server startup to be skipped.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play2.runSkip", defaultValue = "false" )
    private boolean runSkip;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( runSkip )
        {
            getLog().info( "Skipping execution" );
            return;
        }

        File baseDir = project.getBasedir();

        // Make separate method for checking conf file (use in "run" and "start" mojos)
        File confDir = new File( baseDir, "conf" );
        if ( !confDir.isDirectory() )
        {
            getLog().info( "Skipping execution" );
            return;
        }
        if ( !new File( confDir, "application.conf" ).isFile() && !new File( confDir, "application.json" ).isFile() )
        {
            getLog().info( "Skipping execution" );
            return;
        }

        List<File> dependencyClasspath = new ArrayList<File>(20);
        Set<?> classPathArtifacts = project.getArtifacts();//TODO-check if classes are not added
        for ( Iterator<?> iter = classPathArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            getLog().debug( String.format( "CP: %s:%s:%s (%s)", artifact.getGroupId(), artifact.getArtifactId(),
                                           artifact.getType(), artifact.getScope() ) );
            //classPath.createPathElement().setLocation( artifact.getFile() );
            dependencyClasspath.add(artifact.getFile()); //TODO - always != null ?
        }

        Play2Provider play2Provider = getProvider();
        Play2Runner play2Runner = play2Provider.getRunner();

        File analysisCacheFile = getAnalysisCacheFile();
        if ( !analysisCacheFile.exists() )
        {
            throw new MojoExecutionException( String.format( "Analysis cache file \"%s\" not found", analysisCacheFile.getAbsolutePath() ) );
        }
        if ( !analysisCacheFile.isFile() )
        {
            throw new MojoExecutionException( String.format( "Analysis cache \"%s\" is not a file", analysisCacheFile.getAbsolutePath() ) );
        }

        AnalysisProcessor sbtAnalysisProcessor = getSbtAnalysisProcessor();

        try
        {
            //Compiler sbtCompiler = getSbtCompiler();
            //String resolvedScalaVersion = getScalaVersion( sbtCompiler );
            String playDocsSuffix = "unknown-scala-version";
            if ( scalaVersion.startsWith( "2.10." ) )
            {
                playDocsSuffix = "2.10";
            }
            else if ( scalaVersion.startsWith( "2.11." ) )
            {
                playDocsSuffix = "2.11";
            }

            Artifact playDocsArtifact =
                getResolvedArtifact( "com.typesafe.play", "play-docs_" + playDocsSuffix, playVersion );
            Set<Artifact> playDocsDependencies = getAllDependencies( playDocsArtifact );
            Map<String, Artifact> uniquePlayDocsDependencies = new HashMap<String, Artifact>();
            uniquePlayDocsDependencies.put( "com.typesafe.play:play-docs_2.10", playDocsArtifact );
            for ( Artifact playDocsDependency : playDocsDependencies )
            {
                String key = playDocsDependency.getGroupId() + ":" + playDocsDependency.getArtifactId();
                Artifact foundArtifact = uniquePlayDocsDependencies.get( key );
                if ( foundArtifact == null )
                {
                    uniquePlayDocsDependencies.put( key, playDocsDependency );
                }
                else
                // maybe newer found
                {
                    if ( foundArtifact.getVersion().compareTo( playDocsDependency.getVersion() ) < 0 )
                    {
                        uniquePlayDocsDependencies.put( key, playDocsDependency );
                    }
                }
            }
            List<File> playDocsClasspath = new ArrayList<File>( uniquePlayDocsDependencies.size() );
            for ( Artifact dependencyArtifact : uniquePlayDocsDependencies.values() )
            {
                playDocsClasspath.add( dependencyArtifact.getFile() );
            }
            /*System.out.println("playDocsDependencies:");
            for (Artifact playDocsDependency: playDocsDependencies)
            {
                System.out.println("- " + playDocsDependency.getFile().getPath());
            }
            System.out.println("unique playDocsDependencies:");
            for (Artifact playDocsDependency: uniquePlayDocsDependencies.values())
            {
                System.out.println("- " + playDocsDependency.getFile().getPath());
            }*/
            play2Runner.runInDevMode( baseDir, new BuildLinkImpl( new File( project.getBuild().getOutputDirectory() ),
                                                                  analysisCacheFile, sbtAnalysisProcessor ),
                                      dependencyClasspath, playDocsClasspath );
        }
        catch ( Throwable e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    private static class BuildLinkImpl
        implements BuildLink
    {
        private File classesDirectory;

        private File analysisCacheFile;

        private AnalysisProcessor sbtAnalysisProcessor;

        public BuildLinkImpl( File classesDirectory, File analysisCacheFile, AnalysisProcessor sbtAnalysisProcessor )
        {
            this.classesDirectory = classesDirectory;
            this.analysisCacheFile = analysisCacheFile;
            this.sbtAnalysisProcessor = sbtAnalysisProcessor;
        }

        @Override
        public Map<String, File> x()
        {
            Map<String, File> result = new HashMap<String, File>();

            String classesDirectoryPath = classesDirectory.getAbsolutePath() + File.separator;
//            Set<File> allSources = getAllSources();
            Analysis analysis = sbtAnalysisProcessor.readFromFile( analysisCacheFile );
            for ( File sourceFile : analysis.getSourceFiles() )
            {
                Set<File> sourceFileProducts = analysis.getProducts( sourceFile );
                for ( File product : sourceFileProducts )
                {
                    String absolutePath = product.getAbsolutePath();
                    if ( absolutePath.contains( "$" ) )
                    {
                        continue; // skip inner and object classes
                    }
                    String relativePath = absolutePath.substring( classesDirectoryPath.length() );
//                    String name = product.getName();
                    String name = relativePath.substring( 0, relativePath.length() - ".class".length() );
                    /*if (name.indexOf( '$' ) > 0)
                    {
                        name = name.substring( 0, name.indexOf( '$' ) );
                    }*/
                    name = name.replace( File.separator, "." );
                    //System.out.println(sourceFile.getPath() + " -> " + name);
                    result.put( name, sourceFile );
                }
                /*String[] definitionNames = analysis.getDefinitionNames( sourceFile );
                Set<String> uniqueDefinitionNames = new HashSet<String>(definitionNames.length);
                for (String definitionName: definitionNames)
                {
                    if ( !uniqueDefinitionNames.contains( definitionName ) )
                    {
                        result.put( definitionName, sourceFile );
//                        System.out.println( "definitionName:'" + definitionName + "', source:'"
//                                        + sourceFile.getAbsolutePath() + "'" );
                        uniqueDefinitionNames.add( definitionName );
                    }
                }*/
            }
            return result;
        }
    }

}
