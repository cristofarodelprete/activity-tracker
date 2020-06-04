package it.priestly.activitytracker.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.support.UpdateAsset;
import it.priestly.activitytracker.support.UpdateMonitor;
import it.priestly.activitytracker.windows.MainWindow;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UiHelperGuiImpl implements UiHelper {

	private static final String[] languages;
	
	private Locale locale;
	
	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
    private MessageSource messageSource;

	@Autowired
    private MainWindow mainWindow;
	
	static {
		List<String> tags = new ArrayList<String>();
		try {
			PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = patternResolver.getResources("classpath:localization_*.properties");
			for (Resource resource : resources) {
				String filename = resource.getFilename();
				tags.add(filename.substring(13, filename.length() - 11));
			}
		} catch (Exception ex) {
			tags.clear();
		}
		languages = tags.toArray(new String[0]);
	}
	
	public UiHelperGuiImpl() {
		locale = Locale.getDefault();
	}
	
	@Override
	public UpdateMonitor createUpdateMonitor(String version, List<UpdateAsset> assets) {
		return new UpdateMonitorImpl(this, version, assets);
	}
	
	public Set<String> getLanguages() {
		return Collections.unmodifiableSet(Arrays.stream(languages).collect(Collectors.toSet()));
	}
	
	public Map<String,String> getLanguageOptions() {
		Map<String,String> languageMap = new LinkedHashMap<>();
		languageMap.put(null, getMessage("settings.options.language.default"));
		for (String language : getLanguages()) {
			languageMap.put(language, getMessage("settings.options.language." + language));
		}
		return languageMap;
	}
	
	public void setLocale(String languageTag) {
		locale = Locale.forLanguageTag(languageTag);
	}

	public void setLocale() {
		String languageTag = configurationHelper.get(ConfigKey.language);
		if (languageTag != null) {
			setLocale(languageTag);
		} else {
			setLocale(Locale.getDefault().toLanguageTag());
		}
	}
	
	public String getMessage(String code, Object... args) {
		return messageSource.getMessage(code, args, locale);
	}

	public void run() {
        mainWindow.run();
	}
	
	public void die() {
        System.exit(1);
	}
	
	public void exit() {
        System.exit(0);
	}
	
	public void confirm(String message, Runnable yesCallback) {
		confirm(message, yesCallback, null, null);
	}
	
	public void confirm(String message, Runnable yesCallback, Runnable noCallback) {
		confirm(message, yesCallback, noCallback, noCallback);
	}
	
	public void confirm(String message, Runnable yesCallback, Runnable noCallback, Runnable cancelCallback) {
		new Thread(() -> {
			int result = JOptionPane.showConfirmDialog(null, message, null, JOptionPane.YES_NO_OPTION);
			if (result == 0) {
				if (yesCallback != null) {
					yesCallback.run();
				}
			} else if (result == 1) {
				if (noCallback != null) {
					noCallback.run();
				}
			} else {
				if (cancelCallback != null) {
					cancelCallback.run();
				}
			}
		}).start();
	}
	
	public void info(String message) {
		info(message, null);
	}	
	
	public void info(String message, Runnable callback) {
		new Thread(() -> {
			JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.info"), JOptionPane.INFORMATION_MESSAGE);
			if (callback != null) callback.run();
		});
	}
	
	public void warning(String message) {
		warning(message, null);
	}
	
	public void warning(String message, Runnable callback) {
		new Thread(() -> {
			JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
			if (callback != null) callback.run();
		});
	}
	
	public void error(String message) {
		error(message, null);
	}	
	
	public void error(String message, Runnable callback) {
		new Thread(() -> {
			JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
			if (callback != null) callback.run();
		});
	}
	
	public void fatal(String message) {
		new Thread(() -> {
			JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
			die();
		});
	}

	public void saveFile(byte[] data) {
		new Thread(() -> {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try (OutputStream stream = new FileOutputStream(file, false)) {
					stream.write(data);
					stream.flush();
				} catch (IOException e) {
					error(getMessage("message.errorSaving"), null);
				}
			}
		}).start();

	}
}
