package it.priestly.activitytracker.windows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.utils.ConfigurationHelper;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.MapUtils;
import it.priestly.activitytracker.utils.UiHelper;
import it.priestly.activitytracker.utils.UpdateHelper;
import it.priestly.activitytracker.utils.WindowCloseListener;

@Component
public class SettingsWindow extends FormWindow {
	private static final long serialVersionUID = -3540267461075177457L;

	@Autowired
	private UiHelper uiHelper;
	
	@Autowired
	private UpdateHelper updateHelper;
	
	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
	private ActivityService activityService;
	
	private boolean shown = false;
	
	private MainWindow parent = null;
	
	@Override
	protected void addExtraActions(JPanel footer) {
		super.addExtraActions(footer);
		JButton update = new JButton();
		update.setAction(new DelegatedAction(e -> {
			updateHelper.checkUpdates(() -> {
				uiHelper.info(uiHelper.getMessage("message.noUpdates"));
			});
		}));
		update.setText(uiHelper.getMessage("button.label.checkUpdates"));
		footer.add(update);
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
			uiHelper.saveFile(update, data);
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run(MainWindow parent) {
		if (shown) {
			setVisible(true);
			toFront();
			requestFocus();
			return;
		}
		this.parent = parent;
		Map<ConfigKey,Object> config = configurationHelper.get();
		Map<ConfigKey,Map<Object,String>> options = MapUtils.asMap(
				ConfigKey.language, uiHelper.getLanguageOptions());
		super.run(
				uiHelper.getMessage("dialog.title.settings"),
				uiHelper.getMessage("button.label.settings.submit"),
				Arrays.stream(ConfigKey.values()).collect(Collectors.toMap(
						k -> k.name(),
						k -> new Field(
								k.type(),
								uiHelper.getMessage("settings.label." + k.name()),
								options.get(k),
								config.get(k)
						)
				)),
				(map) -> {
					Map<ConfigKey,Object> toSubmit = new HashMap<>();
					for (Map.Entry<String,Field<?>> entry : map.entrySet()) {
						toSubmit.put(
								Enum.valueOf(ConfigKey.class, entry.getKey()),
								entry.getValue().getValue());
					}
					configurationHelper.set(toSubmit);
					if (parent != null) parent.updateConfig();
					return true;
				});
		
		build();
		pack();
		setVisible(true);
		shown = true;
	}

}
