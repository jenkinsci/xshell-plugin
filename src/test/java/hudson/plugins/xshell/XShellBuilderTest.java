package hudson.plugins.xshell;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Test the XShellBuilder class. Avoid null pointer exceptions and more.
 *
 * @author Mark Waite
 */
public class XShellBuilderTest {

    @ClassRule
    public static JenkinsRule rule = new JenkinsRule();

    @Rule
    public TestName testName = new TestName();

    public XShellBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    private String commandLine;
    private String workingDir;
    private boolean executeFromWorkingDir;
    private String regexToKill;
    private String timeAllocated;
    private XShellBuilder builder;

    @Before
    public void setUp() {
        commandLine = "echo Hello \\n; echo from " + testName.getMethodName();
        workingDir = ".";
        executeFromWorkingDir = false;
        regexToKill = null;
        timeAllocated = null;
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetCommandLine() {
        assertThat(builder.getCommandLine(), is(commandLine));
    }

    @Test
    public void testGetWorkingDir() {
        assertThat(builder.getWorkingDir(), is(workingDir));
    }

    @Test
    public void testGetWorkingDirNull() {
        workingDir = null;
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
        assertThat(builder.getWorkingDir(), is(workingDir));
    }

    @Test
    public void testGetExecuteFromWorkingDir() {
        assertThat(builder.getExecuteFromWorkingDir(), is(executeFromWorkingDir));
    }

    @Test
    public void testGetRegexToKill() {
        assertThat(builder.getRegexToKill(), is("")); // Null is converted to ""
    }

    @Test
    public void testGetTimeAllocated() {
        assertThat(builder.getTimeAllocated(), is(timeAllocated));
    }

    @Test
    public void testPerform() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("job-" + testName.getMethodName());
        Launcher launcher = rule.createLocalLauncher();
        OutputStream outputStream = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(outputStream, Charset.forName("UTF-8"));
        AbstractBuild build = project.scheduleBuild2(0).get();
        assertTrue("Failed in perform", builder.perform(build, launcher, listener));
    }

    @Test
    public void testPerformNullWorkingDir() throws Exception {
        FreeStyleProject project = rule.createFreeStyleProject("job-" + testName.getMethodName());
        Launcher launcher = rule.createLocalLauncher();
        OutputStream outputStream = new ByteArrayOutputStream();
        BuildListener listener = new StreamBuildListener(outputStream, Charset.forName("UTF-8"));
        AbstractBuild build = project.scheduleBuild2(0).get();
        workingDir = null;
        builder = new XShellBuilder(commandLine, workingDir, executeFromWorkingDir, regexToKill, timeAllocated);
        assertTrue("Failed in perform", builder.perform(build, launcher, listener));
    }

    @Test
    public void testConvertSeparator() {
        String newSeparator = "/";
        assertThat(XShellBuilder.convertSeparator(commandLine, newSeparator),
                is(commandLine.replaceAll("[\\\\]", newSeparator)));
    }

    private final String WINDOWS_ENV_VAR = "%PATH%";
    private final String UNIX_ENV_VAR = "$PATH";
    private final String UNIX_ENV_VAR_BRACED = "${PATH}";

    @Test
    public void testConvertEnvVarsToUnix() {
        commandLine = "set PATH=C:\\;";
        assertThat(XShellBuilder.convertEnvVarsToUnix(commandLine + WINDOWS_ENV_VAR),
                is(commandLine + UNIX_ENV_VAR));
    }

    @Test
    public void testConvertEnvVarsToWindows() {
        commandLine = "PATH=/opt/jdk17/bin:";
        assertThat(XShellBuilder.convertEnvVarsToWindows(commandLine + UNIX_ENV_VAR),
                is("PATH=/opt/jdk17/bin:" + WINDOWS_ENV_VAR));
    }

    @Test
    public void testConvertEnvVarsToWindowsBraced() {
        // Note that there is no conversion of braced env vars
        commandLine = "PATH=/opt/jdk17/bin:";
        assertThat(XShellBuilder.convertEnvVarsToWindows(commandLine + UNIX_ENV_VAR_BRACED),
                is("PATH=/opt/jdk17/bin:" + UNIX_ENV_VAR_BRACED));
    }
}
