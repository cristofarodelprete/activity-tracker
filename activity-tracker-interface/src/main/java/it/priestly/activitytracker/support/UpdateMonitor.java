package it.priestly.activitytracker.support;

public interface UpdateMonitor {
	void setActiveAsset(UpdateAsset asset);
	
	void unsetActiveAsset();
	
	void update(UpdateAsset asset, int bytes);
	
	void update(UpdateAsset asset, long bytes);

	boolean isCanceled();
}
