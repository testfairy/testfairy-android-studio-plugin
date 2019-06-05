package com.testfairy.plugin.intellij;

import org.gradle.tooling.BuildLauncher;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

	public static <T extends Object> T setStandardOutputOfBuildLauncher(BuildLauncher buildLauncher, OutputStream outputStream) {
		try {
			Method setStandardOutput = getMethodWithName(buildLauncher.getClass(), "setStandardOutput");
			return (T) setStandardOutput.invoke(buildLauncher, outputStream);
		} catch (IllegalAccessException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (IllegalArgumentException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (InvocationTargetException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		}
	}

	public static <T extends Object> T setStandardErrorOfBuildLauncher(BuildLauncher buildLauncher, OutputStream outputStream) {
		try {
			Method setStandardError = getMethodWithName(buildLauncher.getClass(), "setStandardError");
			return (T) setStandardError.invoke(buildLauncher, outputStream);
		} catch (IllegalAccessException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (IllegalArgumentException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (InvocationTargetException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		}
	}

	public static void runBuildLauncher(BuildLauncher buildLauncher) {
		Method run = getMethodWithName(buildLauncher.getClass(), "run", 0);
		try {
			run.invoke(buildLauncher);
		} catch (IllegalAccessException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (IllegalArgumentException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (InvocationTargetException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		}
	}

	public static <T extends Object> T withArgumentsBuildLauncher(BuildLauncher buildLauncher, String... strs) {
		Method withArguments = getMethodWithNameVarArgs(buildLauncher.getClass(), "withArguments", String[].class);
		try {
			return (T) withArguments.invoke(buildLauncher, new Object[] { strs });
		} catch (IllegalAccessException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (IllegalArgumentException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		} catch (InvocationTargetException t) {
			throw new RuntimeException("Cannot call BuildLauncher.setStandardError with reflection", t);
		}
	}

	public static Method getMethodWithName(Class<?> klass, String method) {
		return getMethodWithName(klass, method, null);
	}

	public static Method getMethodWithName(Class<?> klass, String method, Integer parameterCount) {
		if (method == null || klass == null) return null;

		Method[] methods = klass.getDeclaredMethods();

		for (Method m : methods) {
			if (m.getName().equals(method)) {
				if (parameterCount != null) {
					if (m.getParameterTypes().length == parameterCount) {
						return m;
					}
				} else {
					return m;
				}
			}
		}

		return null;
	}

	public static Method getMethodWithNameVarArgs(Class<?> klass, String method, Class<?> varArgsClass) {
		if (method == null || klass == null) return null;

		try {
			return klass.getDeclaredMethod(method, varArgsClass);
		} catch (NoSuchMethodException ignore) {
			return null;
		}
	}
}
