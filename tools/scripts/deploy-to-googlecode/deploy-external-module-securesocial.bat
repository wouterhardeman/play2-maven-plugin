set GROUP_ID_PREFIX=com.google.code.play2-maven-plugin.
set GROUP_ID=%GROUP_ID_PREFIX%securesocial

set REPO_ID=com.google.code.play2-maven-plugin
set REPO_URL=https://play2-maven-plugin.googlecode.com/svn/mavenrepo/releases


set ARTIFACT_ID=securesocial_2.10
set SRC_DIR=../sources/securesocial/%VERSION%
call mvn deploy:deploy-file -Dfile=%SRC_DIR%/%ARTIFACT_ID%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dpackaging=jar -Dversion=%VERSION% -DpomFile=../poms/modules/securesocial/%VERSION%/%ARTIFACT_ID%.pom -Dsources=%SRC_DIR%/%ARTIFACT_ID%-sources.jar -Djavadoc=%SRC_DIR%/%ARTIFACT_ID%-javadoc.jar -DrepositoryId=%REPO_ID% -Durl=dav:%REPO_URL% -e
