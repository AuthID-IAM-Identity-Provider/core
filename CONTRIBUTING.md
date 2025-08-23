Contributing Guide
==================

Thank you for your interest in contributing to this project! We deeply appreciate every contribution, from small bug fixes to major new features.

This guide will help you understand our development workflow and how to get your contributions accepted smoothly.

Code of Conduct
---------------

This project and everyone participating in it is governed by the [Code of Conduct](https://www.google.com/search?q=./CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

🚀 Development Workflow
-----------------------

We use a simplified Git Flow-based workflow. Here are the roles of our main branches:

*   **main**: This branch contains stable, production-ready code. Code only enters main through a release process from the beta branch.
    
*   **dev**: The primary development branch. All feature and fix branches are created from dev and merged back into dev via Pull Requests. This branch is the basis for nightly releases.
    
*   **next**: A branch for releasing "bleeding-edge" pre-release versions from main. Used for community testing of new features.
    
*   **beta**: A stabilization branch for major/minor releases. Only bug fixes are allowed here. This is the basis for rc (Release Candidate) releases.
    
*   **\[version\].x** (e.g., 20.x, 21.x): Long-Term Support (LTS) branches. Used to release bug fixes (patches) for older, supported versions.
    

✍️ Commit Message Convention
----------------------------

We use **Conventional Commits** to automate our release process and changelog generation. **All commits must follow this format.**

The Conventional Commits specification is a lightweight convention on top of commit messages. It provides an easy set of rules for creating an explicit commit history; which makes it easier to write automated tools on top of. This convention dovetails with [SemVer](http://semver.org/), by describing the features, fixes, and breaking changes made in commit messages.

The commit message should be structured as follows:

```
<type>(optional scope): <description>

[optional body]

[optional footer(s)]
```

The commit contains the following structural elements, to communicate intent to the consumers of your library:

1.  **fix:** a commit of the _type_ fix patches a bug in your codebase (this correlates with [PATCH](http://semver.org/#summary) in Semantic Versioning).
    
2.  **feat:** a commit of the _type_ feat introduces a new feature to the codebase (this correlates with [MINOR](http://semver.org/#summary) in Semantic Versioning).
    
3.  **BREAKING CHANGE:** a commit that has a footer BREAKING CHANGE:, or appends a ! after the type/scope, introduces a breaking API change (correlating with [MAJOR](http://semver.org/#summary) in Semantic Versioning). A BREAKING CHANGE can be part of commits of any _type_.
    
4.  _types_ other than fix: and feat: are allowed, for example [@commitlint/config-conventional](https://github.com/conventional-changelog/commitlint/tree/master/@commitlint/config-conventional) (based on the [Angular convention](https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines)) recommends build:, chore:, ci:, docs:, style:, refactor:, perf:, test:, and others.
    
5.  _footers_ other than BREAKING CHANGE:  may be provided and follow a convention similar to [git trailer format](https://git-scm.com/docs/git-interpret-trailers).
    

Additional types are not mandated by the Conventional Commits specification, and have no implicit effect in Semantic Versioning (unless they include a BREAKING CHANGE). A scope may be provided to a commit’s type, to provide additional contextual information and is contained within parenthesis, e.g., feat(parser): add ability to parse arrays.

#### Most Common Commit Types Used in this Project:

*   **feat**: For a new feature. (Triggers a **MINOR** release)
    
*   **fix**: For a bug fix. (Triggers a **PATCH** release)
    
*   **docs**: Changes to documentation.
    
*   **style**: Code style changes (whitespace, semicolons, etc).
    
*   **refactor**: A code change that neither fixes a bug nor adds a feature.
    
*   **perf**: A code change that improves performance.
    
*   **test**: Adding or correcting tests.
    
*   **chore**: Changes to the build process, tooling, etc.
    

#### BREAKING CHANGE:

For changes that are not backward-compatible, add ! after the type or BREAKING CHANGE: in the commit footer. (Triggers a **MAJOR** release)

#### Commit Message Examples:
```
# Simple commit
fix(auth): correct JWT token validation

# Commit with a scope and issue reference
feat(api): add user profile endpoint (#42)

# Commit with a BREAKING CHANGE
refactor(db)!: change user_id column type to UUID
```

Pull Request (PR) Process
-------------------------

1.  **Fork & Clone**: Fork this repository and clone it to your local machine.
    
2.  git checkout devgit pullgit checkout -b feat/your-feature-name
    
3.  **Make Changes**: Implement your code changes and ensure all tests pass.
    
4.  **Commit**: Create commits following the [Commit Message Convention]() above.
    
5.  **Push**: Push your branch to your fork.
    
6.  **Create a Pull Request**: Open a Pull Request from your branch to the dev branch of the main repository.
    
    *   Use the provided PR template.
        
    *   Ensure you link any relevant issues using keywords like Closes #123.
        
    *   The commit list will be auto-filled by a GitHub Action.
        
7.  **Review**: Wait for a review from our team. You may be asked to make some changes.
    

Thank you again for your contribution!