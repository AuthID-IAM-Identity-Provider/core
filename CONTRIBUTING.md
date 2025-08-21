# Contributing to [Project Name]

First off, thank you for considering contributing to [Project Name]! It's people like you that make [Project Name] such a great tool.

This document provides guidelines for contributing to the project.

## How to Contribute

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** to your local machine: `git clone https://github.com/your-username/[project-name].git`
3.  **Create a new branch** for your changes: `git checkout -b my-feature-branch`
4.  **Make your changes** and commit them with a descriptive message (see below).
5.  **Push your branch** to your fork: `git push origin my-feature-branch`
6.  **Open a Pull Request** from your branch to our `main` branch.

## Commit Message Guidelines

We use the **Conventional Commits** specification for our commit messages. This helps us automate versioning and changelog generation.

A commit message should be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Type

The `<type>` must be one of the following:

*   **feat**: A new feature. (Corresponds to a `MINOR` version bump in SemVer)
*   **fix**: A bug fix. (Corresponds to a `PATCH` version bump in SemVer)
*   **docs**: Documentation only changes.
*   **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc).
*   **refactor**: A code change that neither fixes a bug nor adds a feature.
*   **perf**: A code change that improves performance.
*   **test**: Adding missing tests or correcting existing tests.
*   **build**: Changes that affect the build system or external dependencies.
*   **ci**: Changes to our CI configuration files and scripts.
*   **chore**: Other changes that don't modify `src` or `test` files.

### Scope (Optional)

The scope provides additional contextual information and is contained within parentheses, e.g., `feat(api): add new endpoint`.

### Description

The description contains a succinct description of the change:
*   Use the imperative, present tense: "change" not "changed" nor "changes".
*   Don't capitalize the first letter.
*   No dot (.) at the end.

### Body (Optional)

The body should include the motivation for the change and contrast this with previous behavior.

### Footer (Optional)

The footer is the place to reference issue numbers (`Closes #123`).

**Breaking Changes**: A commit that has a footer beginning with `BREAKING CHANGE:` introduces a breaking API change. A breaking change can be part of any type of commit. This corresponds to a `MAJOR` version bump in SemVer.

### Examples

**Commit with a new feature:**
```
feat: allow provided config object to extend other configs
```

**Commit with a bug fix and issue reference:**
```
fix: correct handling of null values in the parser

Closes #45
```

**Commit with a Breaking Change:**
```
feat: change user ID from integer to UUID

BREAKING CHANGE: The `user.id` field is now a string (UUID) instead of an integer. All API consumers must update their data models to handle UUIDs for user IDs.
```

## Semantic Versioning (SemVer)

We follow [Semantic Versioning 2.0.0](https://semver.org/). Our version numbers have the format `MAJOR.MINOR.PATCH`.

The version number is bumped based on the types of commits included in a release:

*   **`MAJOR` version** when you make incompatible API changes (indicated by `BREAKING CHANGE:` in a commit footer).
*   **`MINOR` version** when you add functionality in a backward-compatible manner (indicated by a `feat` commit).
*   **`PATCH` version** when you make backward-compatible bug fixes (indicated by a `fix` commit).

By following these commit conventions, we can automatically determine the next version number and generate a changelog.