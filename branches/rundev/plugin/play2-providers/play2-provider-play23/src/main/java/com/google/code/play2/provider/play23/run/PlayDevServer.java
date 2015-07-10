package com.google.code.play2.provider.play23.run;

import java.io.Closeable;

import play.core.BuildLink;

//    /**
//     * Play dev server
//     */
//    trait PlayDevServer extends Closeable {
//      val buildLink: BuildLink
//    }
interface PlayDevServer
    extends Closeable
{
    BuildLink buildLink();
}