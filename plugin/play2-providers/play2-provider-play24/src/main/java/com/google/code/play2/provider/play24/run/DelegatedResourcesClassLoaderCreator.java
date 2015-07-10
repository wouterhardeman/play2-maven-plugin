package com.google.code.play2.provider.play24.run;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class DelegatedResourcesClassLoaderCreator implements/*extends*/ ClassLoaderCreator
{
    public final URLClassLoader apply(String name, URL urls[], ClassLoader parent)
    {
        return new NamedURLClassLoader(name, urls, parent) {
            @Override
            public java.util.Enumeration<URL> getResources(String name) throws IOException
            {
                return getParent().getResources(name);
            }
            
        };
    }
}