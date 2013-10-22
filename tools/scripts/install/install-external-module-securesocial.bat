set GROUP_ID_PREFIX=com.google.code.play2-maven-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%securesocial

set ARTIFACT_ID=securesocial_2.10
set SRC_DIR=../sources/securesocial/%VERSION%
call mvn install:install-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/securesocial/%VERSION%/%ARTIFACT_ID%.pom -Dsources=%SRC_DIR%/%ARTIFACT_ID%-sources.jar -Djavadoc=%SRC_DIR%/%ARTIFACT_ID%-javadoc.jar
