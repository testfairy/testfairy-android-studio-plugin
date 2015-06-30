package com.testfairy.plugin.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    public static ConsoleView consoleView;
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "", true);
        toolWindow.getContentManager().addContent(content);
    }
}
