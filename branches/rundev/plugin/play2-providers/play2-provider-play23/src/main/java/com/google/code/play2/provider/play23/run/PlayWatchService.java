package com.google.code.play2.provider.play23.run;

import java.io.File;
import java.util.List;

public interface PlayWatchService
{
    /**
     * Watch the given sequence of files or directories.
     *
     * @param filesToWatch The files to watch.
     * @param onChange A callback that is executed whenever something changes.
     * @return A watcher
     */
    PlayWatcher watch(List<File> filesToWatch, PlayWatchCallback onChange);
}