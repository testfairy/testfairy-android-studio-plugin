package com.testfairy.plugin.intellij;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.rngom.ast.builder.BuildException;

import java.io.*;
import java.net.URI;
import java.util.Scanner;

public class BuildAndSendToTestFairy extends AnAction {

    public final static String PASSWORD_KEY = "test fairy api key";
    private Project project;
    private TestFairyConfig testFairyConfig;
    private String fileToPatch;
    private BuildFilePatcher buildFilePatcher;

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.project = e.getProject();

        ConfigureTestFairy configureTestFairyAction = new ConfigureTestFairy();

        if (!configureTestFairyAction.isConfigured()) {
            configureTestFairyAction.execute(e.getProject());
        }

        testFairyConfig = configureTestFairyAction.getConfig();

        execute(e.getProject());
    }

    private void execute(Project project) {
        this.project = project;
        fileToPatch = project.getBasePath() + "/app/build.gradle";
        buildFilePatcher = new BuildFilePatcher(fileToPatch);
        new Task.Backgroundable(project, "Building&Uploading to Test Fairy", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {

                    indicator.setIndeterminate(true);

                    if (!isTestfairyGradlePluginConfigured()) {
                        buildFilePatcher.patchBuildFile(testFairyConfig);
                    }

                    String url = packageRelease();

                    indicator.setText("Launching Browser");
                    launchBrowser(url);

                    indicator.setText("Success");
                    Thread.sleep(3000);
                    indicator.stop();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }.queue();
    }

    private String packageRelease() throws BuildException {
        String result = "";
        try {
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

            ProjectConnection connection;
            connection = GradleConnector.newConnector()
                    .forProjectDirectory(new File(project.getBasePath()))
                    .connect();

            BuildLauncher build = connection.newBuild();
            build.forTasks("testfairyRelease");


            build.setStandardOutput(outputStream);
            build.run();

            connection.close();
            String lines[] = outputStream.toString().split("\\r?\\n");
            int i = lines.length;
            while(--i >= 0) {
                if(lines[i].startsWith("https://app.testfairy.com")){
                    result = lines[i];
                    break;
                }
            }

            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.err.println("WARNING: api URL not found in testfariy build output");
        return result;
    }

    private boolean isTestfairyGradlePluginConfigured() throws IOException {
        String fileContents = new Scanner(new File(fileToPatch)).useDelimiter("\\Z").next();
        return fileContents.contains("com.testfairy.plugins.gradle:testfairy");
    }

    private void launchBrowser(String url) {
        if(url.length() < 5) return;
        try {
            BrowserLauncher.getInstance().browse(new URI(url));
            Thread.sleep(3000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


}
