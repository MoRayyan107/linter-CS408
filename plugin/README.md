# IntelliJ IDEA Plugin

This folder is reserved for a future IntelliJ IDEA plugin for the linter.

The aim is to make the linter easier to use by allowing Java files to be
checked directly inside IntelliJ, rather than running the CLI manually.

The plugin has not been implemented yet. The initial version will likely
connect the existing `linter-core` module to IntelliJ and display linting
results in the editor.

For now, use the [CLI](../linter-cli/README.md). See the
[main README](../README.md) for the current project status and setup commands.

## Build status

There is no plugin implementation or plugin build configuration yet. This
directory is not a Maven module, and the Java CI build does not build or test
an IntelliJ plugin.
