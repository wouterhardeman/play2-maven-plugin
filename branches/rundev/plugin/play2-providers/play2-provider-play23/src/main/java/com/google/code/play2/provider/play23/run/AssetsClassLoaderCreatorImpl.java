package com.google.code.play2.provider.play23.run;

import java.util.List;

public class AssetsClassLoaderCreatorImpl
    implements AssetsClassLoaderCreator
{
    private List<Asset> assets;

    public AssetsClassLoaderCreatorImpl( List<Asset> assets )
    {
        this.assets = assets;
    }

    public final ClassLoader apply( ClassLoader parent )
    {
        return new AssetsClassLoader( parent, assets );
    }

}
