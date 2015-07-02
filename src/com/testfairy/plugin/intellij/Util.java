package com.testfairy.plugin.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
    @NotNull
    static String getStackTrace(Exception exception) {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }
}
