package com.dgsspa.activitytracker.utils;

public abstract class CsvPrinter<T> {

	private static final String newLine = "\n";
	private static final String comma = ",";
	private static final String quotes = "\"";
	
	protected abstract String[] getColumns();
	
	protected abstract Object[] getValues(T item);
	
	private static String tokenize(Object obj) {
		String token = obj != null ? obj.toString() : "";
		if (token.contains(quotes)) {
			token = quotes + token.replace(quotes, quotes + quotes) + quotes;
		} else if (token.contains(comma) || token.contains(newLine)) {
			token = quotes + token + quotes;
		}
		return token;
	}
	
	public byte[] print(Iterable<T> items) {
		StringBuilder builder = new StringBuilder();
		String[] columns = getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) builder.append(comma);
			builder.append(tokenize(columns[i]));
		}
		for (T item : items) {
			Object[] values = getValues(item);
			builder.append(newLine);
			for (int i = 0; i < values.length; i++) {
				if (i > 0) builder.append(comma);
				builder.append(tokenize(values[i]));
			}
		}
		return builder.toString().getBytes();
	}
}
