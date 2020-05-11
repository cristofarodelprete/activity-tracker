package it.priestly.activitytracker.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.support.UpdateAsset;
import it.priestly.activitytracker.support.UpdateMonitor;

@Component
@Configuration
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@PropertySource(value = "classpath*:update.properties", ignoreResourceNotFound = true)
public class ApplicationUpdater {
	
	@Autowired
    private UiHelper uiHelper;
	
	@Value("${application.updateUrl:}")
	private String updateUrl;

	@Value("${application.assetsFilter:}")
	private String assetsFilter;

	public void restart(String newExecutableName) {
		if (Constants.executableName != null) {
			try {
				try (InputStream resourceStream = ApplicationUpdater.class.getClassLoader().getResourceAsStream("ApplicationRestarter.bin")) {
					Files.copy(
							resourceStream,
							Paths.get(Constants.installationPath, "ApplicationRestarter.class"),
							StandardCopyOption.REPLACE_EXISTING);
				}
		        List<String> command = new LinkedList<String>();
		        command.add(Constants.javaBin);
		        command.add("ApplicationRestarter");
		        command.add(Constants.executableName);
		        if (newExecutableName != null && !newExecutableName.equals(Constants.executableName)) {
		        	command.add(newExecutableName);
		        }
		        Runtime.getRuntime().exec(command.toArray(new String[0]), null, new File(Constants.installationPath));
		        uiHelper.exit();
			} catch (Exception e) {
				uiHelper.error(uiHelper.getMessage("message.errorUpdating"), uiHelper::die);
			}
		} else {
			uiHelper.info(uiHelper.getMessage("message.restartRequired"), uiHelper::exit);
		}
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
	
	private void unzip(String destination, InputStream inputStream) throws IOException {
		String destinationPath = new File(destination).getCanonicalPath() + File.separator; 
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
	        ZipEntry zipEntry = zipInputStream.getNextEntry();
	        while (zipEntry != null) {
	            File destFile = new File(destination, zipEntry.getName());
	            String destPath = destFile.getCanonicalPath();
	            if (!destPath.startsWith(destinationPath)) {
	                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	            }
	            Files.copy(zipInputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            zipEntry = zipInputStream.getNextEntry();
	        }
	        zipInputStream.closeEntry();
        }
	}
	
	private void update(String version, List<UpdateAsset> assets) {
		String newExecutableName = null;
		UpdateMonitor updateMonitor = uiHelper.createUpdateMonitor(version, assets);
		for (UpdateAsset asset : assets) {
			updateMonitor.setActiveAsset(asset);
			try (UpdateInputStream inputStream = new UpdateInputStream(updateMonitor, asset)) {
				String assetName = asset.name;
				if (assetName.equals(Constants.executableName)) {
					assetName = assetName + ".new";
					newExecutableName = assetName;
				}
				if (asset.archive) {
					unzip(Constants.installationPath, inputStream);
				} else {
					Files.copy(inputStream, Paths.get(Constants.installationPath, assetName), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (Exception e) {
				uiHelper.error(uiHelper.getMessage("message.errorUpdating"), uiHelper::die);
			};
			updateMonitor.unsetActiveAsset();
		}
		final String toRun = newExecutableName; 
		uiHelper.info(uiHelper.getMessage("message.updated", version), () -> {
			restart(toRun);
		});
		
	}
	
	public void checkUpdates(boolean silent) {
		if (Constants.currentVersion != null && updateUrl != null && !updateUrl.isEmpty()) {
			String latestVersion = null;
			List<UpdateAsset> assets = new ArrayList<UpdateAsset>();
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				JsonNode latest = objectMapper.readTree(new URL(updateUrl));
				latestVersion = latest.get("tag_name").asText().substring(1);
				latest.get("assets").forEach(asset -> {
					String assetName = asset.get("name").asText();
					if (assetsFilter == null || assetsFilter .isEmpty() ||
							assetName.matches(assetsFilter)) {
						String assetUrl = asset.get("browser_download_url").asText();
						int assetSize = asset.get("size").asInt();
						boolean isArchive = assetName.endsWith(".zip");
						assets.add(new UpdateAsset(assetName, assetUrl, assetSize, isArchive));
					}
				});
			} catch (Exception e) {
				latestVersion = null;
				if (!silent) uiHelper.error(uiHelper.getMessage("message.errorCheckingUpdates"));
				return;
			}
			if (compareVersions(Constants.currentVersion, latestVersion) > 0) {
				final String version = latestVersion;
				uiHelper.confirm(uiHelper.getMessage(
						"message.confirmUpdate", Constants.currentVersion, latestVersion
						), () -> {
							update(version, assets);
						});
				return;
			}
		}
		if (!silent) uiHelper.info(uiHelper.getMessage("message.noUpdates"));
	}
}
