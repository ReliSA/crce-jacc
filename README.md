# CRCE-JaCC

Project with CRCE plugins depending on JaCC.

## Prerequisities

- [CRCE](https://github.com/ReliSA/crce) installed
- ReliSA Maven repository in `settings.xml` (ask Premek Brada for details)

## Build

1. `crce-jacc-dependencies` module
2. `crce-metadata-java-parser` module

## Start up

First, copy the `/conf.default` directory and rename the copy to `/conf`. Otherwise, application dependencies will not load correctly.

Run CRCE-JaCC using Maven plugin for pax in `crce-modules-reactor` module:

`mvn pax:provision`
