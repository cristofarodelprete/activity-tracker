package it.priestly.activitytracker.windows;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;

public class SettingsWindow extends FormWindow {
	private static final long serialVersionUID = -3540267461075177457L;

	private String resetLabel;
	
	private Consumer<JButton> resetAction;
	
	private String exportLabel;
	
	private Consumer<JButton> exportAction;
	
	@Override
	protected void addExtraActions(JPanel footer) {
		super.addExtraActions(footer);
		JButton reset = new JButton();
		reset.setAction(new DelegatedAction(e -> {
			resetAction.accept(reset);
		}));
		reset.setText(resetLabel);
		footer.add(reset);
		JButton export = new JButton();
		export.setAction(new DelegatedAction(e -> {
			exportAction.accept(export);
		}));
		export.setText(exportLabel);
		footer.add(export);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SettingsWindow(String title, String button, Map<ConfigKey,String> configLabels,
			Map<ConfigKey,Object> configValues, Map<ConfigKey,Map<Object,String>> configOptions, 
			Consumer<Map<ConfigKey,Object>> submit, String resetLabel, Consumer<JButton> resetAction,
			String exportLabel, Consumer<JButton> exportAction) {
		super(title,
				button,
				configLabels.entrySet().stream().collect(Collectors.toMap(
						k -> k.getKey().name(),
						k -> new Field(
								k.getKey().type(),
								k.getValue(),
								configOptions.get(k.getKey()),
								configValues.get(k.getKey())
						)
				)),
				(map) -> {
					Map<ConfigKey,Object> toSubmit = new HashMap<>();
					for (Map.Entry<String,Field<?>> entry : map.entrySet()) {
						toSubmit.put(
								Enum.valueOf(ConfigKey.class, entry.getKey()),
								entry.getValue().getValue());
					}
					submit.accept(toSubmit);
					return true;
				});
		this.resetLabel = resetLabel;
		this.resetAction = resetAction;
		this.exportLabel = exportLabel;
		this.exportAction = exportAction;
	}

}
