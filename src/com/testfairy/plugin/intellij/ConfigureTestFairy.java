package com.testfairy.plugin.intellij;

import com.intellij.ide.passwordSafe.MasterPasswordUnavailableException;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.ide.passwordSafe.impl.PasswordSafeImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;

public class ConfigureTestFairy extends AnAction {

    private String apiKey;
    private Project project;
    private TestFairyConfig tfe;

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            this.project = e.getProject();
            Plugin.setProject(project);

            this.execute(project);
        }
        catch(Exception exception) {
            Plugin.logException(exception);
        }
    }

    public void execute(Project project) throws IOException {
        this.project = project;
        configure();
    }

    private void persistConfig() {
        PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        try {
            passwordSafe.getMemoryProvider().storePassword(project, this.getClass(), BuildAndSendToTestFairy.PASSWORD_KEY, apiKey);
            passwordSafe.getMasterKeyProvider().storePassword(project, this.getClass(), BuildAndSendToTestFairy.PASSWORD_KEY, apiKey);
            tfe = null; //invalidate configuration
        } catch (MasterPasswordUnavailableException ignored) {
        } catch (PasswordSafeException e) {
            Plugin.broadcastError("Couldn't save API key due to IDE error.");
        }
    }

    private void configure() throws IOException {
        String[] dummy = {};
        this.apiKey = Messages
                .showInputDialog(project, "Enter your TestFairy API key", "Config", Icons.TEST_FAIRY_ICON);
        if (this.apiKey != null && this.apiKey.length() > 0) {
            persistConfig();
            String fileToPatch = project.getBasePath() + "/app/build.gradle";
            (new BuildFilePatcher(fileToPatch)).patchBuildFile(getConfig());
            Plugin.broadcastInfo("API Key Saved.");
        } else {
            Plugin.broadcastInfo("No API key provided.");
        }
    }

    private String getApiKey() {
        PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        try {
            apiKey = passwordSafe.getMemoryProvider().getPassword(project, this.getClass(), BuildAndSendToTestFairy.PASSWORD_KEY);
            if (apiKey == null || apiKey.length() == 0) {
                apiKey = passwordSafe.getMasterKeyProvider().getPassword(project, this.getClass(), BuildAndSendToTestFairy.PASSWORD_KEY);
            }
        } catch (MasterPasswordUnavailableException ignored) {
        } catch (PasswordSafeException e) {
            Plugin.broadcastError("Couldn't save API key due to IDE error.");
        }
        return apiKey;
    }

    public TestFairyConfig getConfig() {
        if (tfe == null) {
            tfe = new TestFairyConfig();
            tfe.apiKey(this.getApiKey());
        }
        return tfe;
    }

    public boolean isConfigured() {
        getApiKey();
        return apiKey != null && apiKey.length() > 0;
    }
}
