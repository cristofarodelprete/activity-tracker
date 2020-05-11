package it.priestly.activitytracker.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UiHelper {
	
	UpdateMonitor createUpdateMonitor(String version, List<UpdateAsset> assets);
	
	Set<String> getLanguages();
	
	Map<String,String> getLanguageOptions();
	
	void setLocale(String languageTag);

	void setLocale();
	
	String getMessage(String code, Object... args);
	
	void run();
	
	void die();
	
	void exit();
	
	void confirm(String message, Runnable yesCallback);
	
	void confirm(String message, Runnable yesCallback, Runnable noCallback);
	
	void confirm(String message, Runnable yesCallback, Runnable noCallback, Runnable cancelCallback);
	
	void info(String message);	
	
	void info(String message, Runnable callback);
	
	void warning(String message);
	
	void warning(String message, Runnable callback);
	
	void error(String message);
	
	void error(String message, Runnable callback);
	
	void fatal(String message);

	void saveFile(byte[] data);
}
