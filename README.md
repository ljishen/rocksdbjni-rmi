# RMI Server for RocksDB [![Build Status](https://travis-ci.com/ljishen/rocksdbjni-rmi.svg?branch=master)](https://travis-ci.com/ljishen/rocksdbjni-rmi) [![GitHub license](https://img.shields.io/github/license/ljishen/rocksdbjni-rmi)](https://github.com/ljishen/rocksdbjni-rmi/blob/master/LICENSE)

## Installation

### Release Version from the Central Repository

```bash
wget https://oss.sonatype.org/service/local/repositories/releases/content/com/github/ljishen/rocksdbjni-rmi/0.0.0.9/rocksdbjni-rmi-0.0.0.9.jar
java -jar rocksdbjni-rmi-0.0.0.9.jar [port] [hostname]
```

### Bleeding Edge / master

```bash
git clone https://github.com/ljishen/rocksdbjni-rmi.git
cd rocksdbjni-rmi
mvn clean package [-P{rocksdb,trocksdb}] [-Drocksdb.version=<the version>]

java -jar target/rocksdbjni-rmi-0.0.0.10-SNAPSHOT.jar [port] [hostname]
```

The `port` and `hostname` are used to run the RMI registry in the server JVM. The default port is `1099`, and the default hostname is `localhost` if the corresponding parameter is not specified. Note that the `hostname` is the hostname or IP of the local machine, and it should be reachable from the client program (e.g., YCSB) who wants to connect to the RMI registry.

You can choose to build the RMI server for either [RocksDB](https://github.com/facebook/rocksdb) or [TRocksDB](https://github.com/KioxiaAmerica/trocksdb) with the option `-P`. You can also specify the version of RocksDB using the build property `rocksdb.version`. The available versions are specified by the availabilities of [the corresponding options files](src/test/resources/).


## Working with YCSB

Please refer to the [YCSB rocksdb-binding](https://github.com/ljishen/YCSB/tree/remote-rocksdb/rocksdb) for detail instruction of using this server.


## Development

Here are the steps to deploy and release a new version of "rocksdbjni-rmi" to [Sonatype OSSRH (OSS Repository Hosting)](https://oss.sonatype.org/). The process mainly follows the [OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html) and tries to provide an aggregated overall steps.

1. Make sure to commit all your changes to the local repository.

2. If you do not have a GPG key pair, generate one following these [detailed instructions](https://central.sonatype.org/pages/working-with-pgp-signatures.html). Please remember to distribute your public key to a key server.

3. Copy the `settings.xml` to either `${maven.home}/conf/settings.xml` as global settings, or `${user.home}/.m2/settings.xml` as user settings. The user settings dominate the global settings if the same settings exist in the two locations. Usually, this could be as simple as

   ```bash
   cp settings.xml ~/.m2/
   ```

4. Update the [Sonatype JIRA](https://issues.sonatype.org/) username and password, as well as the passphrase of your GPG key in your `settings.xml`.

5. Prepare for a release in SCM:

   ```bash
   mvn release:clean release:prepare
   ```

   This process will ask you the release version, the SCM release tag, and the new development version for "rocksdbjni-rmi", and automatically commit the changes of the POM file and the README.md file for you in 3 commits with the message prefix of "[maven-release-plugin]".

6. With version updated, you can deploy the "rocksdbjni-rmi" to the staging repository

   ```bash
   mvn release:perform
   ```

   This execution will deploy to OSSRH but will NOT release to the Central Repository.

7. (Optional) Query the `stagingRepositoryId`s. You will need them for releasing or dropping if you have multiple staging repositories

   ```bash
   mvn nexus-staging:rc-list -P release
   ```

   Here is an example output from the above command:

   ```bash
   [INFO] Scanning for projects...
   [INFO] Inspecting build with total of 1 modules...
   [INFO] Installing Nexus Staging features:
   [INFO]   ... total of 1 executions of maven-deploy-plugin replaced with nexus-staging-maven-plugin
   [INFO]
   [INFO] -----------------< com.github.ljishen:rocksdbjni-rmi >------------------
   [INFO] Building rocksdbjni-rmi 0.0.0.11-SNAPSHOT
   [INFO] --------------------------------[ jar ]---------------------------------
   [INFO]
   [INFO] --- nexus-staging-maven-plugin:1.6.8:rc-list (default-cli) @ rocksdbjni-rmi ---
   [INFO]  + Using server credentials "ossrh" from Maven settings.
   [INFO]  * Connected to Nexus at https://oss.sonatype.org:443/, is version 2.14.16-01 and edition "Professional"
   [INFO] Getting list of available staging repositories...
   [INFO]
   [INFO] ID                   State    Description
   [INFO] comgithubljishen-1053 CLOSED   com.github.ljishen:rocksdbjni-rmi:0.0.0.10
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   [INFO] Total time:  3.835 s
   [INFO] Finished at: 2020-03-25T20:24:27-07:00
   [INFO] ------------------------------------------------------------------------
   ```

   In this case, the `stagingRepositoryId` is `comgithubljishen-1048`.

8. You can manually inspect the [staging repository](https://oss.sonatype.org/#stagingRepositories) through the Nexus Repository Manager.

   1. If everything goes well, you can trigger a release of the staging repository with

      ```bash
      cd target/checkout
      mvn nexus-staging:release -P release [-DstagingRepositoryId=<stagingRepositoryId>]
      ```

      and push the newly created tag and the 3 commits generated by the maven-release-plugin with

      ```bash
      cd ../../
      git push --follow-tags origin master
      ```

   2. otherwise, you can drop the staging repository with

      ```bash
      cd target/checkout
      mvn nexus-staging:drop -P release [-DstagingRepositoryId=<stagingRepositoryId>]
      ```

      and discard the newly created tag and related commits with

      ```bash
      cd ../../
      git reset --hard HEAD^^^
      git tag -d $(git tag -l --sort=-version:refname "rocksdbjni-rmi*" | head -1)
      ```


## Troubleshooting

1. setting pinentry mode 'loopback' failed: Not supported

   This error occurs in [`step 6`](#Development) if you are using GnuPG 2.1.11. To fix this problem,

   ```bash
   echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf
   gpgconf --reload gpg-agent
   ```

   This was [fixed in GnuPG 2.1.12](https://lists.gt.net/gnupg/devel/77927#77927), but if you’re using Ubuntu 16.04 you’re stuck with the affected version. ([source](https://www.fluidkeys.com/tweak-gpg-2.1.11/))
