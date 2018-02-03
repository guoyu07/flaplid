# Flaplid

foo

## Checks

* Supply Chain
  * **DNS records are exactly as expected**
    * An attacker could change DNS traffic to a page looking just like yours but not under your control.
  * **HTTP request ends at expected address after possible redirects** (simulates full Browser)
    * An attacker with access to your website or DNS could change a link to another resource like, for example, documentation to give malicious instructions to a user who assumes its your instructions. This check is simulating a full browser, including Javascript execution, to even catch last-moment changes to a link by malicious JavaScript code.
  * **HTTP request after click on a link on a website ends at expected address after possible redirects** (simulates full Browser)
  * **A file download triggered by a click on a link on a website downloads a file with an expected checksum** (simulates full Browser)
  * **A file downloaded through a direct HTTP request has the expected checksum**
* Password / User / Configuration audit
  * GitHub Organization
    * Reports users in a organization that do not have two factor authentication enabled
  * Slack Team
    * Reports users in a team that do not have two factor authentication enabled
  * AWS IAM
    * Reports users that did not recently sign in (unused accounts)
    * Reports access keys that were not recently used (unused access keys)
    * Reports users with console access that do not have two factor authentication enabled
    * Reports missing or misconfigured password policies (in a very opionated way)
  * AWS Security Groups
    * Reports security groups that have critical ports open to the world (`0.0.0.0/0`, `::/0`)

## Graylog integration

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
