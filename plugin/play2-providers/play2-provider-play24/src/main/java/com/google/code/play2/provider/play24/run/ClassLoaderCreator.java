package com.google.code.play2.provider.play24.run;

import java.net.URL;
//import java.net.URLClassLoader;

public interface ClassLoaderCreator /*extends AbstractFunction3Impl<String, URL[], ClassLoader, ClassLoader>*/ {

    /*URL*/ClassLoader apply(String name, URL[] urls, ClassLoader parent);

}