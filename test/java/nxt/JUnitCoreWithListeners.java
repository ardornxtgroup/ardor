package nxt;

import junit.runner.Version;
import org.junit.internal.JUnitSystem;
import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;

public class JUnitCoreWithListeners extends JUnitCore {
    public static void main(String[] args) {
        runMainAndExit(new RealSystem(), args);
    }

    private static void runMainAndExit(JUnitSystem system, String... args) {
        JUnitCoreWithListeners core = new JUnitCoreWithListeners();
        core.addListener(new TextListener(system));
        MyLogger myLogger = (template, parameters) -> System.out.println(String.format(template, parameters));
        core.addListener(new LoggingListener(myLogger));
        Result result = core.runMain(system, args);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }

    private Result runMain(JUnitSystem system, String... args) {
        system.out().println("JUnit version " + Version.id());
        List<Class<?>> classes = new ArrayList<>();
        List<Failure> missingClasses = new ArrayList<>();
        for (String each : args) {
            try {
                classes.add(Class.forName(each));
            } catch (ClassNotFoundException e) {
                system.out().println("Could not find class: " + each);
                Description description = Description.createSuiteDescription(each);
                Failure failure = new Failure(description, e);
                missingClasses.add(failure);
            }
        }
        RunListener listener = new TextListener(system);
        addListener(listener);
        Result result = run(classes.toArray(new Class[0]));
        for (Failure each : missingClasses) {
            result.getFailures().add(each);
        }
        return result;
    }

    @FunctionalInterface
    private interface MyLogger {
        void log(String template, Object... parameters);
    }

    private static class LoggingListener extends RunListener {
        private final MyLogger logger;

        private LoggingListener(MyLogger logger) {
            this.logger = logger;
        }

        @Override
        public void testRunStarted(Description description) {
            logger.log("Test Run started %s", description.getDisplayName());
        }

        @Override
        public void testRunFinished(Result result) {
            int failed = result.getFailureCount();
            int ignored = result.getIgnoreCount();
            if (failed == 0 && ignored == 0) {
                logger.log("Test Run finished, took %s ms (tests passed: %s)", result.getRunTime(), result.getRunCount());
            } else {
                logger.log("Test Run finished, took %s ms (tests passed: %s, failed: %s, ignored: %s)",
                        result.getRunTime(),
                        result.getRunCount(),
                        failed,
                        ignored);
            }
        }

        @Override
        public void testStarted(Description description) {
            logger.log("Test started %s", description.getDisplayName());
        }

        @Override
        public void testFinished(Description description) {
            logger.log("Test finished %s", description.getDisplayName());
        }

        @Override
        public void testFailure(Failure failure) {
            logger.log("Test Failed %s, reason: %s", failure.getDescription().getDisplayName(), failure.getTrace());
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            logger.log("Test ignored with assumption %s", failure.getDescription().getDisplayName());
        }

        @Override
        public void testIgnored(Description description) {
            logger.log("Test ignored %s", description.getDisplayName());
        }
    }
}
