package com.google.code.play2.provider.play23.run;

import java.io.IOException;
import java.util.jar.JarFile;

import play.core.BuildLink;
import play.core.server.ServerWithStop;

class ReloaderPlayDevServer
    implements PlayDevServer
{
    private ServerWithStop server;

    private JarFile docsJarFile;

    private Reloader reloader;

    public ReloaderPlayDevServer( ServerWithStop server, JarFile docsJarFile, Reloader reloader )
    {
        this.server = server;
        this.docsJarFile = docsJarFile;
        this.reloader = reloader;
    }

    @Override /* Closeable interface */
    public void close()
        throws IOException
    {
//      server.stop()
//      docsJarFile.close()
//      reloader.close()
        server.stop();
        docsJarFile.close();
        reloader.close();

//      // Notify hooks
//      runHooks.run(_.afterStopped())
//
//      // Remove Java properties
//      properties.foreach {
//        case (key, _) => System.clearProperty(key)
//      }
        // FIXME
    }

    @Override /* PlayDevServer interface */
    public BuildLink buildLink()
    {
        return reloader;
    }

}
