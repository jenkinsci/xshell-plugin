# Version history

## Version 0.10 (Dev)

## Version 0.9 (Nov 10, 2013)

-   Improved regular expression ([xshell-pluigin pull \#4 on
    github](https://github.com/jenkinsci/xshell-plugin/pull/4)) - Thanks
    to calarco (<https://github.com/clalarco>)

## Version 0.8 (Apr 11, 2012)

-   Fixed parsing of file separator when URLs are present in the command
    line ([xshell-plugin pull \#2 on
    github](https://github.com/jenkinsci/xshell-plugin/pull/2)) - [issue
    \#13243](http://issues.jenkins-ci.org/browse/JENKINS-13243) - Thanks
    to davehunt (<https://github.com/davehunt>)
-   Replaced dubug with logging on jenkins log - Thanks to davehunt
    (<https://github.com/davehunt>)

## Version 0.7 (Dec 29, 2011)

-   Added environment variable format conversion (e.g. $VAR to %VAR% for
    Windows launcher) - Thanks to tclift (<https://github.com/tclift>)

## Version 0.6 (Mar 1, 2011)

-   Updates for Jenkins

## Version 0.4 (Sep 22, 2010)

-   Modified regex for path separator replacement that was causing an
    exception
    [JENKINS-7538](https://issues.jenkins-ci.org/browse/JENKINS-7538)
-   Added build variables to environment variables (as in
    CommandInterpreter).

## Version 0.3 (May 18, 2010)

-   Replace any '\\' or '/' in the command line with correct file
    separator (selected using OS where the task is executed).

## Version 0.2 (Mar 26, 2010)

-   Run executable from workspace directory also in unix.

## Version 0.1 (Mar 25, 2010)

-   Initial release
-   Runs a single command line
