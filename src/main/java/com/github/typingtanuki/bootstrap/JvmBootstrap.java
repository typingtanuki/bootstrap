package com.github.typingtanuki.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Starts a separate JVM, using the same java as this bootstrap
 * <p>
 * The flags for the jvm are read from a settings file, as well as the program arguments.
 * Both support comments to make it easier to document and use.
 *
 * @author Clerc Mathias
 */
public abstract class JvmBootstrap {
    /**
     * The character used for comments
     */
    private static final String COMMENT_CHAR = "#";
    /**
     * The way to format arguments
     */
    private final ArgumentsType argumentsType;

    public JvmBootstrap() {
        this(ArgumentsType.SINGLE_DOUBLE_DASH);
    }

    public JvmBootstrap(ArgumentsType argumentsType) {
        super();
        this.argumentsType = argumentsType;
    }

    /**
     * Parse the configuration and start the JVM.
     * For a version which does not send exception but sets and exit code, use {@link JvmBootstrap#bootSafely()}
     *
     * @throws BootstrapException If there have been any problems while reading configuration or starting the JVM
     */
    public void boot() throws BootstrapException {
        //Parse the settings
        List<String> jvmArguments = parseJvmArguments();
        List<String> programArguments = parseProgramArguments();

        //Build the actual command
        List<String> startupCommand = new LinkedList<>();
        startupCommand.add(pathToJava().toString());
        startupCommand.addAll(jvmArguments);
        startupCommand.add(mainClass());
        startupCommand.addAll(programArguments);

        //Java process builder
        ProcessBuilder builder = new ProcessBuilder(startupCommand);
        builder.environment().put("JAVA_HOME", pathToJavaHome().toString());
        builder.environment().put("CLASSPATH", makeClasspath());

        Path stdout = stdout();
        Path stderr = stderr();

        try {
            Files.createDirectories(stdout.getParent());
        } catch (IOException e) {
            throw new BootstrapException("Error creating directory for log files at: " + stdout.toString(), e);
        }
        try {
            Files.createDirectories(stderr.getParent());
        } catch (IOException e) {
            throw new BootstrapException("Error creating directory for log files at: " + stderr.toString(), e);
        }

        builder.redirectOutput(stdout.toFile());
        builder.redirectError(stderr.toFile());
        try {
            builder.start();
        } catch (IOException e) {
            throw new BootstrapException(
                    "Error starting process with arguments: \r\n" + String.join(" ", startupCommand), e);
        }
    }

    /**
     * Parse the configuration and start the JVM.
     * For a version which sends exception and does not stop the JVM, use {@link JvmBootstrap#boot()}
     * <p>
     * Exit values:
     * <ul>
     * <li>10: Configuration problem</li>
     * <li>11: Unexpected problem</li>
     * </ul>
     */
    public void bootSafely() {
        try {
            boot();
        } catch (BootstrapException e) {
            System.err.println("Error during bootstrap: " + e.getMessage());
            e.printStackTrace();
            System.exit(10);
        } catch (RuntimeException e) {
            System.err.println("Unexpected error during bootstrap: " + e.getMessage());
            e.printStackTrace();
            System.exit(11);
        }
    }

    /**
     * The canonical name of the class to run
     */
    protected abstract String mainClass();

    /**
     * @return The path to the file into which stdout should be saved (can be same as stderr)
     */
    protected abstract Path stdout();

    /**
     * @return The path to the file into which stderr should be saved (can be same as stdout)
     */
    protected abstract Path stderr();

    /**
     * @return The path to the file containing the arguments for the program
     */
    protected abstract Path programOptionFile();

    /**
     * @return The path to the file containing the JVM flags
     */
    protected abstract Path jvmOptionFile();

    /**
     * @return Extra classpath to give to the new jvm. Those will be added to the current classpath. Can be null.
     */
    protected abstract String classpath();


    /**
     * @return the current classpath + the result of {@link JvmBootstrap#classpath()} (if any)
     */
    private String makeClasspath() {
        String extraClasspath = classpath();
        if (extraClasspath == null || extraClasspath.trim().isEmpty()) {
            return System.getProperty("java.class.path");
        }
        return System.getProperty("java.class.path") + File.pathSeparator + extraClasspath;
    }

    /**
     * @return The path to the home of the current java (can be different from JAVA_HOME system property)
     * @throws BootstrapException if the home folder does not exist (should never happen)
     */
    private Path pathToJavaHome() throws BootstrapException {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        if (!Files.exists(javaHome)) {
            throw new BootstrapException("Error getting current java path, missing at: " + javaHome.toString());
        }
        return javaHome;
    }

    /**
     * @return The path to the binary of the current java
     * @throws BootstrapException if the binary folder/name does not match the one from oracle (can happen with strange JVMs)
     */
    private Path pathToJava() throws BootstrapException {
        Path java = pathToJavaHome().resolve("bin").resolve("java");
        if (!Files.exists(java)) {
            throw new BootstrapException("Error getting current java path, missing at: " + java.toString());
        }
        return java;
    }

    /**
     * Read the jvm config file and extract the flags.
     * <p>
     * The format is 1 flag per line
     * <p>
     * Commented lines, blank lines, comments at the end of a line, ... will be stripped.
     *
     * @return the flags
     * @throws BootstrapException If the file is missing or unreadable
     */
    private List<String> parseJvmArguments() throws BootstrapException {
        List<String> lines = readSettings(jvmOptionFile());

        List<String> arguments = new LinkedList<>();
        for (String rawLine : lines) {
            String line = cleanLine(rawLine);
            if (!line.isEmpty()) {
                arguments.add(line);
            }
        }
        return arguments;
    }

    /**
     * Read the program config file and extract the arguments.
     * <p>
     * The format is 1 argument per line in format <code>flagName=value</code>. When the actual main class will run
     * if will be passed as <code>-flagName=value</code>.
     * <p>
     * Commented lines, blank lines, comments at the end of a line, ... will be stripped.
     *
     * @return the arguments
     * @throws BootstrapException If the file is missing, unreadable or some arguments can not be read
     */
    private List<String> parseProgramArguments() throws BootstrapException {
        List<String> lines = readSettings(programOptionFile());

        List<String> arguments = new LinkedList<>();
        for (String rawLine : lines) {
            String line = cleanLine(rawLine);
            if (!line.isEmpty()) {
                if (line.indexOf('=') == -1) {
                    throw new BootstrapException("Invalid setting detected, missing '=' for " + line);
                }
                String key = line.split("=", 2)[0].trim();
                String value = line.split("=", 2)[1].trim();
                if (key.isEmpty()) {
                    throw new BootstrapException("Invalid setting detected, missing key for " + line);
                }
                if (value.isEmpty()) {
                    throw new BootstrapException("Invalid setting detected, missing value for " + line);
                }
                if (key.length() == 1) {
                    arguments.add(argumentsType.getShortArg() + key + "=" + value);
                } else {
                    arguments.add(argumentsType.getLongArg() + key + "=" + value);
                }
            }
        }
        return arguments;
    }

    /**
     * Check the existence of the settings file and read the content.
     *
     * @param settings the path to the settings file
     * @return the lines in the settings file
     * @throws BootstrapException if the file is missing or unreadable
     */
    private List<String> readSettings(Path settings) throws BootstrapException {
        if (!Files.exists(settings)) {
            throw new BootstrapException("Missing option file: " + settings.toString());
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(settings);
        } catch (IOException e) {
            throw new BootstrapException("Could not read option file: " + settings.toString(), e);
        }
        return lines;
    }

    /**
     * @return the line, without any comment and trimmed
     */
    private String cleanLine(String rawLine) {
        return rawLine.split(COMMENT_CHAR)[0].trim();
    }
}
