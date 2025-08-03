package nl.fizzylogic.maven.dependencymanager.commands;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

@TopCommand
@Command(
    mixinStandardHelpOptions = true,
    subcommands = {AddDependencyCommand.class, SearchDependencyCommand.class})
public class RootCommand {}
