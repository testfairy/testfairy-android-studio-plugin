package com.testfairy.plugin.intellij;

import com.intellij.ide.passwordSafe.MasterPasswordUnavailableException;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.ide.passwordSafe.impl.PasswordSafeImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.testfairy.plugin.intellij.exception.AndroidModuleBuildFileNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        catch (AndroidModuleBuildFileNotFoundException e1) {
            Plugin.broadcastError(e1.getMessage());
            Plugin.logException(e1);
        }
        catch(Exception exception) {
            Plugin.logException(exception);
        }
    }

    public void execute(Project project) throws IOException, AndroidModuleBuildFileNotFoundException {
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

    private void configure() throws IOException, AndroidModuleBuildFileNotFoundException {
        String[] dummy = {};
        this.apiKey = Messages
                .showInputDialog(project, "Enter your TestFairy API key", "Config", Icons.TEST_FAIRY_ICON);
        if (this.apiKey != null && this.apiKey.length() > 0) {
            File androidBuildFile = findProjectBuildFile(project);
            persistConfig();
            (new BuildFilePatcher(androidBuildFile)).patchBuildFile(getConfig());
            Plugin.broadcastInfo("API Key Saved.");
        } else {
            Plugin.broadcastInfo("No API key provided.");
        }
    }

    @NotNull
    public File findProjectBuildFile(Project project) throws AndroidModuleBuildFileNotFoundException {

        String moduleName = "app";

        File settingsFile = new File(project.getBasePath() + "/settings.gradle");
        if(!settingsFile.exists()) {
            throw new AndroidModuleBuildFileNotFoundException("Could not locate build.gradle used for Android project.");
        }
        String moduleLines[] = Util.readFileLines(settingsFile);

        Pattern pattern = Pattern.compile("[']\\:(.*)[']");
        Matcher matcher = pattern.matcher(moduleLines[0]);

        while (matcher.find()) {
            moduleName = matcher.group(1);
        }

        String buildFilePath = project.getBasePath() + "/" + moduleName + "/build.gradle";
        File f = new File(buildFilePath);
        if(!f.exists()) {
            throw new AndroidModuleBuildFileNotFoundException("Could not locate build.gradle used for Android project.");
        }
        return f;
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

    public boolean isConfigured(Project project) throws AndroidModuleBuildFileNotFoundException {
        if(!isBuildFilePatched(project)){
            return false;
        }
        getApiKey();
        return apiKey != null && apiKey.length() > 0;
    }

    private boolean isBuildFilePatched(Project project) throws AndroidModuleBuildFileNotFoundException {
        File androidBuildFile = findProjectBuildFile(project);
        return BuildFilePatcher.isTestfairyGradlePluginConfigured(androidBuildFile);
    }
}
