package hudson.plugins.xshell;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for XShell.
 *
 * @author Marco Ambu
 */
public final class XShellDescriptor extends BuildStepDescriptor<Builder> {

    public XShellDescriptor() {
        super(XShellBuilder.class);
        load();
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        save();
        return true;
    }

    @Override
    public String getHelpFile() {
        return "/plugin/xshell/help.html";
    }

    @Override
    public String getDisplayName() {
        return Messages.XShell_DisplayName();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public XShellBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
        return req.bindJSON(XShellBuilder.class, formData);
    }
}
