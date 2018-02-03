# Flaplid

foo

## Checks

* Supply Chain
  * DNS records are exactly as expected
  * HTTP request ends at expected address after possible redirects (simulates full Browser)
  * HTTP request after click on a link on a website ends at expected address after possible redirects (simulates full Browser)
  * A file download triggered by a click on a link on a website downloads a file with an expected checksum (simulates full Browser)
  * A file downloaded through a direct HTTP request has the expected checksum
* Password / User / Configuration audit
  * GitHub Organization
    * All users in a organization ahve two factor authentication enabled
  * Slack Team
  * AWS IAM
  * AWS Security Groups

## Writing your own checks

## Local development

### Getting an IDE to correctly build the version and git information

We show version and build information during startup:

> 18:38:40.299 [main] INFO Main - Version: 0.1-SNAPSHOT built at [2017-12-24T17:38:36Z] from [d5af363-dirty].

The required information is built by maven during the final release and packaging step, but probably not by your IDE. Tell your IDE to execute the required maven goals before each local build like this in, for example, IntelliJ IDEA:

![IntelliJ build steps](maven-build-ide.png)

The maven goals are:

* `git-commit-id:revision`
* `resources:resources`
