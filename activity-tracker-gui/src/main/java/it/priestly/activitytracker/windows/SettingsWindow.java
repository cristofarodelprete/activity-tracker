package it.priestly.activitytracker.windows;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.utils.DelegatedAction;

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
	
	public SettingsWindow(String title, String button, Map<ConfigKey,String> configLabels,
			Map<ConfigKey,String> configValues, Consumer<Map<ConfigKey,String>> submit,
			String resetLabel, Consumer<JButton> resetAction, String exportLabel, Consumer<JButton> exportAction) {
		super(title,
				button,
				configLabels.entrySet().stream().collect(Collectors.toMap(
						k -> k.getKey().name(),
						k -> k.getValue()
				)),
				configValues.entrySet().stream().collect(Collectors.toMap(
						p -> p.getKey().name(),
						p -> p.getValue()
				)),
				(map) -> {
					submit.accept(map.entrySet().stream().collect(Collectors.toMap(
									p -> Enum.valueOf(ConfigKey.class, p.getKey()),
									p -> p.getValue()
							)));
					return true;
				});
		this.resetLabel = resetLabel;
		this.resetAction = resetAction;
		this.exportLabel = exportLabel;
		this.exportAction = exportAction;
	}

}
