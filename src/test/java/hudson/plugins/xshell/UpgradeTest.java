package hudson.plugins.xshell;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import org.junit.Rule;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import hudson.plugins.xshell.XShellBuilder;

/**
 * Test upgrade from one version of XShell to another.
 *
 * @author MarkEWaite
 */
@RunWith(JUnit4.class)
public class UpgradeTest extends HudsonTestCase {
    @Rule public JenkinsRule j = new JenkinsRule();

    /**
     * XShell upgrade from 0.8 to 0.9 reported a null pointer
     * exception when the job was first executed after the upgrade.
     * The null pointer exception was in the constructor for a regular
     * expression.
     *
     * Creating a new job with a null pointer for the regular
     * expression to kill the job shows the same bug, so this test
     * creates and executes a new job with a null regexToKill.
     */
    @Test @Bug(20660)
    public void testXShellBuilderNullAsRegExToKill() throws IOException, InterruptedException, ExecutionException {

      FreeStyleProject project = j.createFreeStyleProject();

      final String arguments = "hello world";

      final boolean execFromWorkingDir = false;
      final String regexToKill = null;
      final String timeAllocated = null;

      project.getBuildersList().add(new XShellBuilder("echo " + arguments, execFromWorkingDir, regexToKill, timeAllocated));

      FreeStyleBuild build = project.scheduleBuild2(0).get();
      String s = FileUtils.readFileToString(build.getLogFile());

      assertThat(s, not(containsString("java.lang.NullPointerException")));
      assertThat(s, containsString(arguments));
      assertThat(s, containsString("SUCCESS"));
   }
}
