package com.dgsspa.activitytracker.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MapUtils {
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> asMap(Object... params) {
	    if (params.length % 2 > 0) { 
	      throw new IllegalArgumentException("The number of arguments must be even");
	    }
	    int n = params.length / 2;
	    Map<K, V> map = new LinkedHashMap<K, V>();
	    try {
	      for (int i = 0; i < n; i ++) {
	        Object key = params[2 * i];
	        Object value = params[2 * i + 1];
	        map.put(key != null ? (K)key : null, value != null ? (V)value : null);
	      }
	    } catch (ClassCastException ex) {
	      throw new IllegalArgumentException("The arguments must be consistent in type");
	    }
	    return map;
	  }
}
