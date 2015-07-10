package com.google.code.play2.provider.play24.run;

import java.io.File;
import java.util.List;

public class DummyPlayWatchService
    implements FileWatchService
{
    @Override
    public FileWatcher watch( List<File> filesToWatch, FileWatchCallback onChange )
    {
        return new DummyPlayWatcher();
    }

}
