package it.priestly.activitytracker.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ConfigurationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UiHelper {

	private final String[] languages = new String[] { "en", "it" };
	
	private Locale locale = Locale.getDefault();
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
    private MessageSource messageSource;
	
	public List<String> getLanguages() {
		return Arrays.asList(languages);
	}
	
	public void setLocale(String languageTag) {
		locale = Locale.forLanguageTag(languageTag);
	}

	public void setLocale() {
		String languageTag = configurationService.getConfig(ConfigKey.language);
		if (languageTag != null) {
			setLocale(languageTag);
		} else {
			setLocale(Locale.getDefault().toLanguageTag());
		}
	}
	
	public String getMessage(String code, Object... args) {
		return messageSource.getMessage(code, args, locale);
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
			int result = JOptionPane.showConfirmDialog(null, message);
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
		JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.info"), JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void warning(String message) {
		JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
	}
	
	public void error(String message) {
		JOptionPane.showMessageDialog(new JFrame(), message, getMessage("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
	}
	
	public void fatal(String message) {
		error(message);
        System.exit(1);
	}

	public void saveFile(java.awt.Component parent, byte[] data) {
		new Thread(() -> {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try (OutputStream stream = new FileOutputStream(file, false)) {
					stream.write(data);
					stream.flush();
				} catch (IOException e) {
					error(getMessage("message.errorSaving"));
				}
			}
		}).start();

	}
}
