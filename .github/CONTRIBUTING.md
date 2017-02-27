# Contributing to clj-symphony
:+1: First off, thanks for taking the time to contribute! :+1:

# Contributor License Agreement (CLA)
A CLA is a document that specifies how a project is allowed to use your
contribution; they are commonly used in many open source projects.

**_All_ contributions to _all_ projects hosted by the
[Symphony Software Foundation](https://symphony.foundation/) must be made with
a [Foundation CLA](https://symphonyoss.atlassian.net/wiki/display/FM/Legal+Requirements#LegalRequirements-ContributorLicenseAgreement)
in place, and there are [additional legal requirements](https://symphonyoss.atlassian.net/wiki/display/FM/Legal+Requirements) that must also be met.**

As a result, PRs submitted to the clj-symphony project cannot be accepted until you have a CLA in place with the Foundation.

# Contributing Issues

## Prerequisites

* [ ] Have you [searched for duplicates](https://github.com/symphonyoss/clj-symphony/issues?utf8=%E2%9C%93&q=)?  A simple search for exception error messages or a summary of the unexpected behaviour should suffice.
* [ ] Are you running the latest version?
* [ ] Are you sure this is a bug or missing capability?

## Raising an Issue
* Create your issue [here](https://github.com/symphonyoss/clj-symphony/issues/new).
* New issues contain two templates in the description: bug report and enhancement request. Please pick the most appropriate for your issue, **then delete the other**.
  * Please also tag the new issue with either "Bug" or "Enhancement".
* Please use [Markdown formatting](https://help.github.com/categories/writing-on-github/)
liberally to assist in readability.
  * [Code fences](https://help.github.com/articles/creating-and-highlighting-code-blocks/) for exception stack traces and log entries, for example, massively improve readability.

# Contributing Pull Requests (Code & Docs)
To make review of PRs easier, please:

 * Please make sure your PRs will merge cleanly - PRs that don't are unlikely to be accepted.
 * For code contributions, follow the existing code layout.
 * For documentation contributions, follow the general structure, language, and tone of the [existing docs](https://github.com/symphonyoss/clj-symphony/wiki).
 * Keep PRs small and cohesive - if you have multiple contributions, please submit them as independent PRs.
 * Reference issue #s if your PR has anything to do with an issue (even if it doesn't address it).
 * Minimise "spurious" changes (e.g. whitespace shenanigans).
 * Ensure all new files include a header comment block containing the [Apache License v2.0 and your copyright information](http://www.apache.org/licenses/LICENSE-2.0#apply).
 * Add the copyright holder of your contribution to the [NOTICE file](https://github.com/symphonyoss/clj-symphony/blob/master/NOTICE)
 * If necessary (e.g. due to 3rd party dependency licensing requirements), update the [NOTICE file](https://github.com/symphonyoss/clj-symphony/blob/master/NOTICE) with any new attribution notices

## Commit and PR Messages

* **Reference issues, wiki pages, and pull requests liberally!**
* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move button left..." not "Moves button left...")
* Limit the first line to 72 characters or less
* Please start the commit message with one or more applicable emoji:
    * Frequently used:
        * :bug: `:bug:` when fixing a bug
        * :new: `:new:` when implementing an enhancement
        * :ballot_box_with_check: `:ballot_box_with_check:` when completing a task
        * :memo: `:memo:` when writing docs
        * :racehorse: `:racehorse:` when improving performance
        * :art: `:art:` when improving the format/structure of the code
    * Infrequently used:
        * :lock: `:lock:` when dealing with security
        * :fire: `:fire:` when removing code or files
        * :arrow_up: `:arrow_up:` when upgrading dependencies
        * :arrow_down: `:arrow_down:` when downgrading dependencies
        * :penguin: `:penguin:` when fixing something on Linux
        * :apple: `:apple:` when fixing something on Mac OS
        * :checkered_flag: `:checkered_flag:` when fixing something on Windows
        * :white_check_mark: `:white_check_mark:` when adding tests
        * :green_heart: `:green_heart:` when fixing the CI build
    * Unlikely to ever be used in this project (but listed, just in case):
        * :non-potable_water: `:non-potable_water:` when plugging memory leaks
