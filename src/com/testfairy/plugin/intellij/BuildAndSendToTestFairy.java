package com.testfairy.plugin.intellij;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.kohsuke.rngom.ast.builder.BuildException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.InterruptedException;
import java.lang.Exception;

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

    private void execute(final Project project) {
        this.project = project;
        fileToPatch = project.getBasePath() + "/app/build.gradle";
        buildFilePatcher = new BuildFilePatcher(fileToPatch);


        try {
            if (!isTestfairyGradlePluginConfigured()) {
                buildFilePatcher.patchBuildFile(testFairyConfig);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<String> testFairyTasks = getTestFairyTasks();
        final int selection = Messages.showChooseDialog(
                "Select a build target for APK", "Build Target", ArrayUtil.toStringArray(testFairyTasks), testFairyTasks.get(0), Icons.TEST_FAIRY_ICON);

        new Task.Backgroundable(project, "Building&Uploading to Test Fairy", false) {
            @Override
            public void run(ProgressIndicator indicator) {
                try {

                    indicator.setIndeterminate(true);

                    String url = packageRelease(testFairyTasks.get(selection));

                    indicator.setText("Launching Browser");
                    launchBrowser(url);

                    indicator.setText("Success");
                    Thread.sleep(3000);
                    indicator.stop();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }.queue();
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

    private String packageRelease(String task) throws BuildException {
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
                    .forProjectDirectory(getProjectDirectoryFile())
                    .connect();

            BuildLauncher build = connection.newBuild();
            build.forTasks(task);

            build.setStandardOutput(outputStream);
            build.run();

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
                System.err.println("WARNING: api URL not found in testfairy build output");
            }

            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    private File getProjectDirectoryFile() {
        return new File(project.getBasePath());
    }

    private boolean isTestfairyGradlePluginConfigured() throws IOException {
        String fileContents = new Scanner(new File(fileToPatch)).useDelimiter("\\Z").next();
        return fileContents.contains("com.testfairy.plugins.gradle:testfairy");
    }

    private void launchBrowser(String url) {
        if (url.length() < 5) return;
        try {
            BrowserLauncher.getInstance().browse(new URI(url));
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
