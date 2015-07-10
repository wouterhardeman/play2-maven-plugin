package com.google.code.play2.provider.play24.run;

public class SourceModificationWatch
{
//    @tailrec def watch(sourcesFinder: PathFinder, pollDelayMillis: Int, state: WatchState)(terminationCondition: => Boolean): (Boolean, WatchState) =
    public static WatchState watch(int pollDelayMillis, WatchState state)
    {
        return new WatchState(System.currentTimeMillis(), java.util.Collections.<String>emptySet(), true, 0);//FIXME
    }
}