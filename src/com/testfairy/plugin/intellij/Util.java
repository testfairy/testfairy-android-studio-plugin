package com.testfairy.plugin.intellij;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

public class Util {
	@NotNull
	static String getStackTrace(Throwable exception) {
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		return writer.getBuffer().toString();
	}

	public static String[] readFileLines(File fileToPatch) {
		String fileContents = null;
		try {
			fileContents = new Scanner(fileToPatch).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			Plugin.logException(e);
		}
		return fileContents.split("\\r?\\n");
	}
}
