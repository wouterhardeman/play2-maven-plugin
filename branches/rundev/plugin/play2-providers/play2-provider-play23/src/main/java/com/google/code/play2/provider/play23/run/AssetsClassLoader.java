package com.google.code.play2.provider.play23.run;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * A ClassLoader for serving assets.
 *
 * Serves assets from the given directories, at the given prefix.
 *
 * @param assets A list of assets directories, paired with the prefix they should be served from.
 */
public class AssetsClassLoader
    extends ClassLoader
{
    private List<Asset> assets;

    public AssetsClassLoader( ClassLoader parent, List<Asset> assets )
    {
        super( parent );
        this.assets = assets;
    }

/*
        override def findResource(name: String) = {
        assets.collectFirst {
          case (prefix, dir) if exists(name, prefix, dir) =>
            (dir / name.substring(prefix.length)).toURI.toURL
        }.orNull
      }
*/
    @Override /* ClassLoader */
    public URL findResource( String name )
    {
        URL result = null;
        for ( Asset asset : assets )
        {
            if ( exists( name, asset.prefix, asset.dir ) )
            {
                try
                {
                    result = new File( asset.dir, name.substring( asset.prefix.length() ) ).toURI().toURL();
                }
                catch ( MalformedURLException e )
                {
                    // ignore, result = null;
                }
                break;
            }
        }
        return result;
    }

/*
      def exists(name: String, prefix: String, dir: File) = {
        name.startsWith(prefix) && (dir / name.substring(prefix.length)).isFile
      }
*/
    private boolean exists( String name, String prefix, File dir )
    {
        return name.startsWith( prefix ) && new File( dir, name.substring( prefix.length() ) ).isFile();
    }

}
