package com.google.code.play2.provider.play23.run;

import java.util.Collections;
import java.util.Set;

public class WatchState
{
    private long lastCallbackCallTime;

    private Set<String> previousFiles;

    private boolean awaitingQuietPeriod;

    int count;

//    final class WatchState(val lastCallbackCallTime: Long, val previousFiles: Set[String], val awaitingQuietPeriod: Boolean, val count: Int) {
    public WatchState( long lastCallbackCallTime, Set<String> previousFiles, boolean awaitingQuietPeriod, int count )
    {
        this.lastCallbackCallTime = lastCallbackCallTime;
        this.previousFiles = previousFiles;
        this.awaitingQuietPeriod = awaitingQuietPeriod;
        this.count = count;
    }

    public boolean awaitingQuietPeriod()
    {
        return awaitingQuietPeriod;
    }

//    def previousFileCount: Int = previousFiles.size
    private int previousFileCount()
    {
        return previousFiles.size();
    }

//    @deprecated("Use another constructor", "0.13.6")
//    def this(lastCallbackCallTime: Long, previousFileCount: Int, awaitingQuietPeriod: Boolean, count: Int) {
//      this(lastCallbackCallTime, Set.empty[String], awaitingQuietPeriod, count)
//    }

//    object WatchState {
//      def empty = new WatchState(0L, Set.empty[String], false, 0)
//    }
    public static WatchState empty()
    {
        return new WatchState( 0L, Collections.<String> emptySet(), false, 0 );
    }

}
