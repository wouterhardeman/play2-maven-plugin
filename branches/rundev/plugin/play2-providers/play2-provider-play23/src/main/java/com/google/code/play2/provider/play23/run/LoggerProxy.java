package com.google.code.play2.provider.play23.run;

public interface LoggerProxy
{
    public void verbose( String msg );

    public void debug( String msg );

    public void info( String msg );

    public void warn( String msg );

    public void error( String msg );

    public void trace( Throwable exception );

    public void success( String msg );

}
