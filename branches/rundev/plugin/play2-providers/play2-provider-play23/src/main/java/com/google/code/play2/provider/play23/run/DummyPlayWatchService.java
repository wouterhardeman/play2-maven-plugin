package com.google.code.play2.provider.play23.run;

import java.io.File;
import java.util.List;

public class DummyPlayWatchService
    implements PlayWatchService
{
    @Override
    public PlayWatcher watch( List<File> filesToWatch, PlayWatchCallback onChange )
    {
        return new DummyPlayWatcher();
    }

}
