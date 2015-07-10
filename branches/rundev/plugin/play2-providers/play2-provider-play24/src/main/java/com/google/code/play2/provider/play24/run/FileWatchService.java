package com.google.code.play2.provider.play24.run;

import java.io.File;
import java.util.List;

public interface FileWatchService
{
    /**
     * Watch the given sequence of files or directories.
     *
     * @param filesToWatch The files to watch.
     * @param onChange A callback that is executed whenever something changes.
     * @return A watcher
     */
    FileWatcher watch(List<File> filesToWatch, FileWatchCallback onChange);
}