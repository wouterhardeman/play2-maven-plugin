package com.google.code.play2.provider.play23.run;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.play2.provider.api.BuildLink;

/*
    val reloadCompile = () => PlayReload.compile(
      () => Project.runTask(playReload in scope, state).map(_._2).get,
      () => Project.runTask(reloaderClasspath in scope, state).map(_._2).get,
      () => Project.runTask(streamsManager in scope, state).map(_._2).get.toEither.right.toOption
    )
 */
public class ReloadCompileImpl implements ReloadCompile {

    private static class SourceMapImpl extends HashMap<String, Source> implements SourceMap
    {
    }

    private BuildLink buildLink;

    /* TEST */
    public ReloadCompileImpl(BuildLink buildLink)
    {
        this.buildLink = buildLink;
    }

    public CompileResult apply()
    {
        SourceMap sources = new SourceMapImpl();
        Map<String, File> x = buildLink.x();
        for (Map.Entry<String, File> entry: x.entrySet())
        {
            String name = entry.getKey();
            File sourceFile = entry.getValue();
            sources.put(name, new Source( sourceFile, null/*scala.Option.<File>empty()*/));
        }

        List<File> classpath = new ArrayList<File>(1);
        classpath.add(new File("target/classes"));

        CompileResult result = new CompileSuccess(sources, classpath);
        return result;
    }
    
//    private Reloader.CompileResult compile(Function0<Result<sbt.inc.Analysis>> reloadCompile,
//                                           Function0<Result<Classpath>> classpath
//                                           /*, Function0<Option<Streams>> streams*/)
//    {
//        
//    }
}