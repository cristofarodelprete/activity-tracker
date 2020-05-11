package it.priestly.activitytracker.windows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.springframework.stereotype.Component;

@Component
public class RegistryHelper {

	@SuppressWarnings("unchecked")
	private <T> T fromRegistryValue(String type, String value) {
		if (type == null || value == null || type.equals("REG_NONE")) return null;
		try {
			if (type.equals("REG_SZ") || type.equals("REG_LINK") || type.equals("REG_EXPAND_SZ")) {
				return (T)value;
			} else if (type.equals("REG_MULTI_SZ")) {
				return (T)value.split("\\\\0");
			} else if (type.startsWith("REG_DWORD")) {
				//boolean bigEndian = type.endsWith("_BIG_ENDIAN");
				if (value.startsWith("0x")) value =  value.substring(2);
				return (T)Integer.valueOf(value, 16);
			} else if (type.startsWith("REG_QWORD")) {
				//boolean bigEndian = type.endsWith("_BIG_ENDIAN");
				if (value.startsWith("0x")) value =  value.substring(2);
				return (T)Long.valueOf(value, 16);
			} else if (type.equals("REG_BINARY")) {
				return (T)DatatypeConverter.parseHexBinary(value);
			} else {
				return null;
			}
		} catch (ClassCastException ex) {
			return null;
		}
	}
	
	private String toRegistryString(Object value) {
		if (value instanceof String) {
			return (String)value;
		} else if(value instanceof String[]) {
			return String.join("\\0", (String[])value);
		} else if(value instanceof Integer) {
			return "0x" + Integer.toHexString((Integer)value);
		} else if(value instanceof Long) {
			return "0x" + Long.toHexString((Long)value);
		} else if(value instanceof byte[]) {
			return DatatypeConverter.printHexBinary((byte[])value);
		} else {
			return null;
		}
	}

	private String toRegistryType(Class<?> clazz) {
		if (clazz.equals(String.class)) {
			return "REG_SZ";
		} else if(clazz.equals(String[].class)) {
			return "REG_MULTI_SZ";
		} else if(clazz.equals(Integer.class)) {
			return "REG_DWORD";
		} else if(clazz.equals(Long.class)) {
			return "REG_QWORD";
		} else if(clazz.equals(byte[].class)) {
			return "REG_BINARY";
		} else {
			return null;
		}
	}
	
	public <T> T read(String path, String key) {
		try {
			Process process = Runtime.getRuntime().exec("reg query \"" + path + "\" /v " + key);
			StringBuilder output = new StringBuilder(); 
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				String line;
	            while ((line = bufferedReader.readLine()) != null)
	            {
	                output.append(line + System.lineSeparator());
	            }
	            process.waitFor();
			} catch (Exception ex) {
				return null;
			}
			String[] lines = output.toString().split(System.lineSeparator());
			if (lines.length <= 2) return null;
			String outputLine = String.join(System.lineSeparator(),
					Arrays.copyOfRange(lines, 2, lines.length));
			String prefix = "    " + key + "    "; 
			String type = null;
			String valueString = null;
			if (outputLine.startsWith(prefix)) {
				outputLine = outputLine.substring(prefix.length());
				int blank = outputLine.indexOf("    ");
				type = outputLine.substring(0, blank);
				valueString = outputLine.substring(blank + 4);
			}
			return fromRegistryValue(type, valueString);
		} catch (Exception e) {
			return null;
		}

	}
	
	public <T> void write(String path, String key, T value) {
		String command = null;
		if (value != null) {
			String type = toRegistryType(value.getClass());
			String valueString = toRegistryString(value);
			if (type == null || valueString == null) return;
			command = "reg add \"" + path + "\" /v " + key + " /t " + type + " /d " + valueString + " /f";
		} else {
			command = "reg delete \"" + path + "\" /v " + key + " /f";
		}
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			int i = 0;
		}
	}
}
