package com.github.typingtanuki.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Bootstrap extends JvmBootstrap {
    public static void main(String... args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.bootSafely();
    }

    @Override
    protected String classpath() {
        return null;
    }

    @Override
    protected List<String> jvmProperties() {
        return null;
    }

    @Override
    protected String mainClass() {
        return TestMain.class.getCanonicalName();
    }

    @Override
    protected Path stdout() {
        return Paths.get("./logs/stdout.log");
    }

    @Override
    protected Path stderr() {
        return Paths.get("./logs/stderr.log");
    }

    @Override
    protected Path programOptionFile() {
        return Paths.get("./conf/options.conf").toAbsolutePath();
    }

    @Override
    protected Path jvmOptionFile() {
        return Paths.get("./conf/jvm.conf").toAbsolutePath();
    }
}
