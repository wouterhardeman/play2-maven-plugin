package com.google.code.play2.provider.play23.run;

import java.io.File;

public class Asset
{
    String prefix;

    File dir;

    public Asset(String prefix, File dir)
    {
        this.prefix = prefix;
        this.dir = dir;
    }
}