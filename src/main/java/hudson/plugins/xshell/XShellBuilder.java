package hudson.plugins.xshell;

import hudson.*;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.StreamBuildListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * XShell Builder Plugin.
 *
 * @author Marco Ambu
 */
public final class XShellBuilder extends Builder {

  private static final Logger LOG = Logger.getLogger(XShellBuilder.class.getName());
  private static final Pattern WIN_ENV_VAR_REGEX = Pattern.compile("%([a-zA-Z0-9_]+)%");
  private static final Pattern UNIX_ENV_VAR_REGEX = Pattern.compile("\\$([a-zA-Z0-9_]+)");
  public static final String UNIX_SEP = "/";
  public static final String WINDOWS_SEP = "\\";

  @Extension
  public static final XShellDescriptor DESCRIPTOR = new XShellDescriptor();

  /**
   * Command line.
   */
  private final String commandLine;

  /**
   * Specify if command is executed from working dir.
   */
  private final Boolean executeFromWorkingDir;

  /**
    * if output matches this then kill the step.
    */
  private final String regexToKill;

  /**
    * amount of time to allocate to the step
    */
  private final String timeAllocated;


  public String getCommandLine() {
    return commandLine;
  }

  public Boolean getExecuteFromWorkingDir() {
    return executeFromWorkingDir;
  }

  public String getRegexToKill() {
     return regexToKill;
  }

  public String getTimeAllocated() {
     return timeAllocated;
  }

    @DataBoundConstructor
  public XShellBuilder(final String commandLine, final Boolean executeFromWorkingDir, final String regexToKill, final String timeAllocated) {
    this.commandLine = Util.fixEmptyAndTrim(commandLine);
      this.executeFromWorkingDir = executeFromWorkingDir;
      this.regexToKill = regexToKill == null ? "" : regexToKill;
      this.timeAllocated = timeAllocated;
  }

  @Override
  public Descriptor<Builder> getDescriptor() {
    return DESCRIPTOR;
  }

  @Override
  public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
          throws InterruptedException, IOException {

      LOG.log(Level.FINE, "Unmodified command line: " + commandLine);
      LOG.log(Level.FINE, "Regex to kill: " + regexToKill);
      LOG.log(Level.FINE, "Time allocated before kill: " + timeAllocated);

    String cmdLine = convertSeparator(commandLine, (launcher.isUnix() ? UNIX_SEP : WINDOWS_SEP));
    LOG.log(Level.FINE, "File separators sanitized: " + cmdLine);
      
    if (launcher.isUnix()) {
      cmdLine = convertEnvVarsToUnix(cmdLine);
    } else {
      cmdLine = convertEnvVarsToWindows(cmdLine);
    }
    LOG.log(Level.FINE, "Environment variables sanitized: " + cmdLine);

    ArgumentListBuilder args = new ArgumentListBuilder();
    if (cmdLine != null) {
      args.addTokenized((launcher.isUnix() && executeFromWorkingDir) ? "./" + cmdLine : cmdLine);
      LOG.log(Level.FINE, "Execute from working directory: " + args.toStringWithQuote());
    }

    if (!launcher.isUnix()) {
      args = args.toWindowsCommand();
      LOG.log(Level.FINE, "Windows command: " + args.toStringWithQuote());
    }

    EnvVars env = build.getEnvironment(listener);
    env.putAll(build.getBuildVariables());

    LOG.log(Level.FINEST, "Environment variables: " + env.entrySet().toString());
    LOG.log(Level.FINE, "Command line: " + args.toStringWithQuote());
    LOG.log(Level.FINE, "Working directory: " + build.getWorkspace());

    Pattern r = Pattern.compile(this.regexToKill == null ? "" : this.regexToKill);
    Long timeAllowed;

    try{
        timeAllowed = Long.parseLong(this.timeAllocated);
    }catch(Exception e){
        timeAllowed = (long) 0;
    }

    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamBuildListener sbl = new StreamBuildListener(baos);

       final Proc child =     launcher.decorateFor(build.getBuiltOn()).launch()
              .cmds(args).envs(env).stdout(sbl).pwd(build.getWorkspace()).start();

       Long startTime = System.currentTimeMillis();

      while(child.isAlive()){

        baos.flush();
        String s = baos.toString();
        baos.reset();

        listener.getLogger().print(s);
        listener.getLogger().flush();

        if ((this.regexToKill != null) && (this.regexToKill.length() > 0) && (r.matcher(s).find())){
            LOG.log(Level.FINEST, "Matched failure in log");
            child.kill();
            listener.getLogger().println("Matched <" + this.regexToKill +"> in output. Terminated");
        }else if( (timeAllowed > 0) && ((System.currentTimeMillis() - startTime) / 1000) > timeAllowed){
            LOG.log(Level.FINEST, "Timed out");
            child.kill();
            listener.getLogger().println("Timed out <" + this.timeAllocated +">. Terminated");
        }else{
          Thread.sleep(2);
        }
      }

      baos.flush();
      listener.getLogger().print(baos.toString());
      listener.getLogger().flush();
      return child.join() == 0;
    } catch (final IOException e) {
      Util.displayIOException(e, listener);
      final String errorMessage = Messages.XShell_ExecFailed();
      e.printStackTrace(listener.fatalError(errorMessage));
      return false;
    }
  }
  
  public static String convertSeparator(String cmdLine, String newSeparator) {
    String match = "[/" + Pattern.quote("\\") + "]";
    String replacement = Matcher.quoteReplacement(newSeparator);

    Pattern words = Pattern.compile("\\S+");
    Pattern urls = Pattern.compile("(https*|ftp|git):");
    StringBuffer sb = new StringBuffer();
    Matcher m = words.matcher(cmdLine);
    while (m.find()) {
      String item = m.group();
      if (!urls.matcher(item).find()) {
        // Not sure if File.separator is right if executing on slave with OS different from master's one
        //String cmdLine = commandLine.replaceAll("[/\\\\]", File.separator);
        m.appendReplacement(sb, Matcher.quoteReplacement(item.replaceAll(match, replacement)));
      }
    }
    m.appendTail(sb);

    return sb.toString();
  }

  /**
   * Convert Windows-style environment variables to UNIX-style.
   * E.g. "script --opt=%OPT%" to "script --opt=$OPT"
   *
   * @param cmdLine The command line with Windows-style env vars to convert.
   * @return The command line with UNIX-style env vars.
   */
  public static String convertEnvVarsToUnix(String cmdLine) {
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
  public static String convertEnvVarsToWindows(String cmdLine) {
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
