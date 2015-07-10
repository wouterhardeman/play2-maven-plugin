package com.google.code.play2.provider.play23.run;

import java.net.URL;

public interface ClassLoaderCreator
{

    ClassLoader apply( String name, URL[] urls, ClassLoader parent );

}
