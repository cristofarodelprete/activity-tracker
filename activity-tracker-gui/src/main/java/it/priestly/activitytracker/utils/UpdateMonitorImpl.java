package it.priestly.activitytracker.utils;

import java.util.List;

import javax.swing.ProgressMonitor;

import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.support.UpdateAsset;
import it.priestly.activitytracker.support.UpdateMonitor;

public class UpdateMonitorImpl implements UpdateMonitor {
	
	private final int total;
	
	private int downloaded;
	
	private UpdateAsset currentAsset;
	
	private ProgressMonitor progressMonitor;
	
	private UiHelper uiHelper;
	
	public UpdateMonitorImpl(UiHelper uiHelper, String version, List<UpdateAsset> assets) {
		this.uiHelper = uiHelper;
		this.downloaded = 0;
		this.total = assets.stream().reduce(0, (sum, asset) -> sum + asset.size, (a, b) -> a + b);
		this.progressMonitor = new ProgressMonitor(null, uiHelper.getMessage("dialog.title.updating", version), null, 0, total);
		this.progressMonitor.setMillisToDecideToPopup(100);
		this.progressMonitor.setMillisToPopup(100);
	}

	@Override
	public void update(UpdateAsset asset, int bytes) {
		if (asset != currentAsset) {
			progressMonitor.setNote(uiHelper.getMessage("update.label.downloading", asset.name));
		}
		downloaded += bytes;
		progressMonitor.setProgress(downloaded);

	}

	@Override
	public void update(UpdateAsset asset, long bytes) {
		if (asset != currentAsset) {
			currentAsset = asset;
			progressMonitor.setNote(uiHelper.getMessage("update.label.downloading", asset.name));
		}
		downloaded += bytes;
		progressMonitor.setProgress(downloaded);
	}
	
	@Override
	public boolean isCanceled() {
		return progressMonitor.isCanceled();
	}

	@Override
	public void setActiveAsset(UpdateAsset asset) {
		if (asset != currentAsset) {
			progressMonitor.setNote(uiHelper.getMessage("update.label.downloading", asset.name));
			currentAsset = asset;
		}
	}

	@Override
	public void unsetActiveAsset() {
		progressMonitor.setNote(null);
		currentAsset = null;
	}

}
