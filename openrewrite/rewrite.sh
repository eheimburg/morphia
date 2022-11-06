#! /bin/sh

gitDir="target/morphia-2.2.x"
workspace="target/tests-2.2.x"
core="$gitDir/core"

clone() {
  if [ ! -d $gitDir ]
  then
    echo "$gitDir doesn't exist.  Cloning repository."
    git clone -b 2.2.x https://github.com/MorphiaOrg/morphia/ $gitDir
  fi
}

prepareWorkspace() {
  rm -rf $workspace

  mkdir -p $workspace
  cp src/test/resources/test-pom.xml $workspace/pom.xml
  cp rewrite.yml $workspace
  test="$workspace/src/test/java"
  mkdir -p $test
  cp -r $core/src/test/java/* $test
}

showDiff() {
  echo Diff:
  diff -r $core/src/test/java/ $workspace/src/test/java/
}

clone
prepareWorkspace
showDiff

cd $workspace
mvn org.openrewrite.maven:rewrite-maven-plugin:runNoFork \
                    -Drewrite.activeRecipes=dev.morphia.experimental
cd -

showDiff
#mvn -f $workspace/pom.xml test-compile
