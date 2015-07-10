package com.google.code.play2.provider.play23.run;

import java.net.URL;
import java.net.URLClassLoader;

public class NamedURLClassLoaderCreator
    implements ClassLoaderCreator
{
    public final URLClassLoader apply( String name, URL[] urls, ClassLoader parent )
    {
        return new NamedURLClassLoader( name, urls, parent );
    }

}
