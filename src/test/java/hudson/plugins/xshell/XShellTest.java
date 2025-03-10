package hudson.plugins.xshell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * @author marco.ambu
 */
@WithJenkins
class XShellTest {

    @Test
    void testConvertSeparatorUnixToWin(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertSeparator("mycmd a/b", XShellBuilder.WINDOWS_SEP);
        assertEquals("mycmd a\\b", s);
    }

    @Test
    void testConvertSeparatorWinToUnix(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertSeparator("mycmd a\\b", XShellBuilder.UNIX_SEP);
        assertEquals("mycmd a/b", s);
    }

    @Test
    void testEnvVarUnixToWin(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertEnvVarsToWindows("mycmd $VAR");
        assertEquals("mycmd %VAR%", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd $VAR/one_dir");
        assertEquals("mycmd %VAR%/one_dir", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd $WORKSPACE/$JOB_NAME");
        assertEquals("mycmd %WORKSPACE%/%JOB_NAME%", s);
    }

    @Test
    void testEnvVarUnixToUnix(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertEnvVarsToUnix("mycmd $VAR");
        assertEquals("mycmd $VAR", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd $VAR/one_dir");
        assertEquals("mycmd $VAR/one_dir", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd $WORKSPACE/$JOB_NAME");
        assertEquals("mycmd $WORKSPACE/$JOB_NAME", s);
    }

    @Test
    void testEnvVarWinToUnix(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertEnvVarsToUnix("mycmd %VAR%");
        assertEquals("mycmd $VAR", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd %VAR%\\one_dir");
        assertEquals("mycmd $VAR\\one_dir", s);

        s = XShellBuilder.convertEnvVarsToUnix("mycmd %WORKSPACE%\\%JOB_NAME%");
        assertEquals("mycmd $WORKSPACE\\$JOB_NAME", s);
    }

    @Test
    void testEnvVarWinToWin(JenkinsRule jenkinsRule) {
        String s = XShellBuilder.convertEnvVarsToWindows("mycmd %VAR%");
        assertEquals("mycmd %VAR%", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd %VAR%\\one_dir");
        assertEquals("mycmd %VAR%\\one_dir", s);

        s = XShellBuilder.convertEnvVarsToWindows("mycmd %WORKSPACE%\\%JOB_NAME%");
        assertEquals("mycmd %WORKSPACE%\\%JOB_NAME%", s);
    }
}
