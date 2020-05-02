package it.priestly.activitytracker.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

@Component
@Configuration
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@PropertySource("classpath:update.properties")
public class UpdateHelper {

	private class UpdateAsset {
		public final String name;
		
		public final String url;
		
		public final boolean archive;
		
		public UpdateAsset(String name, String url, boolean archive) {
			this.name = name;
			this.url = url;
			this.archive = archive;
		}
	}
	
	@Autowired
    private UiHelper uiHelper;
	
	@Value("${application.executable:}")
	private String executableName;
	
	@Value("${application.updateUrl:}")
	private String updateUrl;

	@Value("${application.assetsToUpdate:}")
	private String assetsToUpdate;

	private String path = System.getProperty("user.dir");
	
	public void restart() {
		try {
			Runtime.getRuntime().exec(new File(path, executableName).getAbsolutePath(), null, new File(path));
		} catch (IOException e) {
			uiHelper.error("message.errorRestarting");
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
	
	private void update(List<UpdateAsset> assets, Runnable callback) {
		for (UpdateAsset asset : assets) {
			try (InputStream inputStream = new URL(asset.url).openStream()) {
				if (asset.name.equals(executableName)) {
					Files.move(Paths.get(path, executableName), Paths.get(path, executableName + ".bck"), StandardCopyOption.REPLACE_EXISTING);
				}
				if (asset.archive) {
					unzip(path, inputStream);
				} else {
					Files.copy(inputStream, Paths.get(path, asset.name), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				uiHelper.getMessage("message.errorUpdating");
				System.exit(1);
			};
		}
		restart();
	}
	
	public void checkUpdates(String currentVersion, Runnable callback) {
		try {
			Files.deleteIfExists(Paths.get(path, executableName + ".bck"));
		} catch (IOException e1) {
			// do nothing
		}
		if (currentVersion != null) {
			String latestVersion = null;
			List<UpdateAsset> assets = new ArrayList<UpdateAsset>();
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				JsonNode latest = objectMapper.readTree(new URL(updateUrl));
				latestVersion = latest.get("tag_name").asText().substring(1);
				latest.get("assets").forEach(asset -> {
					String assetName = asset.get("name").asText();
					if (assetsToUpdate == null || assetsToUpdate .isEmpty() ||
							assetName.matches(assetsToUpdate)) {
						String assetUrl = asset.get("browser_download_url").asText();
						boolean isArchive = assetName.endsWith(".zip");
						assets.add(new UpdateAsset(assetName, assetUrl, isArchive));
					}
				});
			} catch (Exception e) {
				latestVersion = null;
			}
			if (compareVersions(currentVersion, latestVersion) > 0) {
				uiHelper.confirm(uiHelper.getMessage("message.confirmUpdate", currentVersion, latestVersion), () -> {
					update(assets, callback);
				}, callback);
				return;
			}
		}
		callback.run();
	}
}
