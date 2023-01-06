package hudson.plugins.xshell;

import static org.junit.Assert.*;

import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author marco.ambu
 */
public class XShellTest {
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testConvertSeparatorUnixToWin() throws Exception {
        String s = XShellBuilder.convertSeparator("mycmd a/b", XShellBuilder.WINDOWS_SEP);
        assertEquals("mycmd a\\b", s);
    }

    @Test
    public void testConvertSeparatorWinToUnix() throws Exception {
        String s = XShellBuilder.convertSeparator("mycmd a\\b", XShellBuilder.UNIX_SEP);
        assertEquals("mycmd a/b", s);
    }

    @Test
    public void testEnvVarUnixToWin() throws Exception {
        String s = XShellBuilder.convertEnvVarsToWindows("mycmd $VAR");
        assertEquals("mycmd %VAR%", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd $VAR/one_dir");
        assertEquals("mycmd %VAR%/one_dir", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd $WORKSPACE/$JOB_NAME");
        assertEquals("mycmd %WORKSPACE%/%JOB_NAME%", s);
    }

    @Test
    public void testEnvVarUnixToUnix() throws Exception {
        String s = XShellBuilder.convertEnvVarsToUnix("mycmd $VAR");
        assertEquals("mycmd $VAR", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd $VAR/one_dir");
        assertEquals("mycmd $VAR/one_dir", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd $WORKSPACE/$JOB_NAME");
        assertEquals("mycmd $WORKSPACE/$JOB_NAME", s);
    }

    @Test
    public void testEnvVarWinToUnix() throws Exception {
        String s = XShellBuilder.convertEnvVarsToUnix("mycmd %VAR%");
        assertEquals("mycmd $VAR", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd %VAR%\\one_dir");
        assertEquals("mycmd $VAR\\one_dir", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd %WORKSPACE%\\%JOB_NAME%");
        assertEquals("mycmd $WORKSPACE\\$JOB_NAME", s);
    }

    @Test
    public void testEnvVarWinToWin() throws Exception {
        String s = XShellBuilder.convertEnvVarsToWindows("mycmd %VAR%");
        assertEquals("mycmd %VAR%", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd %VAR%\\one_dir");
        assertEquals("mycmd %VAR%\\one_dir", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd %WORKSPACE%\\%JOB_NAME%");
        assertEquals("mycmd %WORKSPACE%\\%JOB_NAME%", s);
    }
}
