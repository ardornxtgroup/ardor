----
# OPERATORS GUIDE #

----
## How to verify the Ardor Software package? ##
  Releases are signed by Jelurida using [GPG](https://en.wikipedia.org/wiki/GNU_Privacy_Guard). It is **highly** recommended to verify the signature every time you download new version. [There are some notes](https://bitcointalk.org/index.php?topic=345619.msg4406124#msg4406124) how to do this. [This script](https://github.com/nxt-ext/nxt-kit/blob/master/distrib/safe-nxt-download.sh) automates this process on Linux.

----
## How to configure Ardor? ##

  - config files under `conf/`
  - options are described in config files
  - **do not edit** `conf/nxt-default.properties` **nor** `conf/logging-default.properties`
  - use own config file instead: `conf/nxt.properties` or `conf/logging.properties`
  - only deviations from default config

----
## How to update Ardor? ##

  - **if configured as described above**, just unpack a new version over the existing installation directory
  - next run of Ardor will upgrade database if necessary
  
----

## How to manage multiple Ardor nodes? ##
  Check [Nxt-Kit's homepage](https://github.com/nxt-ext/nxt-kit) for more information.

----