package com.google.code.play2.provider.play23.run;

public interface AssetsClassLoaderCreator
{
    ClassLoader apply(ClassLoader parent);
}