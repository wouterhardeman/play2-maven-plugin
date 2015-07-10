package com.google.code.play2.provider.play24.run;

public interface AssetsClassLoaderCreator
{
    ClassLoader apply(ClassLoader parent);
}