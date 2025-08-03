package nl.fizzylogic.maven.dependencymanager;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;


@TopCommand
@Command(mixinStandardHelpOptions = true, subcommands = {AddDependencyCommand.class, SearchCommand.class})
public class RootCommand {
}
