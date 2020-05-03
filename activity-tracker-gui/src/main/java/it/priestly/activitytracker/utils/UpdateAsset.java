package it.priestly.activitytracker.utils;

public class UpdateAsset {
	public final String name;
	
	public final String url;
	
	public final int size;
	
	public final boolean archive;
	
	public UpdateAsset(String name, String url, int size, boolean archive) {
		this.name = name;
		this.url = url;
		this.size = size;
		this.archive = archive;
	}
}
