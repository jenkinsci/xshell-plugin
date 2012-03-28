package hudson.plugins.xshell;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * XShell Builder Plugin.
 *
 * @author Marco Ambu
 */
public final class XShellBuilder extends Builder {
  private static final Pattern WIN_ENV_VAR_REGEX = Pattern.compile("%(\\S+?)%");
  private static final Pattern UNIX_ENV_VAR_REGEX = Pattern.compile("\\$(\\S+)");

  @Extension
  public static final XShellDescriptor DESCRIPTOR = new XShellDescriptor();

  /**
   * Set to true for debugging.
   */
  private static final boolean DEBUG = false;

  /**
   * Command line.
   */
  private final String commandLine;

  /**
   * Specify if command is executed from working dir.
   */
  private final Boolean executeFromWorkingDir;

  public String getCommandLine() {
    return commandLine;
  }

  public Boolean getExecuteFromWorkingDir() {
    return executeFromWorkingDir;
  }

  @DataBoundConstructor
  public XShellBuilder(final String commandLine, final Boolean executeFromWorkingDir) {
    this.commandLine = Util.fixEmptyAndTrim(commandLine);
    this.executeFromWorkingDir = executeFromWorkingDir;
  }

  @Override
  public Descriptor<Builder> getDescriptor() {
    return DESCRIPTOR;
  }

  @Override
  public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
          throws InterruptedException, IOException {

    String match = "[/" + Pattern.quote("\\") + "]";
    String replacement = Matcher.quoteReplacement("\\");

    Pattern words = Pattern.compile("\\S+");
    Pattern urls = Pattern.compile("(https*|ftp):");
    StringBuffer sb = new StringBuffer();
    Matcher m = words.matcher(commandLine);
    while (m.find()) {
      String item = m.group();
      if (!urls.matcher(item).find()) {
        // Not sure if File.separator is right if executing on slave with OS different from master's one
        //String cmdLine = commandLine.replaceAll("[/\\\\]", File.separator);
        m.appendReplacement(sb, Matcher.quoteReplacement(item.replaceAll(match, replacement)));
      }
    }
    m.appendTail(sb);

    String cmdLine = sb.toString();
      
    if (launcher.isUnix()) {
      cmdLine = convertEnvVarsToUnix(cmdLine);
    } else {
      cmdLine = convertEnvVarsToWindows(cmdLine);
    }

    ArgumentListBuilder args = new ArgumentListBuilder();
    if (cmdLine != null) {
      args.addTokenized((launcher.isUnix() && executeFromWorkingDir) ? "./" + cmdLine : cmdLine);
    }

    if (!launcher.isUnix()) {
      args = args.toWindowsCommand();
    }

    EnvVars env = build.getEnvironment(listener);
    env.putAll(build.getBuildVariables());

    if (DEBUG) {
      final PrintStream logger = listener.getLogger();
      for (final Map.Entry<String, String> entry : env.entrySet()) {
          logger.println("(DEBUG) env: key= " + entry.getKey() + " value= " + entry.getValue());
      }
      logger.println("Args: " + args.toStringWithQuote());
      logger.println("Working dir: " + build.getModuleRoot());
    }

    try {
      final int result = launcher.decorateFor(build.getBuiltOn()).launch()
              .cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();
      return result == 0;
    } catch (final IOException e) {
      Util.displayIOException(e, listener);
      final String errorMessage = Messages.XShell_ExecFailed();
      e.printStackTrace(listener.fatalError(errorMessage));
      return false;
    }
  }

  /**
   * Convert Windows-style environment variables to UNIX-style.
   * E.g. "script --opt=%OPT%" to "script --opt=$OPT"
   *
   * @param cmdLine The command line with Windows-style env vars to convert.
   * @return The command line with UNIX-style env vars.
   */
  private String convertEnvVarsToUnix(String cmdLine) {
    if (cmdLine == null) {
      return null;
    }

    StringBuffer sb = new StringBuffer();

    Matcher m = WIN_ENV_VAR_REGEX.matcher(cmdLine);
    while (m.find()) {
      m.appendReplacement(sb, "\\$$1");
    }
    m.appendTail(sb);

    return sb.toString();
  }

  /**
   * Convert UNIX-style environment variables to Windows-style.
   * E.g. "script --opt=$OPT" to "script --opt=%OPT%"
   *
   * @param cmdLine The command line with Windows-style env vars to convert.
   * @return The command line with UNIX-style env vars.
   */
  private String convertEnvVarsToWindows(String cmdLine) {
    if (cmdLine == null) {
      return null;
    }

    StringBuffer sb = new StringBuffer();

    Matcher m = UNIX_ENV_VAR_REGEX.matcher(cmdLine);
    while (m.find()) {
      m.appendReplacement(sb, "%$1%");
    }
    m.appendTail(sb);

    return sb.toString();
  }
}
