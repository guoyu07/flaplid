# Flaplid

[![Build Status](https://travis-ci.org/lennartkoopmann/flaplid.svg?branch=master)](https://travis-ci.org/lennartkoopmann/flaplid/)
[![License](https://img.shields.io/github/license/lennartkoopmann/flaplid.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)

Flaplid is an easy to use suite of pre-built security checks to continuously monitor security relevant configuration. It
is supposed to be run as a cron job and will report issues it finds for each configured check.

## Checks

Currently, the following check types can be configured for Flaplid. See also _Writing your own checks_ below and
[Configuring Flaplid](https://github.com/lennartkoopmann/flaplid/wiki/Configuring-Flaplid) in the wiki.

* _Supply Chain_
  * **DNS records are exactly as expected** - [Configure this check](https://github.com/lennartkoopmann/flaplid/wiki/Checks:-DNS)
    * An attacker could change DNS traffic to a page looking just like yours but not under your control. (See also [Lessons learned from a Man-in-the-Middle attack](https://web.archive.org/web/20180121023728/https://www.fox-it.com/en/insights/blogs/blog/fox-hit-cyber-attack/)
  * **HTTP request ends at expected address after possible redirects** (simulates full browser, stores artifacts locally
    for forensics)
    * An attacker with access to your website or DNS could change a link to another resource like, for example,
      documentation to give malicious instructions to a user who assumes its your instructions. This check is simulating
      a full browser, including Javascript execution, to even catch last-moment changes to a link by malicious JavaScript
      code.
  * **HTTP request after click on a link on a website ends at expected address after possible redirects**
    (simulates full browser, stores artifacts locally for forensics)
    - [Configure this check](https://github.com/lennartkoopmann/flaplid/wiki/Checks:-Website-Link-Target)
  * **A file download triggered by a click on a link on a website downloads a file with an expected checksum**
    (simulates full browser, stores artifacts locally for forensics)
  * **A file downloaded through a direct HTTP request has the expected checksum**
* _Password / User / Configuration audit_
  * **GitHub Organization** - [Configure this check](https://github.com/lennartkoopmann/flaplid/wiki/Checks:-GitHub)
    * Reports users in a organization that do not have two factor authentication enabled
  * **Slack Team**
    * Reports users in a team that do not have two factor authentication enabled
  * **AWS IAM**
    * Reports users that did not recently sign in (unused accounts)
    * Reports access keys that were not recently used (unused access keys)
    * Reports users with console access that do not have two factor authentication enabled
    * Reports missing or misconfigured password policies (in a very opionated way)
  * **AWS Security Groups**
    * Reports security groups that have critical ports open to the world (`0.0.0.0/0`, `::/0`)

## Prerequisites

All you need is a **operating system with at least Java 8 installed**. (`sudo apt install openjdk-8-jre-headless`) Flaplid
is known to be working on Linux, OSX and Windows.

For checks that use a headless browser, you have to install phantomjs. Do not use the phantomjs provided by your operating system package manager because it will be most likely too old. Instead, download the binary from [phantomjs.org](http://phantomjs.org/download.html)

On Linux, the phantomjs installation looks like this:

```
$ wget https://github.com/ariya/phantomjs/releases/download/2.1.3/phantomjs # replace with latest version
$ chmod +x phantomjs
$ sudo mv phantomjs /usr/bin
$ phantomjs -v
2.1.3-dev-release
```

It is recommended to connect a [Graylog](https://www.graylog.org/) (free and open source log management - see
_Graylog integration_ below) system for easy reporting and alerting but you could run Flaplid without it.

Flaplid does not require any significant resources. It will run absolutely fine on the smallest public cloud hosts or any
Raspberry Pi model, including the Pi Zero.

## Installation and usage

All you need is the Flaplid JAR file, downloaded from the [Releases page](https://github.com/lennartkoopmann/flaplid/releases).

Example execution:

```
$ java -Xmx512m -jar flaplid-0.4.jar -c config.yml --tags foo,bar,baz
2018-02-10T18:43:15.525 [main] INFO  horse.wtf.flaplid.Main - Starting up. (•_•) .. ( •_•)>⌐■-■ .. (⌐■_■)
2018-02-10T18:43:15.528 [main] INFO  horse.wtf.flaplid.Main - Version: 0.3 built at [2018-02-11T00:35:54Z].
2018-02-10T18:43:15.555 [main] INFO  horse.wtf.flaplid.Main - Running all enabled checks matching any of the following tags: [testing, slack].
2018-02-10T18:43:15.715 [main] INFO  horse.wtf.flaplid.Main - Including configuration from [/mnt/workspace/flaplid/conf.d].
2018-02-10T18:43:15.718 [main] INFO  horse.wtf.flaplid.Main - Reading configuration from [/mnt/workspace/flaplid/conf.d/acmecorp_dns.yml].
2018-02-10T18:43:15.850 [main] INFO  horse.wtf.flaplid.Main - Flaplid run ID is [3b57a0a5-fde9-4113-98af-018a78844cb6] on sensor [flaplid-checks-1].
2018-02-10T18:43:15.867 [main] INFO  horse.wtf.flaplid.Main - Not running check [acmecorp-aws-iam] because it does not have any of the requested tags.
2018-02-10T18:43:15.867 [main] INFO  horse.wtf.flaplid.Main - Not running check [acmecorp-aws-security-groups] because it does not have any of the requested tags.
2018-02-10T18:43:15.871 [main] INFO  horse.wtf.flaplid.checks.Check - Running check [slack_team:acmecorp#warning].
2018-02-10T18:43:16.315 [main] INFO  horse.wtf.flaplid.checks.Check - Running check [dns:testing-a-failing-test#emergency].
2018-02-10T18:43:16.400 [main] INFO  horse.wtf.flaplid.checks.Check - Running check [dns:testing-another-failing-test#emergency].
2018-02-10T18:43:16.960 [main] INFO  horse.wtf.flaplid.checks.Check - Running check [website_download:graylog-downloads-tgz#emergency].
2018-02-10T18:43:16.930 [main] INFO  horse.wtf.flaplid.checks.supplychain.helpers.PhantomJS - Opening [https://www.graylog.org/downloads] to get element link via CSS selector [.button-10 (ix#0)].
2018-02-10T18:43:16.391 [main] INFO  horse.wtf.flaplid.checks.supplychain.helpers.FileDownloader - Downloading file from [https://packages.graylog2.org/releases/graylog/graylog-2.4.3.tgz].
2018-02-10T18:43:16.054 [main] INFO  horse.wtf.flaplid.checks.supplychain.WebsiteDownloadCheck - Completed download. Comparing checksums. Expecting checksum [c6c2e029307abda5e55603375797bec1a4c44fbc2e99988527f4639d6a7a8f4f]
2018-02-10T18:43:16.065 [main] INFO  horse.wtf.flaplid.checks.supplychain.WebsiteDownloadCheck - Checksums match. (c6c2e029307abda5e55603375797bec1a4c44fbc2e99988527f4639d6a7a8f4f==c6c2e029307abda5e55603375797bec1a4c44fbc2e99988527f4639d6a7a8f4f)
2018-02-10T18:43:16.066 [main] INFO  horse.wtf.flaplid.checks.supplychain.WebsiteDownloadCheck - Deleting downloaded file from attic as requested. (archive_matched_files:false)

[... check results below ...]

2018-02-10T18:43:16.484 [main] WARN  horse.wtf.flaplid.Main - Check slack_team:acmecorp-slack#warning: Team member has no MFA device configured: john_doe (John Doe)
2018-02-10T18:43:16.484 [main] WARN  horse.wtf.flaplid.Main - Check slack_team:acmecorp-slack#warning: Team member has no MFA device configured: jane_doe (Jane Doe)
2018-02-10T18:43:16.486 [main] WARN  horse.wtf.flaplid.Main - Check dns:testing-a-failing-test#emergency: Expected records [173.194.67.27, alt1.aspmx.l.google.com, alt2.aspmx.l.google.com, aspmx2.googlemail.com, aspmx3.googlemail.com] but found [alt1.aspmx.l.google.com, alt2.aspmx.l.google.com, aspmx2.googlemail.com, aspmx3.googlemail.com, aspmx.l.google.com].
2018-02-10T18:43:16.486 [main] WARN  horse.wtf.flaplid.Main - Check dns:testing-another-failing-test#emergency: Expected no DNS records but found <1>. The records are: [188.166.203.69]
```

The `-Xmx512m` flag instructs Java to use a maximum of 512 MB for heap space. Increase this number if you are getting a `java.lang.OutOfMemoryError: Java heap space` error.

Available CLI arguments:

* `--config-file`, `-c` (required): Path to Flaplid configuration file (see _Configuration_ below)
* `--tags`, `-t` (optional): Only run checks with specified tags. Multiple tags can be specified as comma separated values. All tags are run if this argument is absent.

## Configuration

The Flaplid configuration file has to be in YAML format. Note that for valid YAML, indention is important and must be 
set to two whitespaces. This can be annoying to debug but leads to vastly improved readability compared to JSON, XML or
flat configuration files.

The following parameters are available:

* `sensor_id` (required): The name of this flaplid instance. Useful when running multiple sensors. Example: `flaplid-checks-1`
* `attic_folder` (required): Relative path to a local folder that will be used to store artifacts of failed checks. For example, if
                  a file download check raises an issue, the downloaded file will be stored in the `attic_folder` to
                  allow later analysis and forensics. (See _Attic_ below)
* `graylog_address` (optional): Address (format: `host:port`) of a Graylog GELF TCP input for transmission of results
                                and status information after each Flaplid run. (See _Graylog integration_ below) 
* `checks` (required): An array of check configurations

An example configuration can be found [here](https://github.com/lennartkoopmann/flaplid/blob/master/config.yml.example).

See _Checks_ above for all available checks and their respective configurations.

### Including multiple configuration files

The Flaplid configuration file can grow pretty large if you are running many checks. To allow organizing check
configurations into separate files, you can instruct Flaplid to read all files in another folder that end with `.yml`
and merge their configurations.

For example, your main `config.yml` could look like this:

```
sensor_id: flaplid-checks-1
attic_folder: attic/
graylog_address: graylog.example.com:12000
include: conf.d/
```

The `conf.d` folder has the following files:

```
$ ls -al conf.d
drwxrwxr-x 2 tun3 tun3 4096 Feb 10 19:31 .
drwxr-xr-x 8 tun3 tun3 4096 Feb 10 19:30 ..
-rw-r--r-- 1 tun3 tun3    0 Feb 10 19:31 aws_checks.yml
-rw-r--r-- 1 tun3 tun3    0 Feb 10 19:31 dns_checks.yml
-rw-r--r-- 1 tun3 tun3    0 Feb 10 19:31 website_checks.yml
```

Each of the `.yml` files in `conf.d` contain their own `checks` array:

```
checks:
  - type: website_redirect
    id: rdr-graylog-net
    severity: emergency
    tags:
      - redirects
      - slow
    enabled: true
    url: http://graylog.net/
    expected_final_target: https://www.graylog.org/
    archive_mismatches: true
    archive_matches: false

  - type: website_redirect
    id: rdr-www-graylog-net
    severity: emergency
    tags:
      - redirects
      - slow
    enabled: true
    url: http://www.graylog.net/
    expected_final_target: https://www.graylog.org/
    archive_mismatches: true
    archive_matches: false
```

Flaplid will merge all the `checks` arrays it finds into one large array and then execute each test.

## Attic

TBD

## Graylog integration

TBD

## Writing your own checks

TBD

## Local development

### Getting an IDE to correctly build the version and git information

We show version and build information during startup:

> 18:38:40.299 [main] INFO Main - Version: 0.1-SNAPSHOT built at [2017-12-24T17:38:36Z] from [d5af363-dirty].

The required information is built by maven during the final release and packaging step, but probably not by your IDE.
Tell your IDE to execute the required maven goals before each local build like this in, for example, IntelliJ IDEA:

![IntelliJ build steps](maven-build-ide.png)

The maven goals are:

* `git-commit-id:revision`
* `resources:resources`

Note that this is completely optional and will slow down the IDE build process.

### Releasing a new version

Project maintainers with authorization can release a new version like this:

* `mvn release:prepare`
* `mvn release:peform`
* `git fetch # to pull local state after maven changed it on remote`
* Upload artifact from `target/` to Github releases. This is currently not automated but could be in the future.
