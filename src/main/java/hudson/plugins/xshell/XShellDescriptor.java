package hudson.plugins.xshell;

import hudson.model.Descriptor;
import hudson.tasks.Builder;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for XShell.
 * 
 * @author Marco Ambu
 */
public final class XShellDescriptor extends Descriptor<Builder> {

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
    public XShellBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
        return req.bindJSON(XShellBuilder.class, formData);
    }

}
