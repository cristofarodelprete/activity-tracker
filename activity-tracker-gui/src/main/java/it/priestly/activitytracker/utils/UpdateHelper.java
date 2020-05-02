package it.priestly.activitytracker.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Configuration
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@PropertySource("classpath:update.properties")
public class UpdateHelper {

	@Autowired
    private UiHelper uiHelper;
	
	@Value("${application.executable}")
	private String executableName;
	
	@Value("${application.updateUrl}")
	private String updateUrl;
	
	public void restart() {
		String path = System.getProperty("user.dir");
		try {
			Runtime.getRuntime().exec(new File(path, executableName).getAbsolutePath(), null, new File(path));
		} catch (IOException e) {
			uiHelper.error("error restarting application");
			System.exit(1);
		}
		System.exit(0);
	}
	
	private int compareVersions(String a, String b) {
		if (a == null && b == null) return 0;
		if (a != null && b == null) return -1;
		if (a == null && b != null) return 1;
		String[] va = a.split("\\.");
		String[] vb = b.split("\\.");
		int l = Math.min(va.length, vb.length);
		for (int i = 0; i < l; i++) {
			Integer na = null;
			Integer nb = null;
			try { na = Integer.valueOf(va[i]); } catch (NumberFormatException ex) { }
			try { nb = Integer.valueOf(vb[i]); } catch (NumberFormatException ex) { }
			if (na != null && nb == null) return 1;
			if (na == null && nb != null) return -1;
			if (na != null && nb != null) {
				int cmp = nb.compareTo(na);
				if (cmp != 0) return cmp;
			}
		}
		if (va.length > l) return -1;
		if (vb.length > l) return 1;
		return 0;
	}
	
	public void retrieveLatestVersion() {
	}
	
	public void checkUpdates(String currentVersion, Runnable callback) {
		String latestVersion = null;
		Map<String,String> assets = new HashMap<String,String>();
		uiHelper.info("current version: " + currentVersion);
		if (currentVersion != null && !currentVersion.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				JsonNode latest = objectMapper.readTree(new URL(updateUrl));
				latestVersion = latest.get("tag_name").asText().substring(1);
				latest.get("assets").forEach(a -> {
					assets.put(a.get("name").asText(), a.get("browser_download_url").asText());
				});
			} catch (Exception e) {
				uiHelper.error("unable to retrieve latest release");
			}
			if (compareVersions(currentVersion, latestVersion) > 0) {
				uiHelper.confirm("New version found, do you want to update?", () -> {
					for (Map.Entry<String,String> entry : assets.entrySet()) {
						try (InputStream inputStream = new URL(entry.getValue()).openStream()) {
							Files.copy(inputStream, Paths.get(entry.getKey()), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							uiHelper.error("error writing files");
						};
					}
					restart();
				}, callback);
				return;
			}
		}
		callback.run();
	}
}
