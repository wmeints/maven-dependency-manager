package com.infosupport.maven;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name="add")
public class AddDependencyCommand  implements Runnable {
    @Parameters(paramLabel = "REFERENCE", description="The dependency <groupId>:<artifactId>:[versionId]")
    String ref;

    @Override
    public void run() {

    }
}
