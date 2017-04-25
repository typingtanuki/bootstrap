# Simple tool for starting JVMs in java

## Motivation

Java was meant to be platform agnostic, but when packaging a java application the bootstrap can be difficult.
Each application needs to be bundled with overly complex platform specific scripts, application arguments are mixed with
JVM arguments resulting in very complex setups for customers.

The simple tool allows you to write minimal startup scripts and have:

* Full control over the flags passed to the JVM
* Full control over the arguments passed to your application
* User friendly settings for both JVM flags and application arguments with comments

Have no dependencies.

## How to use
 
### Create your own bootstrap
 
Extend the JvmBootstrap class and implement the few abstract methods from it:
```java
public class MyBootstrap extends JvmBootstrap {
    /**
    * Minimal setup
    */
    public static void main(String... args) {
        MyBootstrap bootstrap = new MyBootstrap();
        bootstrap.bootSafely();
    }

    /**
    * Extra classpath, if <code>null</code> or empty string only current classpath will be used
    */
    @Override
    protected String classpath() {
        return null;
    }

    /**
    * Name of the main class to load in the new JVM
    */
    @Override
    protected String mainClass() {
        return TestMain.class.getCanonicalName();
    }

    /**
    * Path to where the standard out of the new JVM should be saved
    */
    @Override
    protected Path stdout() {
        return Paths.get("./logs/stdout.log");
    }

    /**
    * Path to where the standard err of the new JVM should be saved
    */
    @Override
    protected Path stderr() {
        return Paths.get("./logs/stderr.log");
    }

    /**
    * Path to the file containing the program arguments
    */
    @Override
    protected Path programOptionFile() {
        return Paths.get("./conf/options.conf");
    }

    /**
    * Path to the file containing the JVM flags
    */
    @Override
    protected Path jvmOptionFile() {
        return Paths.get("./conf/jvm.conf");
    }
}
```

Then you just have to make scripts which will run this class, 
without any JVM flags required and everything else will be taken care of.

### JVM option file format

The format is simple:

* 1 line per JVM flag
* `#` is used for comments, whole lines can be comments or just the end of a line
* Any blank line is ignore

Sample:
```yaml
####
## Comments can be used to make headers
####

#Flag Xmx controls the .... and can be used for ...
-Xmx2g
#Flag Xms controls the .... and can be used for ...
-Xms2g

#This property is used for...
-Dmy.propert=someValue #If value is 42, then ...
```

### Program option file format

The format is simple:

* 1 line per argument
* Argument name and value is separated by an `=`
* `#` is used for comments, whole lines can be comments or just the end of a line
* Any blank line is ignore

Sample:
```yaml
####
## Comments can be used to make headers
####

#Host name
#If localhost, 127.0.0.1 or ...
host=localhost
#Port for the API
port=12345 #Default 45454

#This is a short flag
h=12

#If you uncomment the following line, you will enable...
#enableMe=true
```

### Controlling program arguments format

Different application use different scheme for arguments.
By default the bootstrap will use single dash `-` for short arguments (1 char long)
and double dashes `--` for longer arguments.

If you want to use a different scheme, pass a different `ArgumentsType` at construction time.

## Limitations

Will only work on JVM implementations where:

* The java binary is located in a `bin` folder
* The java binary is `java`

This is true for sun/oracle java as well as openJDK but might not be true from other vendors.

## License

Released under [MIT](LICENSE). Copyright (c) Clerc Mathias