package it.priestly.activitytracker.converters;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class EntityConverter<SRC, DST> {
	
	private final Class<SRC> srcClass;
	
	private final Class<DST> dstClass;
	
	protected abstract void convertFrom(SRC src, DST dst);
	
	protected abstract void convertTo(SRC src, DST dst);
	
	protected EntityConverter(Class<SRC> srcClass, Class<DST> dstClass) {
		this.srcClass = srcClass;
		this.dstClass = dstClass;
	}
	
	public SRC from(SRC src, DST dst) {
		if (dst != null) {
			if (src == null) {
				try {
					src = srcClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) { }
			}
			convertFrom(src, dst);
			return src;
		} else {
			return null;
		}
	}
	
	public DST to(SRC src, DST dst) {
		if (src != null) {
			if (dst == null) {
				try {
					dst = dstClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) { }
			}
			convertTo(src, dst);
			return dst;
		} else {
			return null;
		}
	}
		
	public SRC from(DST dst) {
		return from(null, dst);
	}
	
	public DST to(SRC src) {
		return to(src, null);
	}
	
	public Optional<SRC> from(Optional<DST> dst) {
		return dst.isPresent() ? Optional.of(from(dst.get())) : Optional.empty();
	}
	
	public Optional<DST> to(Optional<SRC> src) {
		return src.isPresent() ? Optional.of(to(src.get())) : Optional.empty();
	}
	
	public List<SRC> from(List<DST> dst) {
		return dst != null ? dst.stream().map(this::from).collect(Collectors.toList()) : null;
	}
	
	public List<DST> to(List<SRC> src) {
		return src != null ? src.stream().map(this::to).collect(Collectors.toList()) : null;
	}
}
