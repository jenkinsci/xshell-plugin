package hudson.plugins.xshell;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test upgrade from one version of XShell to another.
 *
 * @author MarkEWaite
 */
@WithJenkins
class UpgradeTest {

    /**
     * XShell upgrade from 0.8 to 0.9 reported a null pointer exception when the job was first
     * executed after the upgrade. The null pointer exception was in the constructor for a regular
     * expression.
     *
     * <p>Creating a new job with a null pointer for the regular expression to kill the job shows
     * the same bug, so this test creates and executes a new job with a null regexToKill.
     */
    @Test
    @Issue("JENKINS-20660")
    void testXShellBuilderNullAsRegExToKill(JenkinsRule j) throws Exception {

        FreeStyleProject project = j.createFreeStyleProject();

        final String arguments = "hello world";

        final boolean execFromWorkingDir = false;
        final String regexToKill = null;
        final String timeAllocated = null;

        project.getBuildersList()
                .add(new XShellBuilder("echo " + arguments, "", execFromWorkingDir, regexToKill, timeAllocated));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        j.assertLogNotContains("java.lang.NullPointerException", build);
        j.assertLogContains(arguments, build);
        j.assertLogContains("SUCCESS", build);
    }
}
