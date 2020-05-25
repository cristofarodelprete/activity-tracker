package it.priestly.activitytracker.windows;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.utils.ConfigurationHelper;
import it.priestly.activitytracker.utils.Constants;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.ApplicationUpdater;
import it.priestly.activitytracker.utils.WindowCloseListener;

@Component
public class SettingsWindow extends FormWindow {
	private static final long serialVersionUID = -3540267461075177457L;
	
	@Autowired
	private UiHelper uiHelper;
	
	@Autowired
	private ApplicationUpdater updateHelper;
	
	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
	private ActivityService activityService;
	
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
		Map<ConfigKey,Object> toSubmit = new EnumMap<>(ConfigKey.class);
		for (Map.Entry<String,Field<?>> entry : map.entrySet()) {
			toSubmit.put(
					Enum.valueOf(ConfigKey.class, entry.getKey()),
					entry.getValue().getValue());
		}
		configurationHelper.set(toSubmit);
		if (parent != null) parent.updateConfig();
		return true;
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
		Map<ConfigKey,Map<Object,String>> options = new EnumMap<>(ConfigKey.class);
		options.put(ConfigKey.language, new LinkedHashMap<Object,String>(uiHelper.getLanguageOptions()));
		for (ConfigKey key : ConfigKey.values()) {
			if (key.type().isEnum()) {
				Map<Object,String> map = new LinkedHashMap<Object,String>();
				for (Object enumObj : key.type().getEnumConstants()) {
					String enumName = ((Enum<?>)enumObj).name();
					map.put(enumName, uiHelper.getMessage("settings.options." + key.name() + "." + enumName));
				}
				if (!options.containsKey(key)) {
					options.put(key, map);
				}
			}
		}
		Map<String,Field<?>> fieldMap = new LinkedHashMap<>();
		Set<ConfigKey> keys = configurationHelper.getKeys();
		for (ConfigKey key : keys) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Field<?> field = new Field(
					key.type(),
					uiHelper.getMessage("settings.label." + key.name()),
					options.get(key),
					config.get(key)
			);
			fieldMap.put(key.name(), field);
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
