package com.google.code.play2.provider.play24.run;

import java.net.URL;
import java.net.URLClassLoader;

public class NamedURLClassLoaderCreator implements/*extends*/ ClassLoaderCreator
{
    public final URLClassLoader apply(String name, URL[] urls, ClassLoader parent)
    {
        return new NamedURLClassLoader(name, urls, parent);
    }
}