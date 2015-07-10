package com.google.code.play2.provider.play24.run;

public class LoggerProxyImpl
    implements LoggerProxy
{
    @Override
    public void verbose( String msg )
    {
        System.out.println( "verbose: " + msg );
    }

    @Override
    public void debug( String msg )
    {
        System.out.println( "debug: " + msg );
    }

    @Override
    public void info( String msg )
    {
        System.out.println( "info: " + msg );
    }

    @Override
    public void warn( String msg )
    {
        System.out.println( "warn: " + msg );
    }

    @Override
    public void error( String msg )
    {
        System.out.println( "error: " + msg );
    }

    @Override
    public void trace( Throwable exception )
    {
        Throwable t = exception;
        System.out.println( "trace: " + t.getMessage() + '\n' );
        t.printStackTrace();
    }

    @Override
    public void success( String msg )
    {
        System.out.println( "success: " + msg );
    }

}
