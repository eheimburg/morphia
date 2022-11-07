#! /bin/sh

showDiff() {
  echo Diff:
  git diff src
}

showDiff

mvn rewrite:run

showDiff
