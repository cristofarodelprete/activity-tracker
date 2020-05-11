package it.priestly.activitytracker.windows;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.enums.PlatformKey;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.support.PlatformSupport;
import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.utils.ConfigurationHelper;
import it.priestly.activitytracker.utils.Constants;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.GuiConstants;
import it.priestly.activitytracker.utils.MapUtils;
import it.priestly.activitytracker.utils.ApplicationUpdater;
import it.priestly.activitytracker.utils.WindowCloseListener;

@Component
public class SettingsWindow extends FormWindow {
	private static final long serialVersionUID = -3540267461075177457L;
	
	private static final String configKeyPrefix = "cfg";
	
	private static final String platformKeyPrefix = "ptf";

	@Autowired
	private UiHelper uiHelper;
	
	@Autowired
	private ApplicationUpdater updateHelper;
	
	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
	private ActivityService activityService;
	
	@Autowired(required = false)
	private List<PlatformSupport<?>> platformSupport;
	
	private boolean shown = false;
	
	private MainWindow parent = null;
	
	@Override
	protected void addExtraActions(JPanel footer) {
		super.addExtraActions(footer);
		if (Constants.executableName != null) {
			JButton update = new JButton();
			update.setAction(new DelegatedAction(e -> {
				updateHelper.checkUpdates(false);
			}));
			update.setText(uiHelper.getMessage("button.label.checkUpdates"));
			footer.add(update);
		}
		JButton reset = new JButton();
		reset.setAction(new DelegatedAction(e -> {
			uiHelper.confirm(uiHelper.getMessage("message.confirmReset"), () -> {
				activityService.clearAll();
				if (parent != null) parent.render();
			});
		}));
		reset.setText(uiHelper.getMessage("button.label.reset"));
		footer.add(reset);
		JButton export = new JButton();
		export.setAction(new DelegatedAction(e -> {
			byte[] data = activityService.exportAll();
			uiHelper.saveFile(data);
		}));
		export.setText(uiHelper.getMessage("button.label.export"));
		footer.add(export);
	}
	
	public SettingsWindow() {
		super();
		addWindowListener(new WindowCloseListener(e -> {
			shown = false;
			parent = null;
		}));
	}
	
	private boolean submit(Map<String,Field<?>> map) {
		Map<ConfigKey,Object> toSubmitConfig = new EnumMap<>(ConfigKey.class);
		Map<PlatformKey,Object> toSubmitPlatform = new EnumMap<>(PlatformKey.class);
		for (Map.Entry<String,Field<?>> entry : map.entrySet()) {
			if (entry.getKey().startsWith(configKeyPrefix + ":")) {
				ConfigKey key = Enum.valueOf(
						ConfigKey.class,
						entry.getKey().substring(configKeyPrefix.length() + 1));
				toSubmitConfig.put(key, entry.getValue().getValue());
			} else if (entry.getKey().startsWith(platformKeyPrefix + ":")) {
				PlatformKey key = Enum.valueOf(
						PlatformKey.class,
						entry.getKey().substring(configKeyPrefix.length() + 1));
				toSubmitPlatform.put(key, entry.getValue().getValue());
			}
		}
		configurationHelper.set(toSubmitConfig);
		for (PlatformSupport<?> ps : platformSupport) {
			@SuppressWarnings("unchecked")
			PlatformSupport<Object> pso = (PlatformSupport<Object>)ps;
			if (ps.isSupported()) {
				PlatformKey key = ps.getKey();
				if (toSubmitPlatform.containsKey(key)) {
					pso.set(toSubmitPlatform.get(key));
				}
			}
		}
		if (parent != null) parent.updateConfig();
		return true;
	}
	
	private boolean isSettingSupported(ConfigKey key) {
		switch (key) {
			case enableTransparency:
				return GuiConstants.transparencySupported;
			case checkUpdates:
				return Constants.executableName != null;
			default:
				return true;
		}
	}
	
	public void run(MainWindow parent) {
		if (shown) {
			setVisible(true);
			toFront();
			requestFocus();
			return;
		}
		this.parent = parent;
		Map<ConfigKey,Object> config = configurationHelper.get();
		Map<ConfigKey,Map<Object,String>> configOptions = new EnumMap<>(ConfigKey.class);
		configOptions.put(ConfigKey.language, new LinkedHashMap<Object,String>(uiHelper.getLanguageOptions()));
		Map<PlatformKey,Map<Object,String>> platformOptions = MapUtils.asMap();
		Map<String,Field<?>> fieldMap = new LinkedHashMap<>();
		for (ConfigKey key : ConfigKey.values()) {
			if (!isSettingSupported(key)) continue;
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Field<?> field = new Field(
					key.type(),
					uiHelper.getMessage("settings.label.config." + key.name()),
					configOptions.get(key),
					config.get(key)
			);
			fieldMap.put(configKeyPrefix + ":" + key.name(), field);
		}
		Set<PlatformKey> addedPlatformKeys = new HashSet<>();
		if (platformSupport != null) {
			for (PlatformSupport<?> ps : platformSupport) {
				if (ps.isSupported()) {
					PlatformKey key = ps.getKey();
					if (!addedPlatformKeys.contains(key)) {
						addedPlatformKeys.add(key);
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Field<?> field = new Field(
								key.type(),
								uiHelper.getMessage("settings.label.platform." + key.name()),
								platformOptions.get(key),
								ps.get()
						);
						fieldMap.put(platformKeyPrefix + ":" + key.name(), field);
					}
				}
			}
		}
		fieldMap = fieldMap.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		super.run(
				uiHelper.getMessage("dialog.title.settings"),
				uiHelper.getMessage("button.label.settings.submit"),
				fieldMap,
				this::submit);
		build();
		pack();
		setVisible(true);
		shown = true;
	}

}
