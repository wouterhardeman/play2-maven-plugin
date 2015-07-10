package com.google.code.play2.provider.play24.run;

import java.net.URL;
import java.net.URLClassLoader;

class NamedURLClassLoader extends URLClassLoader
{
    private final String name;
    private final URL[] urls; // for toString() only
    
    public NamedURLClassLoader(String name, URL urls[], ClassLoader parent)
    {
        super(urls, parent);
        this.name = name;
        this.urls = urls; // for toString() only
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( name ).append( "{" ).append( urls[0].toString() );
        for ( int i = 1; i < urls.length; i++ )
        {
            sb.append( ", " ).append( urls[i].toString() );
        }
        sb.append( "}" );
        return sb.toString();
        // return name + "{" + getURLs.map(_.toString).mkString(", ") + "}"
    }
}