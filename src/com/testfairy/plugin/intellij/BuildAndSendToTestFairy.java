package com.testfairy.plugin.intellij;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ArrayUtil;
import com.testfairy.plugin.intellij.exception.AndroidModuleBuildFileNotFoundException;
import com.testfairy.plugin.intellij.exception.TestFairyException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.InterruptedException;

public class BuildAndSendToTestFairy extends AnAction {

    public final static String PASSWORD_KEY = "TestFairy api key";
    private Project project;
    private TestFairyConfig testFairyConfig;
    private File fileToPatch;
    private BuildFilePatcher buildFilePatcher;

    List<String> testFairyTasks;
    private ConfigureTestFairy configureTestFairyAction;

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.project = e.getProject();
        Plugin.setProject(project);

        activateToolWindows();

        ToolWindowManager.getInstance(project).getToolWindow("TestFairy").activate(new Runnable() {
            @Override
            public void run() {
                try {

                    TestFairyConsole.clear();

                    configureTestFairyAction = new ConfigureTestFairy();

                    if (!configureTestFairyAction.isConfigured(project)) {
                        configureTestFairyAction.execute(project);
                    }

                    if (!configureTestFairyAction.isConfigured(project)) {
                        Plugin.broadcastError("TestFairy is not configured for this project.");
                        return;
                    }

                    testFairyConfig = configureTestFairyAction.getConfig();

                    execute(project);
                } catch (AndroidModuleBuildFileNotFoundException e1) {
                    Plugin.broadcastError(e1.getMessage());
                } catch (Exception exception) {
                    Plugin.logException(exception);
                }
            }
        });


    }

    private void activateToolWindows() {
        if (ToolWindowManager.getInstance(project).getToolWindow("Messages") != null) {
            ToolWindowManager.getInstance(project).getToolWindow("Messages").activate(null);
        }
        ToolWindowManager.getInstance(project).getToolWindow("Event Log").activate(null);

    }

    private void execute(final Project project) throws AndroidModuleBuildFileNotFoundException {
        this.project = project;
        fileToPatch = configureTestFairyAction.findProjectBuildFile(project);
        buildFilePatcher = new BuildFilePatcher(fileToPatch);


        try {
            if (!isTestfairyGradlePluginConfigured()) {
                buildFilePatcher.patchBuildFile(testFairyConfig);
            }
        } catch (IOException e) {
            Plugin.logException(e);
        }

        Task.Backgroundable bgTask = new Task.Backgroundable(project, "Uploading to TestFairy", false) {
            public int selection;

            public int getSelection() {
                return this.selection;
            }

            @Override
            public void run(ProgressIndicator indicator) {
                Plugin.setIndicator(indicator);
                indicator.setIndeterminate(true);

                Plugin.logInfo("Preparing Gradle Wrapper");
                testFairyTasks = getTestFairyTasks();
                Plugin.setIndicator(null);
            }

            @Override
            public void onSuccess() {

                if (testFairyTasks.size() == 0) {
                    Plugin.broadcastError("No TestFairy build tasks found.");
                    return;
                }

                selection = Messages.showChooseDialog(
                        "Select a build target for APK", "Build Target", ArrayUtil.toStringArray(testFairyTasks), testFairyTasks.get(0), Icons.TEST_FAIRY_ICON);
                if (selection == -1) {
                    return;
                }

                new Backgroundable(project, "Uploading to TestFairy", false) {
                    @Override
                    public void run(ProgressIndicator indicator) {
                        try {
                            Plugin.setIndicator(indicator);

                            indicator.setIndeterminate(true);

                            String url = packageRelease(testFairyTasks.get(selection));

                            launchBrowser(url);

                            Plugin.logInfo("Done");
                            Thread.sleep(3000);
                            indicator.stop();
                            Plugin.setIndicator(null);

                        } catch (InterruptedException e1) {
                            Plugin.logException(e1);
                        } catch (TestFairyException tfe) {
                            Plugin.broadcastError("Invalid TestFairy API key. Please use Build/TestFairy/Settings to fix.");
                        } catch (URISyntaxException e) {
                            Plugin.logException(e);
                        }
                    }
                }.queue();
            }

        };
        bgTask.queue();


    }

    private List<String> getTestFairyTasks() {
        List<String> tasks = new ArrayList<String>();

        OutputStream outputStream = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            //Netbeans IDE automatically overrides this toString()
            public String toString() {
                return this.string.toString();
            }
        };

        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(getProjectDirectoryFile())
                .connect();

        BuildLauncher buildLauncher = connection.newBuild();
        buildLauncher.forTasks(":tasks");

        buildLauncher.setStandardOutput(outputStream);
        buildLauncher.run();

        for (String line : outputStream.toString().split("\\r?\\n")) {
            if (line.startsWith("testfairy")) {
                tasks.add(line.split(" ")[0]);
            }
        }

        return tasks;
    }

    private String packageRelease(String task) throws TestFairyException {
        String result = "";
        OutputStream outputStream;
        try {
            outputStream = new OutputStream() {
                private StringBuilder string = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    char[] s = {(char) b};
                    this.string.append((char) b);
                    TestFairyConsole.consoleView.print(new String(s), ConsoleViewContentType.SYSTEM_OUTPUT);
                }

                //Netbeans IDE automatically overrides this toString()
                public String toString() {
                    return this.string.toString();
                }
            };

            ProjectConnection connection;
            connection = GradleConnector.newConnector()
                    .forProjectDirectory(getProjectDirectoryFile())
                    .connect();

            BuildLauncher build = connection.newBuild();
            build.forTasks(task);

            build.setStandardOutput(outputStream);
            build.setStandardError(outputStream);
            try {
                build.run();
            } catch (GradleConnectionException gce) {
                if (checkInvalidAPIKey(gce)) {
                    throw new TestFairyException("Invalid API key. Please use Build/TestFairy/Settings to fix.");
                }
            } catch (IllegalStateException ise) {
                throw new TestFairyException(ise.getMessage());

            }

            connection.close();

            String lines[] = outputStream.toString().split("\\r?\\n");
            int i = lines.length;
            while (--i >= 0) {
                if (lines[i].startsWith("https://app.testfairy.com")) {
                    result = lines[i];
                    break;
                }
            }
            if (result.length() == 0) {
                Plugin.logError("TestFairy project URL not found in build output");
            }

            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            Plugin.logException(e1);
        }
        return result;
    }

    private boolean checkInvalidAPIKey(GradleConnectionException gce) {

        String stackTrace = Util.getStackTrace(gce);

        if (stackTrace.contains("Invalid API key")) {
            return true;
        }

        return false;

    }

    private File getProjectDirectoryFile() {
        return new File(project.getBasePath());
    }

    private boolean isTestfairyGradlePluginConfigured() throws IOException {
        String fileContents = new Scanner(fileToPatch).useDelimiter("\\Z").next();
        return fileContents.contains("com.testfairy.plugins.gradle:testfairy");
    }

    private void launchBrowser(String url) throws InterruptedException, URISyntaxException {
        if (url.length() < 5) return;
        Plugin.logInfo("Launching Browser: " + url);
        BrowserLauncher.getInstance().browse(new URI(url));
        Thread.sleep(3000);
    }
}
