package hudson.plugins.xshell;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test the XShellBuilder class. Avoid null pointer exceptions and more.
 *
 * @author Mark Waite
 */
@WithJenkins
class XShellBuilderTest {

    private static JenkinsRule rule;

    private String commandLine;
    private String workingDir;
    private boolean executeFromWorkingDir;
    private String regexToKill;
    private String timeAllocated;
    private XShellBuilder builder;

    private final Random random = new Random();

    @BeforeAll
    static void setUp(JenkinsRule r) {
        rule = r;
    }

    @BeforeEach
    void setUp(TestInfo info) {
        commandLine = "echo Hello \\n; echo from "
                + info.getTestMethod().orElseThrow().getName();
        workingDir = ".";
        executeFromWorkingDir = false;
        regexToKill = random.nextBoolean() ? null : "xyzzy.*grue";
        timeAllocated = random.nextBoolean() ? null : "37";
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
    }

    @Test
    void testGetCommandLine() {
        assertThat(builder.getCommandLine(), is(commandLine));
    }

    @Test
    void testGetWorkingDir() {
        assertThat(builder.getWorkingDir(), is(workingDir));
    }

    @Test
    void testGetWorkingDirNull() {
        workingDir = null;
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
        assertThat(builder.getWorkingDir(), is(workingDir));
    }

    @Test
    void testGetExecuteFromWorkingDir() {
        assertThat(builder.getExecuteFromWorkingDir(), is(executeFromWorkingDir));
    }

    @Test
    void testGetRegexToKill() {
        String expected = regexToKill == null ? "" : regexToKill;
        assertThat(builder.getRegexToKill(), is(expected));
    }

    @Test
    void testGetTimeAllocated() {
        assertThat(builder.getTimeAllocated(), is(timeAllocated));
    }

    @Test
    void testPerform(TestInfo info) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(
                "job-" + info.getTestMethod().orElseThrow().getName());
        Launcher launcher = rule.createLocalLauncher();
        OutputStream outputStream = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(outputStream, StandardCharsets.UTF_8);
        AbstractBuild build = project.scheduleBuild2(0).get();
        assertTrue(builder.perform(build, launcher, listener), "Failed in perform");
    }

    @Test
    void testPerformNullWorkingDir(TestInfo info) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(
                "job-" + info.getTestMethod().orElseThrow().getName());
        Launcher launcher = rule.createLocalLauncher();
        OutputStream outputStream = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(outputStream, StandardCharsets.UTF_8);
        AbstractBuild build = project.scheduleBuild2(0).get();
        workingDir = null;
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
        assertTrue(builder.perform(build, launcher, listener), "Failed in perform");
    }

    @Test
    void testPerformAbsoluteWorkingDir(TestInfo info) throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject(
                "job-" + info.getTestMethod().orElseThrow().getName());
        Launcher launcher = rule.createLocalLauncher();
        OutputStream outputStream = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(outputStream, StandardCharsets.UTF_8);
        AbstractBuild build = project.scheduleBuild2(0).get();
        workingDir = System.getProperty("java.io.tmpdir");
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
        assertTrue(builder.perform(build, launcher, listener), "Failed in perform");
    }

    @Test
    void testConvertSeparator() {
        String newSeparator = "/";
        assertThat(
                XShellBuilder.convertSeparator(commandLine, newSeparator),
                is(commandLine.replaceAll("[\\\\]", newSeparator)));
    }

    private final String WINDOWS_ENV_VAR = "%PATH%";
    private final String UNIX_ENV_VAR = "$PATH";
    private final String UNIX_ENV_VAR_BRACED = "${PATH}";

    @Test
    void testConvertEnvVarsToUnix() {
        commandLine = "set PATH=C:\\;";
        assertThat(XShellBuilder.convertEnvVarsToUnix(commandLine + WINDOWS_ENV_VAR), is(commandLine + UNIX_ENV_VAR));
    }

    @Test
    void testConvertEnvVarsToUnixNullArgument() {
        assertThat(XShellBuilder.convertEnvVarsToUnix(null), is(nullValue()));
    }

    @Test
    void testConvertEnvVarsToWindows() {
        commandLine = "PATH=/opt/jdk17/bin:";
        assertThat(
                XShellBuilder.convertEnvVarsToWindows(commandLine + UNIX_ENV_VAR),
                is("PATH=/opt/jdk17/bin:" + WINDOWS_ENV_VAR));
    }

    @Test
    void testConvertEnvVarsToWindowsNullArgument() {
        assertThat(XShellBuilder.convertEnvVarsToWindows(null), is(nullValue()));
    }

    @Test
    void testConvertEnvVarsToWindowsBraced() {
        // Note that there is no conversion of braced env vars
        commandLine = "PATH=/opt/jdk17/bin:";
        assertThat(
                XShellBuilder.convertEnvVarsToWindows(commandLine + UNIX_ENV_VAR_BRACED),
                is("PATH=/opt/jdk17/bin:" + UNIX_ENV_VAR_BRACED));
    }
}
