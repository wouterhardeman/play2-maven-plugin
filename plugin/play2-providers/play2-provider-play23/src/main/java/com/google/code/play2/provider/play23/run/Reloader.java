package com.google.code.play2.provider.play23.run;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

import play.core.BuildDocHandler;
import play.core.BuildLink;
import play.core.classloader.ApplicationClassLoaderProvider;
import play.core.classloader.DelegatingClassLoader;
import play.core.server.ServerWithStop;

public class Reloader implements BuildLink
{
    // Regex to match Java System Properties of the format -Dfoo=bar
    //TMP static val SystemProperty = "-D([^=]+)=(.*)".r

    //TODO-move out of here
    private static class Property
    {
        String key, value;

        public Property( String key, String value )
        {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Take all the options in javaOptions of the format "-Dfoo=bar" and return them as a Seq of key value pairs of the format ("foo" -> "bar")
     */
    /*TMP static def extractSystemProperties(javaOptions: Seq[String]): Seq[(String, String)] = {
      javaOptions.collect { case SystemProperty(key, value) => key -> value }
    }*/
    private static List<Property> extractSystemProperties( List<String> javaOptions )
    {
        List<Property> result = new ArrayList<Property>();
        for ( String javaOption : javaOptions )
        {
            // TODO - use regex
            if ( javaOption.startsWith( "-D" ) )
            {
                String[] namevalue = javaOption.substring( 2 ).split( "=", 2 );
                result.add( new Property( namevalue[0], namevalue[1] ) );
            }
        }
        return result;
    }

    private static int parsePort( String portString )
    {
        try
        {
            return Integer.parseInt( portString );
        }
        catch ( NumberFormatException e )
        {
            throw new RuntimeException( "Invalid port argument: " + portString );// ??
            // System.err.println("Invalid port argument: " + portString);//FIXME
        }
    }

/* static
    def filterArgs(args: Seq[String], defaultHttpPort: Int): (Seq[(String, String)], Option[Int], Option[Int]) = {
        val (properties, others) = args.span(_.startsWith("-D"))

        val javaProperties = properties.map(_.drop(2).split('=')).map(a => a(0) -> a(1)).toSeq

        // collect arguments plus config file property if present
        val httpPort = Option(System.getProperty("http.port"))
        val httpsPort = Option(System.getProperty("https.port"))

        //port can be defined as a numeric argument or as disabled, -Dhttp.port argument or a generic sys property
        val maybePort = others.headOption.orElse(javaProperties.toMap.get("http.port")).orElse(httpPort)
        val maybeHttpsPort = javaProperties.toMap.get("https.port").orElse(httpsPort).map(parsePort)
        if (maybePort.exists(_ == "disabled")) (javaProperties, Option.empty[Int], maybeHttpsPort)
        else (javaProperties, maybePort.map(parsePort).orElse(Some(defaultHttpPort)), maybeHttpsPort)
      }
*/
    private static URL[] urls( List<File> cp ) // throws MalformedURLException
    { //= cp.map(_.toURI.toURL).toArray
        try
        {
            URL[] result = new URL[cp.size()];
            for ( int i = 0; i < cp.size(); i++ )
            {
                File file = cp.get( i );
                result[i] = file.toURI().toURL();
            }
            return result;
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e );// ?????
        }
    }

    private static ClassLoaderCreator createURLClassLoader = new NamedURLClassLoaderCreator();
    /*private static ClassLoaderCreator createURLClassLoader = new ClassLoaderCreator() {

        @Override
        public URLClassLoader apply( final String name, URL[] urls, ClassLoader parent )
        {
            return new NamedURLClassLoader(name, urls, parent);
        }

    };*/

/*
    val createDelegatedResourcesClassLoader: ClassLoaderCreator = (name, urls, parent) => new java.net.URLClassLoader(urls, parent) {
        require(parent ne null)
        override def getResources(name: String): java.util.Enumeration[java.net.URL] = getParent.getResources(name)
        override def toString = name + "{" + getURLs.map(_.toString).mkString(", ") + "}"
      }
*/
    private static ClassLoaderCreator createDelegatedResourcesClassLoader = new ClassLoaderCreator()
    {

        @Override
        public URLClassLoader apply( final String name, URL[] urls, ClassLoader parent )
        {
            return new NamedURLClassLoader( name, urls, parent )
            {
                @Override
                public Enumeration<URL> getResources( String name )
                    throws IOException
                {
                    return getParent().getResources( name );
                }

            };
        }

    };

//    def assetsClassLoader(allAssets: Seq[(String, File)])(parent: ClassLoader): ClassLoader = new AssetsClassLoader(parent, allAssets)
    private static ClassLoader assetsClassLoader( ClassLoader parent, List<Asset> assets )
    {
        return new AssetsClassLoader( parent, assets );
    }

//    def commonClassLoader(classpath: Classpath) = {
//        lazy val commonJars: PartialFunction[java.io.File, java.net.URL] = {
//          case jar if jar.getName.startsWith("h2-") || jar.getName == "h2.jar" => jar.toURI.toURL
//        }
//
//        new java.net.URLClassLoader(classpath.collect(commonJars).toArray, null /* important here, don't depend of the sbt classLoader! */ ) {
//          override def toString = "Common ClassLoader: " + getURLs.map(_.toString).mkString(",")
//        }
//      }
    public static ClassLoader commonClassLoader( List<File> classpath )
        throws MalformedURLException
    {
        List<URL> commonClasspath = new ArrayList<URL>( 1 );
        for ( File depFile : classpath )
        {
            String name = depFile.getName();
            if ( name.startsWith( "h2-" ) || name == "h2.jar" )
            {
                commonClasspath.add( depFile.toURI().toURL() );
            }
        }
        return new URLClassLoader( commonClasspath.toArray( new URL[commonClasspath.size()] ), null /* important here, don't depend of the sbt classLoader! */ );
    }

    //Moja proba rozwiazania cyklu
    private static class XApplicationClassLoaderProvider
        implements ApplicationClassLoaderProvider
    {
        private Reloader reloader;

        public void setReloader( Reloader reloader )
        {
            this.reloader = reloader;
        }

        @Override
        public ClassLoader get()
        {
            return reloader != null ? reloader.getClassLoader() : null;
        }
    }

//    /**
//     * Start the Play server in dev mode
//     *
//     * @return A closeable that can be closed to stop the server
//     */
//    def startDevMode(runHooks: Seq[RunHook], javaOptions: Seq[String],
//      dependencyClasspath: Classpath, dependencyClassLoader: ClassLoaderCreator,
//      reloadCompile: () => CompileResult, reloaderClassLoader: ClassLoaderCreator,
//      assetsClassLoader: ClassLoader => ClassLoader, commonClassLoader: ClassLoader,
//      monitoredFiles: Seq[String], playWatchService: PlayWatchService,
//      docsClasspath: Classpath, defaultHttpPort: Int, projectPath: File,
//      devSettings: Seq[(String, String)], args: Seq[String],
//      runSbtTask: String => AnyRef): PlayDevServer = {
    public static PlayDevServer startDevMode( List<String> javaOptions, List<File> dependencyClasspath,
                                              ClassLoaderCreator dependencyClassLoader, ReloadCompile reloadCompile,
                                              ClassLoaderCreator reloaderClassLoader,
                                              AssetsClassLoaderCreator assetsClassLoader,
                                              ClassLoader commonClassLoader, List/* <String> */<File> monitoredFiles,
                                              PlayWatchService playWatchService, List<File> docsClasspath,
                                              int defaultHttpPort, File projectPath, List<DevSetting> devSettings,
                                              List<String> args )
        throws Throwable
    {
        List<Property> properties;
        Integer httpPort = Integer.valueOf( 9000 );// FIXME
        Integer httpsPort = null;// FIXME
//        val (properties, httpPort, httpsPort) = filterArgs(args, defaultHttpPort = defaultHttpPort)
//        val systemProperties = extractSystemProperties(javaOptions)
        List<Property> systemProperties = extractSystemProperties( javaOptions );

//        require(httpPort.isDefined || httpsPort.isDefined, "You have to specify https.port when http.port is disabled")

//        // Set Java properties
//        (properties ++ systemProperties).foreach {
//          case (key, value) => System.setProperty(key, value)
//        }
        //TODO to samo dla properties
        for ( Property property : systemProperties )
        {
            System.setProperty( property.key, property.value );
        }

        System.out.println();

        /*
         * We need to do a bit of classloader magic to run the Play application.
         *
         * There are seven classloaders:
         *
         * 1. buildLoader, the classloader of sbt and the Play sbt plugin.
         * 2. commonLoader, a classloader that persists across calls to run.
         *    This classloader is stored inside the
         *    PlayInternalKeys.playCommonClassloader task. This classloader will
         *    load the classes for the H2 database if it finds them in the user's
         *    classpath. This allows H2's in-memory database state to survive across
         *    calls to run.
         * 3. delegatingLoader, a special classloader that overrides class loading
         *    to delegate shared classes for build link to the buildLoader, and accesses
         *    the reloader.currentApplicationClassLoader for resource loading to
         *    make user resources available to dependency classes.
         *    Has the commonLoader as its parent.
         * 4. applicationLoader, contains the application dependencies. Has the
         *    delegatingLoader as its parent. Classes from the commonLoader and
         *    the delegatingLoader are checked for loading first.
         * 5. docsLoader, the classloader for the special play-docs application
         *    that is used to serve documentation when running in development mode.
         *    Has the applicationLoader as its parent for Play dependencies and
         *    delegation to the shared sbt doc link classes.
         * 6. playAssetsClassLoader, serves assets from all projects, prefixed as
         *    configured.  It does no caching, and doesn't need to be reloaded each
         *    time the assets are rebuilt.
         * 7. reloader.currentApplicationClassLoader, contains the user classes
         *    and resources. Has applicationLoader as its parent, where the
         *    application dependencies are found, and which will delegate through
         *    to the buildLoader via the delegatingLoader for the shared link.
         *    Resources are actually loaded by the delegatingLoader, where they
         *    are available to both the reloader and the applicationLoader.
         *    This classloader is recreated on reload.
         *
         * Someone working on this code in the future might want to tidy things up
         * by splitting some of the custom logic out of the URLClassLoaders and into
         * their own simpler ClassLoader implementations. The curious cycle between
         * applicationLoader and reloader.currentApplicationClassLoader could also
         * use some attention.
         */

        ClassLoader buildLoader = Reloader.class.getClassLoader();

        /**
         * ClassLoader that delegates loading of shared build link classes to the
         * buildLoader. Also accesses the reloader resources to make these available
         * to the applicationLoader, creating a full circle for resource loading.
         */
//        lazy val delegatingLoader: ClassLoader = new DelegatingClassLoader(commonClassLoader, buildLoader, new ApplicationClassLoaderProvider {
//          def get: ClassLoader = { reloader.getClassLoader.orNull }
//        })
        XApplicationClassLoaderProvider x = new XApplicationClassLoaderProvider();
        ClassLoader delegatingLoader = new DelegatingClassLoader( commonClassLoader, buildLoader, x );

//        lazy val applicationLoader = dependencyClassLoader("PlayDependencyClassLoader", urls(dependencyClasspath), delegatingLoader)
        ClassLoader applicationLoader =
            dependencyClassLoader.apply( "PlayDependencyClassLoader", urls( dependencyClasspath ), delegatingLoader );
//        lazy val assetsLoader = assetsClassLoader(applicationLoader)
        ClassLoader assetsLoader = assetsClassLoader.apply( applicationLoader );

//        lazy val reloader = new Reloader(reloadCompile, reloaderClassLoader, assetsLoader, projectPath, devSettings, monitoredFiles, playWatchService, runSbtTask)
        Reloader reloader =
            new Reloader( reloadCompile, reloaderClassLoader, assetsLoader, projectPath, devSettings, monitoredFiles,
                          playWatchService/* , runSbtTask */);
        x.setReloader( reloader );

        try
        {
            // Now we're about to start, let's call the hooks:
//            runHooks.run(_.beforeStarted())

            // Get a handler for the documentation. The documentation content lives in play/docs/content
            // within the play-docs JAR.
//            val docsLoader = new URLClassLoader(urls(docsClasspath), applicationLoader)
            ClassLoader docsLoader = new URLClassLoader( urls( docsClasspath ), applicationLoader );
//            val docsJarFile = {
//                val f = docsClasspath.filter(_.getName.startsWith("play-docs")).head
//                new JarFile(f)
//            }
            JarFile docsJarFile = null;
            for ( File f : docsClasspath )
            {
                if ( f.getName().startsWith( "play-docs" ) )
                {
                    docsJarFile = new JarFile( f );
                    break;
                }
            }
//            val buildDocHandler = {
//                val docHandlerFactoryClass = docsLoader.loadClass("play.docs.BuildDocHandlerFactory")
//                val factoryMethod = docHandlerFactoryClass.getMethod("fromJar", classOf[JarFile], classOf[String])
//                factoryMethod.invoke(null, docsJarFile, "play/docs/content").asInstanceOf[BuildDocHandler]
//            }
            Class<?> docHandlerFactoryClass = docsLoader.loadClass( "play.docs.BuildDocHandlerFactory" );
            Method factoryMethod = docHandlerFactoryClass.getMethod( "fromJar", JarFile.class, String.class );
            BuildDocHandler buildDocHandler =
                (BuildDocHandler) factoryMethod.invoke( null, docsJarFile, "play/docs/content" );

//            val server = {
//                val mainClass = applicationLoader.loadClass("play.core.server.NettyServer")
//                if (httpPort.isDefined) {
//                  val mainDev = mainClass.getMethod("mainDevHttpMode", classOf[BuildLink], classOf[BuildDocHandler], classOf[Int])
//                  mainDev.invoke(null, reloader, buildDocHandler, httpPort.get: java.lang.Integer).asInstanceOf[play.core.server.ServerWithStop]
//                } else {
//                  val mainDev = mainClass.getMethod("mainDevOnlyHttpsMode", classOf[BuildLink], classOf[BuildDocHandler], classOf[Int])
//                  mainDev.invoke(null, reloader, buildDocHandler, httpsPort.get: java.lang.Integer).asInstanceOf[play.core.server.ServerWithStop]
//                }
//            }
            Class<?> mainClass = applicationLoader.loadClass( "play.core.server.NettyServer" );
            String mainMethod = httpPort != null ? "mainDevHttpMode" : "mainDevOnlyHttpsMode";
            int port = httpPort != null ? httpPort.intValue() : httpsPort.intValue();
            Method mainDev = mainClass.getMethod( mainMethod, BuildLink.class, BuildDocHandler.class, Integer.TYPE );
            ServerWithStop server = (ServerWithStop) mainDev.invoke( null, reloader, buildDocHandler, port );

            // Notify hooks
//            runHooks.run(_.afterStarted(server.mainAddress))

//            new PlayDevServer {
//              val buildLink = reloader
//
//              def close() = {
//                server.stop()
//                docsJarFile.close()
//                reloader.close()
//
//                // Notify hooks
//                runHooks.run(_.afterStopped())
//
//                // Remove Java properties
//                properties.foreach {
//                  case (key, _) => System.clearProperty(key)
//                }
//              }
//            }
            return new ReloaderPlayDevServer( server, docsJarFile, reloader );
        }
        catch ( Throwable t )
        {
//            // Let hooks clean up
//            runHooks.foreach { hook =>
//              try {
//                hook.onError()
//              } catch {
//                case e: Throwable => // Swallow any exceptions so that all `onError`s get called.
//              }
//            }
//            throw e
            throw t;
        }
    }

    private ReloadCompile reloadCompile;

    private ClassLoaderCreator createClassLoader;

    private ClassLoader baseLoader;

    private final File projectPath;

    private List<DevSetting> devSettings;

    private List/* <String> */<File> monitoredFiles;

    private PlayWatchService playWatchService;

    public Reloader( ReloadCompile reloadCompile, ClassLoaderCreator createClassLoader, ClassLoader baseLoader,
                     final File projectPath, List<DevSetting> devSettings, List/* <String> */<File> monitoredFiles,
                     PlayWatchService playWatchService/* , runSbtTask: String => AnyRef */)
    {
        this.reloadCompile = reloadCompile;
        this.createClassLoader = createClassLoader;
        this.baseLoader = baseLoader;
        this.projectPath = projectPath;
        this.devSettings = devSettings;
        this.monitoredFiles = monitoredFiles;
        this.playWatchService = playWatchService;

        watchState = WatchState.empty();
        watcher = playWatchService.watch( monitoredFiles, new PlayWatchCallback()
        {
            public void onChange()
            {
                changed = true;
            }
        } );
        classLoaderVersion = new AtomicInteger( 0 );
    }
//    // The current classloader for the application
//    @volatile private var currentApplicationClassLoader: Option[ClassLoader] = None
    private volatile ClassLoader currentApplicationClassLoader = null;
    // Flag to force a reload on the next request.
    // This is set if a compile error occurs, and also by the forceReload method on BuildLink, which is called for
    // example when evolutions have been applied.
//    @volatile private var forceReloadNextTime = false
    private volatile boolean forceReloadNextTime = false;
    // Whether any source files have changed since the last request.
//    @volatile private var changed = false
    private volatile boolean changed = false;
    // The last successful compile results. Used for rendering nice errors.
//    @volatile private var currentSourceMap = Option.empty[SourceMap]
    private volatile SourceMap currentSourceMap = null;
    // A watch state for the classpath. Used to determine whether anything on the classpath has changed as a result
    // of compilation, and therefore a new classloader is needed and the app needs to be reloaded.
//    @volatile private var watchState: WatchState = WatchState.empty
    private volatile WatchState watchState;// = null;

    // Create the watcher, updates the changed boolean when a file has changed.
//    private val watcher = playWatchService.watch(monitoredFiles.map(new File(_)), () => {
//      changed = true
//    })
    private PlayWatcher watcher;//too early = playWatchService.watch( monitoredFiles, new PlayWatchCallback() { public void onChange(){changed = true;}} );
//    private val classLoaderVersion = new java.util.concurrent.atomic.AtomicInteger(0)
    private AtomicInteger classLoaderVersion;//too early = new AtomicInteger(0);

    /**
     * Contrary to its name, this doesn't necessarily reload the app.  It is invoked on every request, and will only
     * trigger a reload of the app if something has changed.
     *
     * Since this communicates across classloaders, it must return only simple objects.
     *
     *
     * @return Either
     * - Throwable - If something went wrong (eg, a compile error).
     * - ClassLoader - If the classloader has changed, and the application should be reloaded.
     * - null - If nothing changed.
     */
    @Override /* BuildLink interface */
    synchronized public Object reload()
    {
        Object result = null;
//        if (changed || forceReloadNextTime || currentSourceMap.isEmpty || currentApplicationClassLoader.isEmpty) {
        if ( changed || forceReloadNextTime || currentSourceMap == null || currentApplicationClassLoader == null )
        {
//            val shouldReload = forceReloadNextTime
            boolean shouldReload = forceReloadNextTime;

//            changed = false
//            forceReloadNextTime = false
            changed = false;
            forceReloadNextTime = false;

//             Run the reload task, which will trigger everything to compile
//            reloadCompile() match {
            CompileResult compileResult = reloadCompile.apply();
//            case CompileFailure(exception) =>
            if ( compileResult instanceof CompileFailure )
            {
                CompileFailure compileFailure = (CompileFailure) compileResult;

//              // We force reload next time because compilation failed this time
//              forceReloadNextTime = true
//              exception
                forceReloadNextTime = true;
                result = compileFailure.exception;
            }
//            case CompileSuccess(sourceMap, classpath) =>
            else if ( compileResult instanceof CompileSuccess )
            {
                CompileSuccess compileSuccess = (CompileSuccess) compileResult;

//                currentSourceMap = Some(sourceMap)
                currentSourceMap = compileSuccess.sources;

                // We only want to reload if the classpath has changed.  Assets don't live on the classpath, so
                // they won't trigger a reload.
                // Use the SBT watch service, passing true as the termination to force it to break after one check
//                val (_, newState) = SourceModificationWatch.watch(PathFinder.strict(classpath).***, 0, watchState)(true)
//                SourceModificationWatch.WatchResult watchresult = SourceModificationWatch.watch(/*??,*/ 0, watchState);
                WatchState newState = SourceModificationWatch.watch( /* ??, */0, watchState );
                // SBT has a quiet wait period, if that's set to true, sources were modified
//                val triggered = newState.awaitingQuietPeriod
                boolean triggered = newState.awaitingQuietPeriod();
//                watchState = newState
                watchState = newState;

//                if (triggered || shouldReload || currentApplicationClassLoader.isEmpty) {
                if ( triggered || shouldReload || currentApplicationClassLoader == null )
                {
                    // Create a new classloader
//                    val version = classLoaderVersion.incrementAndGet
                    int version = classLoaderVersion.incrementAndGet();
//                    val name = "ReloadableClassLoader(v" + version + ")"
                    String name = "ReloadableClassLoader(v" + version + ")";
//                    val urls = Reloader.urls(classpath)
                    URL[] urls = Reloader.urls( compileSuccess.classpath );
//                    val loader = createClassLoader(name, urls, baseLoader)
//                    currentApplicationClassLoader = Some(loader)
                    currentApplicationClassLoader = createClassLoader.apply( name, urls, baseLoader );
//                    loader
                  result = currentApplicationClassLoader;
//                } else {
//                    null // null means nothing changed
                }
            }
        }
        return result;
    }

//    lazy val settings = {
//        import scala.collection.JavaConverters._
//        devSettings.toMap.asJava
//    }
    @Override /* BuildLink interface */
    public Map<String, String> settings()
    {
        Map<String, String> result = new HashMap<String, String>( devSettings.size() );
        for ( DevSetting devSetting : devSettings )
        {
            result.put( devSetting.key, devSetting.value );
        }
        return result;
    }

//    def forceReload() {
//        forceReloadNextTime = true
//    }
    @Override /* BuildLink interface */
    public void forceReload()
    {
        forceReloadNextTime = true;
    }

//    def findSource(className: String, line: java.lang.Integer): Array[java.lang.Object] = {
//        val topType = className.split('$').head
//        currentSourceMap.flatMap { sources =>
//          sources.get(topType).map { source =>
//            Array[java.lang.Object](source.original.getOrElse(source.file), line)
//          }
//        }.orNull
//    }
    @Override /* BuildLink interface */
    public Object[] findSource( String className, Integer line )
    {
        Object[] result = null;
        String topType = className.split( "$" )[0];
        if ( currentSourceMap != null )
        {
            Source source = currentSourceMap.get( topType );
            result = new Object[] { source.file/* FIXME */, line };
        }
        return result;
    }

//    def runTask(task: String): AnyRef = runSbtTask(task)
    @Override /* BuildLink interface */
    public Object runTask( String task )
    {
        return null; // NOT USED
    }

    @Override /* BuildLink interface */
    public File projectPath()
    {
        return projectPath;
    }

//    def close() = {
//        currentApplicationClassLoader = None
//        currentSourceMap = None
//        watcher.stop()
//    }
    void close()
    {
        currentApplicationClassLoader = null;
        currentSourceMap = null;
        watcher.stop();
    }

//    def getClassLoader = currentApplicationClassLoader
    public ClassLoader getClassLoader()
    {
        return currentApplicationClassLoader;
    }

}
