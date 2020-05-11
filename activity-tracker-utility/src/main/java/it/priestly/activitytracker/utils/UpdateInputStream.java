package it.priestly.activitytracker.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CancellationException;

import it.priestly.activitytracker.support.UpdateAsset;
import it.priestly.activitytracker.support.UpdateMonitor;

public class UpdateInputStream extends FilterInputStream {
	
	private UpdateAsset asset;
	
	private UpdateMonitor monitor;
	
	public UpdateInputStream(UpdateMonitor monitor, UpdateAsset asset) throws IOException {
		super(new URL(asset.url).openStream());
		this.asset = asset;
		this.monitor = monitor;
	}
	
	@Override
	public int read() throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int c = in.read();
		if (c >= 0) monitor.update(asset, 1);
		return c;
	}

	@Override
	public int read(byte[] b) throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int nr = in.read(b);
		if (nr > 0) monitor.update(asset, nr);
		return nr;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int nr = in.read(b, off, len);
		if (nr > 0) monitor.update(asset, nr);
		return nr;
	}

	@Override
	public long skip(long n) throws IOException {
		long nr = in.skip(n);
		if (nr > 0) monitor.update(asset, nr);
		return nr;
	}
}