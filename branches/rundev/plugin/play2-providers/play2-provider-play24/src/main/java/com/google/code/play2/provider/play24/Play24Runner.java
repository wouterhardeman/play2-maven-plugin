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

package com.google.code.play2.provider.play24;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.code.play2.provider.api.BuildLink;
import com.google.code.play2.provider.api.Play2Runner;

import com.google.code.play2.provider.play24.run.Asset;
import com.google.code.play2.provider.play24.run.AssetsClassLoaderCreatorImpl;
import com.google.code.play2.provider.play24.run.ClassLoaderCreator;
import com.google.code.play2.provider.play24.run.DelegatedResourcesClassLoaderCreator;
import com.google.code.play2.provider.play24.run.DevSetting;
import com.google.code.play2.provider.play24.run.DummyPlayWatchService;
//import com.google.code.play2.provider.play24.run.LoggerProxyImpl;
import com.google.code.play2.provider.play24.run.NamedURLClassLoaderCreator;
import com.google.code.play2.provider.play24.run.FileWatchService;
import com.google.code.play2.provider.play24.run.ReloadCompile;
import com.google.code.play2.provider.play24.run.ReloadCompileImpl;
import com.google.code.play2.provider.play24.run.Reloader;
//import com.google.code.play2.provider.play24.run.RunSbtTask;

public class Play24Runner
    implements Play2Runner
{
    @Override
    public String getProdServerMainClass()
    {
        return "play.core.server.ProdServerStart";
    }

    @Override
    public String getDevServerMainClass()
    {
        return getProdServerMainClass();
    }

    @Override
    public void runInDevMode(File baseDirectory, BuildLink buildLink, List<File> dependencyClasspath, List<File> docsClasspath) throws Throwable
    {
        //com.google.code.play2.provider.play24.run.Play24DevModeRunner.run( dependencyClasspath );

        //ClassLoader debugLoader = Play24DevModeRunner.class.getClassLoader();
        //System.out.println("debugLoader:"+debugLoader);
        
//        Seq<RunHook> runHooks = JavaConversions.asScalaBuffer( Collections.<RunHook>emptyList() );
        List<String> javaOptions = Collections.<String>emptyList();
//        Seq<File> dependencyClasspathSeq = JavaConversions.asScalaBuffer( dependencyClasspath );
        /*Function3<String, URL[], ClassLoader, ClassLoader>*/ClassLoaderCreator dependencyClassLoader = new NamedURLClassLoaderCreator();
        /*Function0<Reloader.CompileResult>*/ReloadCompile reloadCompile = new ReloadCompileImpl(buildLink);
        /*Function3<String, URL[], ClassLoader, ClassLoader>*/ClassLoaderCreator reloaderClassLoader = new DelegatedResourcesClassLoaderCreator();

        List/*<Tuple2<String, File>>*/<Asset> allAssets = new ArrayList/*<Tuple2<String, File>>*/<Asset>(1);
        allAssets.add( new /*Tuple2<String, File>*/Asset("public/", new File(baseDirectory, "public")) ); // file should be new File("target/web/public/main")
        AssetsClassLoaderCreatorImpl/*Function1<ClassLoader, ClassLoader>*/ assetsClassLoader = new AssetsClassLoaderCreatorImpl( allAssets/*JavaConversions.asScalaBuffer( allAssets )*/ );

        ClassLoader commonClassLoader = Reloader.commonClassLoader(dependencyClasspath);

        List<File> monitoredFiles = new ArrayList<File>(4);
        monitoredFiles.add(new File(baseDirectory, "app"));
        monitoredFiles.add(new File(baseDirectory, "conf"));
        monitoredFiles.add(new File(baseDirectory, "app/assets"));
        monitoredFiles.add(new File(baseDirectory, "public"));
//        Seq<String> monitoredFilesSeq = JavaConversions.asScalaBuffer( monitoredFiles/*Collections.<String>emptyList()*/ );

//        LoggerProxy loggerProxy = new LoggerProxyImpl();
        FileWatchService playWatchService = new DummyPlayWatchService();//PlayWatchService$.MODULE$._mthdefault("target.value", 500 /* SBT 0.13.8 Defaults.scala L.147 */, lp/*new LoggerProxyImpl()*//*sLog.value -  LoggerProxy*/);
        // PlayWatchService playWatchService = new SbtPlayWatchService(pollDelayMillis)
        //PlayWatchService playWatchService = PlayWatchService$.MODULE$.default(new File("target")/*FIXME*//*targetDirectory: File*/, 1000/*FIXME*//*pollDelayMillis: Int*/, lp/*logger: LoggerProxy*/);
//        PlayWatchService playWatchService = PlayWatchService$.MODULE$.jdk7(loggerProxy);

//        Seq<File> docsClasspathSeq = JavaConversions.asScalaBuffer( docsClasspath );
        int defaultHttpPort = 9000;
        String defaultHttpAddress = "0.0.0.0";
        List<DevSetting> devSettings = Collections.<DevSetting>emptyList();
//        Seq<Tuple2<String, String>> devSettings = JavaConversions.asScalaBuffer( Collections.<Tuple2<String, String>>emptyList() );
        List<String> args = Collections.<String>emptyList();
//        Seq<String> args = JavaConversions.asScalaBuffer( Collections.<String>emptyList() );
//        Function1<String, Object> runSbtTask = new RunSbtTask(); /* String => AnyRef */
        String mainClassName = "play.core.server.DevServerStart";

        Reloader.startDevMode( /*runHooks, */javaOptions, dependencyClasspath/*Seq*/, dependencyClassLoader,
                                        reloadCompile, reloaderClassLoader, assetsClassLoader, commonClassLoader,
                                        monitoredFiles/*Seq*/, playWatchService, docsClasspath/*Seq*/, defaultHttpPort,
                                        defaultHttpAddress, baseDirectory,
                                        devSettings, args/*, runSbtTask*/, mainClassName );
        while ( true )
        {
            Thread.sleep( 1000L );
        }
    }

    /*private static ClassLoader getCommonClassLoader( List<File> dependencyClasspath ) throws java.net.MalformedURLException
    {
        List<URL> commonClasspath = new ArrayList<URL>(1);
        for ( File depFile: dependencyClasspath )
        {
            String name = depFile.getName();
            if ( name.startsWith( "h2-" ) || name == "h2.jar" )
            {
                commonClasspath.add( depFile.toURI().toURL() );
            }
        }
        return new URLClassLoader( commonClasspath.toArray( new URL[commonClasspath.size()] ), null /* important here, don't depend of the sbt classLoader! *//* );
    }*/

}
