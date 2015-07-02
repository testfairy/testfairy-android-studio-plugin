package com.testfairy.plugin.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;

public class TestFairyConsole implements com.intellij.openapi.wm.ToolWindowFactory {
    public static ConsoleView consoleView;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "", true);
        toolWindow.getContentManager().addContent(content);
    }

    public static void info(String s) {
        if (consoleView == null) {
            return;
        }
        consoleView.print(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void error(String s) {
        if (consoleView == null) {
            return;
        }
        consoleView.print(s + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    public static void clear() {
        if (consoleView == null) {
            return;
        }
        consoleView.clear();
    }

}
