package it.priestly.activitytracker.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CancellationException;

import javax.swing.ProgressMonitor;

public class MonitoredInputStream extends FilterInputStream {
	
	private int nread = 0;
	
	private int startAt;
	
	private ProgressMonitor monitor;
	
	public MonitoredInputStream(ProgressMonitor monitor, int startAt, InputStream in) {
		super(in);
		this.startAt = startAt;
		this.monitor = monitor;
	}
	
	@Override
	public int read() throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int c = in.read();
		if (c >= 0) monitor.setProgress(startAt + (++nread));
		return c;
	}

	@Override
	public int read(byte[] b) throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int nr = in.read(b);
		if (nr > 0) monitor.setProgress(startAt + (nread += nr));
		return nr;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (monitor.isCanceled()) throw new CancellationException();
		int nr = in.read(b, off, len);
		if (nr > 0) monitor.setProgress(startAt + (nread += nr));
		return nr;
	}

	@Override
	public long skip(long n) throws IOException {
		long nr = in.skip(n);
		if (nr > 0) monitor.setProgress(startAt + (nread += nr));
		return nr;
	}
}