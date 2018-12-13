package nxt.tools;

import nxt.util.security.BlockchainPermission;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JDKToolsWrapper {

    /**
     * We rely on verbose compilation to determine which class files were created.
     * Example for the line we are looking for using this pattern:
     * Javac 8 - [wrote RegularFileObject[C:\Users\riker\AppData\Local\Temp\src\com\jelurida\ardor\contracts\ForgingReward.class]]
     * Javac 10 - [wrote DirectoryFileObject[C:\Users\riker\AppData\Local\Temp\src:com/jelurida/ardor/contracts/ForgingReward.class]]
     */
    private static final Pattern JAVAC_8_CLASS_COMPILATION_EVENT = Pattern.compile("^\\[wrote RegularFileObject\\[(.*)\\]\\]");
    private static final Pattern JAVAC_10_CLASS_COMPILATION_EVENT = Pattern.compile("^\\[wrote DirectoryFileObject\\[(.*):([^\\\\].*)\\]\\]");
    private static final Pattern JAVAC_11_CLASS_COMPILATION_EVENT = Pattern.compile("^\\[wrote (.*)\\]");

    enum OPTION {
        SOURCE('s', "source", true, "path to source code file to verify", false, (OPTION)null),
        JAVAC('j', "javac", true, "javac options (space separated, surround with quotes)", false, (OPTION)null);

        private final char opt;
        private final String longOpt;
        private boolean hasArgs;
        private String description;
        private boolean isAction;
        private OPTION[] dependencies;

        OPTION(char opt, String longOpt, boolean hasArgs, String description, boolean isAction, OPTION... dependencies) {
            this.opt = opt;
            this.longOpt = longOpt;
            this.hasArgs = hasArgs;
            this.description = description;
            this.isAction = isAction;
            this.dependencies = dependencies;
        }

        public String getOpt() {
            return Character.toString(opt);
        }

        public String getLongOpt() {
            return longOpt;
        }

        public boolean hasArgs() {
            return hasArgs;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAction() {
            return isAction;
        }

        public OPTION[] getDependencies() {
            return dependencies;
        }
    }

    public static void main(String[] args) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        Options options = new Options();
        Arrays.stream(OPTION.values()).forEach(o -> options.addOption(new Option(o.getOpt(), o.getLongOpt(), o.hasArgs(), o.getDescription())));
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(JDKToolsWrapper.class.getName(), options);
            return;
        }
        if (!cmd.hasOption(OPTION.SOURCE.longOpt)) {
            formatter.printHelp(JDKToolsWrapper.class.getName(), options);
            return;
        }
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path outputPath = Paths.get(tmpDir, "src");
        Map<String, byte[]> compiledClasses = JDKToolsWrapper.compile(cmd.getOptionValue(OPTION.SOURCE.longOpt), cmd.getOptionValue(OPTION.JAVAC.longOpt), outputPath);
        if (compiledClasses == null) {
            System.out.println("No classes compiled");
            return;
        }
        compiledClasses.keySet().forEach(c -> System.out.printf("Class %s bytes size %d", c, compiledClasses.getOrDefault(c, new byte[0]).length));
    }

    public static Map<String, byte[]> compile(String source, String javacOptions, Path outputPath) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        System.out.printf("Compiling source file %s with compiler options %s\n", source, javacOptions);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("Java compiler is not supported when running JRE use JDK instead");
            return null;
        }
        if (!Files.exists(outputPath)) {
            try {
                Files.createDirectory(outputPath);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, Charset.forName("UTF8"));
        try {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputPath.toFile()));
        } catch (IOException e) {
            throw new IllegalStateException();
        }
        File file = Paths.get(source).toFile();
        Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(file));
        StringWriter output = new StringWriter();

        List<String> options;
        List<String> defaultOptions = Arrays.asList("-encoding", "utf8", "-verbose");
        if (javacOptions == null || javacOptions.equals("")) {
            options = defaultOptions;
        } else {
            options = Arrays.asList(javacOptions.split(" "));
            options = Stream.concat(options.stream(), defaultOptions.stream()).collect(Collectors.toList());
        }
        JavaCompiler.CompilationTask task = compiler.getTask(output, fileManager, diagnostics, options, null, compilationUnit);
        Boolean result = task.call();
        System.out.println("Compilation Succeeded: " + result);
        List<Diagnostic<? extends JavaFileObject>> diagnosticList = diagnostics.getDiagnostics();
        String messages = diagnosticList.stream().map(Object::toString).collect(Collectors.joining("\n"));
        System.out.println("Compilation messages: " + messages);
        System.out.println("Output: " + output.toString());
        if (!result) {
            return null;
        }
        String outputStr = output.toString();
        String[] outputLines = outputStr.split("\r\n");
        Map<String, byte[]> compiledClasses = new HashMap<>();
        for (String line : outputLines) {
            Path path;
            Matcher matcher = JAVAC_10_CLASS_COMPILATION_EVENT.matcher(line);
            if (matcher.matches() && matcher.groupCount() == 2) {
                String classFolderPath = matcher.group(1);
                String classFilePath = matcher.group(2);
                path = Paths.get(classFolderPath, classFilePath);
            } else {
                matcher = JAVAC_8_CLASS_COMPILATION_EVENT.matcher(line);
                if (matcher.matches() && matcher.groupCount() == 1) {
                    String classFilePath = matcher.group(1);
                    path = Paths.get(classFilePath);
                } else {
                    matcher = JAVAC_11_CLASS_COMPILATION_EVENT.matcher(line);
                    if (matcher.matches() && matcher.groupCount() == 1) {
                        String classFilePath = matcher.group(1);
                        path = Paths.get(classFilePath);

                    } else {
                        continue;
                    }
                }
            }
            byte[] b;
            try {
                b = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            compiledClasses.put(path.toAbsolutePath().toString(), b);
        }
        return compiledClasses;
    }

    enum DEBUG_INFO {
        SOURCE("Compiled from \""),
        LINES("LineNumberTable:"),
        VARS("LocalVariableTable:");

        DEBUG_INFO(String identifier) {
            this.identifier = identifier;
        }

        private String identifier;

        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * Given the bytes of a class file, this tool reconstructs which javac -g option was used when compiling the class
     * See: http://www.herongyang.com/Java-Tools/javac-g-Controlling-Debugging-Information.html
     * See: https://www.javaworld.com/article/2073963/core-java/determining-level-of-java-debug-in-class-file-via-javap.html
     * @param classBytes the byte array representing the class bytes
     * @return the javac -g command line options
     */
    public static String javap(byte[] classBytes) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path outputPath = Paths.get(tmpDir, "javapdata");
        Path classFile = outputPath.resolve("Temp.class");
        if (!Files.exists(outputPath)) {
            try {
                Files.createDirectory(outputPath);
                Files.write(classFile, classBytes);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        boolean result;
        try {
            // JavapFileManager fileManager = JavapFileManager.create(diagnostics, writer);
            Class<?> javapFileManagerClass = Class.forName("com.sun.tools.javap.JavapFileManager");
            Method create = javapFileManagerClass.getMethod("create", DiagnosticListener.class, PrintWriter.class);
            JavaFileManager fileManager = (JavaFileManager)create.invoke(null, diagnostics, writer);

            // JavapTask javapTask = new JavapTask(writer, fileManager, diagnostics, Collections.singletonList("-v"), Collections.singletonList(classFile.toString()));
            Class<?> javapTaskClass = Class.forName("com.sun.tools.javap.JavapTask");
            Constructor<?> constructor = javapTaskClass.getConstructor(Writer.class, JavaFileManager.class, DiagnosticListener.class, Iterable.class, Iterable.class);
            Object javapTask = constructor.newInstance(writer, fileManager, diagnostics, Collections.singletonList("-v"), Collections.singletonList(classFile.toString()));

            // boolean result = javapTask.call();
            Method call = javapTaskClass.getMethod("call");
            result = (Boolean)call.invoke(javapTask);
        } catch (Throwable t) {
            if (t instanceof ClassNotFoundException) {
                System.out.println("Javap tool is not supported when running JRE use JDK instead");
                return null;
            } else {
                throw new IllegalStateException(t);
            }
        }
        List<Diagnostic<? extends JavaFileObject>> diagnosticList = diagnostics.getDiagnostics();
        String messages = diagnosticList.stream().map(Object::toString).collect(Collectors.joining("\n"));
        System.out.println("Javap messages: " + messages);
        System.out.println("Javap Output: " + output.toString());
        if (!result) {
            System.out.println("Javap failed");
            return null;
        }
        List<String> lines = Arrays.asList(output.toString().split("\\r?\\n"));

        // Convert major version to target release 52 --> 8, 54 --> 10 we assume that the difference will stay 44 in future Java releases.
        // Make sure our version and the compiler version used to compile the class are the same
        int contractCompilerVersion = lines.stream().filter(line -> line.contains("major version:")).mapToInt(line -> Integer.parseInt(line.split(": ")[1]) - 44).findFirst().orElse(-1);
        String version = System.getProperty("java.version");
        String[] tokens = version.split("\\.");
        int myCompilerVersion = Integer.parseInt(tokens.length == 0 ? version : tokens[0].equals("1") ? tokens[1] : tokens[0]);
        if (contractCompilerVersion != myCompilerVersion) {
            System.out.printf("Contract was compiled with Java %d, you are using Java %d, re-run the verification with Java JDK version %d\n",
                    contractCompilerVersion, myCompilerVersion, contractCompilerVersion);
            return null;
        }

        // Guess the debug info flags used when compiling the contract
        List<String> debugInfo = new ArrayList<>();
        for (DEBUG_INFO info : DEBUG_INFO.values()) {
            if (lines.stream().anyMatch(line -> line.contains(info.getIdentifier()))) {
                debugInfo.add(info.toString().toLowerCase());
            }
        }
        if (debugInfo.size() == 0) {
            return "-g:none";
        } else if (debugInfo.size() == 2 && debugInfo.contains(DEBUG_INFO.SOURCE.toString().toLowerCase()) && debugInfo.contains(DEBUG_INFO.LINES.toString().toLowerCase())) {
            return "";
        } else if (debugInfo.size() == 3) {
            return "-g";
        } else {
            return "-g:" + String.join(",", debugInfo);
        }
    }

}
