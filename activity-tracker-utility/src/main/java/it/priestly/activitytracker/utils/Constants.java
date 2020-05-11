package it.priestly.activitytracker.utils;

import java.io.File;
import java.lang.management.ManagementFactory;

public abstract class Constants {
	public final static boolean debugMode;
	public final static String classPath;
	public final static String executableName;
	public final static String executablePath;
	public final static String installationPath;
	public final static String currentVersion;
	public final static String javaBin;
	public final static boolean isWindows;
	
	static {
		debugMode = ManagementFactory.getRuntimeMXBean()
				.getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
		currentVersion = Constants.class.getPackage().getImplementationVersion();
		File cpf = new File(System.getProperty("java.class.path"));
		if (cpf.isFile()) {
			classPath = null;
			executableName = cpf.getName();
			executablePath = cpf.getAbsolutePath();
			installationPath = new File(cpf.getParent()).getAbsolutePath();
		} else {
			classPath = cpf.getAbsolutePath();
			executableName = null;
			executablePath = null;
			installationPath = cpf.getAbsolutePath();
		}
		javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = os.contains("win");
	}
	
}
