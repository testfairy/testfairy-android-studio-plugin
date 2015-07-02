package com.testfairy.plugin.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public class Plugin implements ApplicationComponent {
    private static ProgressIndicator indicator;
    private static Project project;

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "TestFairyAndroidStudioMenu";
    }

    public static void setIndicator(ProgressIndicator indicator) {
        Plugin.indicator = indicator;
    }
    public static void setProject(Project project) {
        Plugin.project = project;
    }

    public static void broadcastInfo(String s){
        logInfo(s);
        Notifications.Bus.notify(new Notification("TestFairyGroup", "TestFairy", s, NotificationType.INFORMATION), project);
    }

    public static void logInfo(String s){
        if(indicator != null) {
            indicator.setText(s);
        }
        TestFairyConsole.info(s);
    }

    public static void broadcastError(String s){
        Notifications.Bus.notify(new Notification("TestFairyGroup", "TestFairy", s, NotificationType.ERROR), project);
        TestFairyConsole.error(s);
    }

    public static void logError(String s){
        TestFairyConsole.error(s);
    }

    public static void logException(Exception exception){
        TestFairyConsole.error(Util.getStackTrace(exception));
    }
}
