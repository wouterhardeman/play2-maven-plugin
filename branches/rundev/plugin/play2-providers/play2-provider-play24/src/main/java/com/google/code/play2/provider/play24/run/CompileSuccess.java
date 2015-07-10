package com.google.code.play2.provider.play24.run;

import java.io.File;
import java.util.List;

public class CompileSuccess
    implements CompileResult
{
    SourceMap sources;

    List<File> classpath;

    public CompileSuccess( SourceMap sources, List<File> classpath )
    {
        this.sources = sources;
        this.classpath = classpath;
    }

}
