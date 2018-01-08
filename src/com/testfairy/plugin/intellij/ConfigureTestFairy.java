package com.testfairy.plugin.intellij;

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

	@Override
	public void actionPerformed(AnActionEvent e) {
		try {
			this.project = e.getProject();
			Plugin.setProject(project);

			this.execute(project);
		} catch (AndroidModuleBuildFileNotFoundException e1) {
			Plugin.broadcastError(e1.getMessage());
		} catch (Exception exception) {
			Plugin.logException(exception);
		}
	}

	public void execute(Project project) throws IOException, AndroidModuleBuildFileNotFoundException {
		this.project = project;
		configure();
	}

	private void configure() throws IOException, AndroidModuleBuildFileNotFoundException {

		this.apiKey = Messages.showInputDialog(project, "Enter your TestFairy API key", "Config", Icons.TEST_FAIRY_ICON);

		if (this.apiKey != null && this.apiKey.length() > 0) {
			File androidBuildFile = findProjectBuildFile(project);
			new BuildFilePatcher(androidBuildFile).patchBuildFile(apiKey);
			Plugin.broadcastInfo("TestFairy Uploader section was added to " + androidBuildFile + " successfully.");

		} else {
			Plugin.broadcastInfo("TestFairy Uploader can't work without an API Key.");
		}
	}

	/**
	 * Read gradle.settings and find first android application module.
	 * That module's gradle.build file will be patched with TF gradle plugin stuff.
	 *
	 * @param project
	 * @return File gradle.build file to be patched
	 * @throws AndroidModuleBuildFileNotFoundException
	 */
	@NotNull
	public File findProjectBuildFile(Project project) throws AndroidModuleBuildFileNotFoundException {

		String moduleName = "app";

		File settingsFile = new File(project.getBasePath() + "/settings.gradle");
		if (!settingsFile.exists()) {
			throw new AndroidModuleBuildFileNotFoundException("Could not locate build.gradle used for Android project.");
		}

		String moduleLines[] = Util.readFileLines(settingsFile);

		int i = 0;
		while (i < moduleLines.length && moduleLines[i].trim().equals("")) {
			i++;
		}
		Pattern pattern = Pattern.compile("[']\\:([^']*)[']");
		Matcher matcher = pattern.matcher(moduleLines[i]);

		while (matcher.find()) {
			moduleName = matcher.group(1);
			if (isAppModule(project.getBasePath(), moduleName)) {
				break;
			}
		}

		String buildFilePath = getModuleBuildFilePath(project.getBasePath(), moduleName);
		File f = new File(buildFilePath);
		if (!f.exists()) {
			throw new AndroidModuleBuildFileNotFoundException("Could not locate build.gradle used for Android project.");
		}

		return f;
	}

	private boolean isAppModule(String basePath, String moduleName) {
		File gradleBuildFile = new File(getModuleBuildFilePath(basePath, moduleName));

		String fileLines[] = Util.readFileLines(gradleBuildFile);
		for (String line : fileLines) {
			if (line.contains("plugin") && line.contains("android")) {
				return true;
			}
		}

		return false;
	}

	private String getModuleBuildFilePath(String basePath, String moduleName) {
		return basePath + "/" + moduleName + "/build.gradle";
	}

	public boolean isBuildFilePatched(Project project) throws AndroidModuleBuildFileNotFoundException {

		File androidBuildFile = findProjectBuildFile(project);
		return BuildFilePatcher.isTestfairyGradlePluginConfigured(androidBuildFile);
	}
}
