package it.priestly.activitytracker.windows;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.animations.UpdateButtonAnimation;
import it.priestly.activitytracker.enums.ActivityDisplayStyle;
import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.objects.Activity;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.support.FadeMouseListener;
import it.priestly.activitytracker.support.FrameDragListener;
import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.utils.ConfigurationHelper;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.GuiUtils;
import it.priestly.activitytracker.utils.WindowCloseListener;

@Component
public class MainWindow extends JFrame {
	private static final long serialVersionUID = -150759974554189358L;
	
	@Autowired
	private UiHelper uiHelper;

	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
	private ActivityService activityService;

	@Autowired
	private SettingsWindow settingsWindow;
	
	@Autowired
	private GuiUtils guiUtils;
	
	private JPanel list = null;
	
	UpdateButtonAnimation activityAnimation = null;
	
	private FadeMouseListener fadeMouseListener = null;
	
	private FrameDragListener frameDragListener = null;

	private JButton settings = null;

	private JButton add = null;

	private JButton exit = null;

	private JLabel title = null;

	private String deleteText = null;
	
	private ActivityDisplayStyle displayStyle = null;
	
	private String formatTime(Duration duration) {
		List<Integer> time = new ArrayList<Integer>();
		if (!duration.isZero() && !duration.isNegative()) {
			long tmp = duration.getSeconds();
			time.add((int)(tmp % 60));
			tmp /= 60;
			if (tmp > 0) {
				time.add((int)(tmp % 60));
				tmp /= 60;
				if (tmp > 0) {
					time.add((int)tmp);
				}
			}
		}
		Collections.reverse(time);
		return time.stream().map(t -> zpad(t, 2))
				.collect(Collectors.joining(":"));
	}
	
	private String zpad(int n, int d) {
		String s = Long.toString(n);
		while (s.length() < d) {
			s = "0" + s;
		}
		return s;
	}
	
	private String formatPercentage(Duration current, Duration total) {
		if (current.isNegative() || current.isZero()) return "";
		double n = (double)current.getSeconds() / (double)total.getSeconds();
		return Double.toString(((double)Math.round(n * 1000)) / 10d) + "%";
	}
	
	private void buttonPrinter(Map<JButton,Activity> activity) {
		Duration total = Duration.ZERO;
		Map<JButton,Duration> activityDurations = new HashMap<JButton,Duration>();
		for (Map.Entry<JButton,Activity> entry : activity.entrySet()) {
			Duration time = entry.getValue().getTotalAllocatedTime();
			total = total.plus(time);
			activityDurations.put(entry.getKey(), time);
		}
		for (JButton button : activity.keySet()) {
			String details = "";
			if (displayStyle == ActivityDisplayStyle.time ||
					displayStyle == ActivityDisplayStyle.timeAndPercentage) {
				details = formatTime(activityDurations.get(button));
			}
			if (displayStyle == ActivityDisplayStyle.percentage ||
					displayStyle == ActivityDisplayStyle.timeAndPercentage) {
				details = (!details.isEmpty() ? details + " - " : "") +
						formatPercentage(activityDurations.get(button), total);
			}
			button.setText(activity.get(button).getName() +
					(!details.isEmpty() ? " (" + details + ")" : ""));
		}
	}
	
	public void render() {
		if (activityAnimation != null) {
			activityAnimation.stop();
			activityAnimation = null;
		}
		List<Activity> activities = activityService.getActivities();
		list.removeAll();
		GridBagLayout layout = (GridBagLayout)list.getLayout();
		int y = 0;
		Map<JButton,Activity> buttonMap = new HashMap<JButton,Activity>();
		for (Activity activity : activities) {
			if (activity != null) {
				JButton button = new JButton();
				button.setAction(new DelegatedAction(e -> {
					activityService.switchToActivity(activity.getId());
					render();
				}));
				button.setText("");
				button.setFont(button.getFont().deriveFont(Font.BOLD));
				button.setForeground(Color.WHITE);
				button.setBackground(activity.isActive() ? Color.decode("#008000") : Color.decode("#800000"));
				layout.setConstraints(button, new GridBagConstraints(0, y, 1, 1, 1, 0,
						GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				list.add(button);
				JButton delete = new JButton();
				delete.setAction(new DelegatedAction(e -> {
					uiHelper.confirm(uiHelper.getMessage("message.confirmDelete", activity.getName()), () -> {
						activityService.deleteActivity(activity.getId());
						render();
					});
				}));
				delete.setText(deleteText);
				layout.setConstraints(delete, new GridBagConstraints(1, y, 1, 1, 0, 0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				list.add(delete);
				buttonMap.put(button, activity);
				y++;
			}
		}
		buttonPrinter(buttonMap);
		activityAnimation = new UpdateButtonAnimation(buttonMap, this::buttonPrinter);
		validate();
		pack();
		repaint();
		new Thread(activityAnimation).start();
	}
	
	private void exit() {
		if (activityAnimation != null) {
			long count = activityAnimation.countActive();
			String message = count > 1 ?
					uiHelper.getMessage("message.activitiesRunning", count) :
					uiHelper.getMessage("message.activityRunning");
			uiHelper.confirm(message, () -> {
				activityService.switchOff();
				uiHelper.exit();
			}, () -> {
				uiHelper.exit();
			}, null);
		} else {
			uiHelper.exit();
		}
	}
	
	private JPanel buildHeader() {
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		title = new JLabel();
		title.setFont(title.getFont().deriveFont(20f));
		header.add(title);
		header.add(Box.createRigidArea(new Dimension(5, 0)));
		header.add(Box.createHorizontalGlue());
		exit = new JButton();
		exit.setAction(new DelegatedAction(e -> {
			exit();
		}));
		header.add(exit);
		return header;
	}
	
	private JPanel buildFooter() {
		JPanel footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.LINE_AXIS));
		footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    footer.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		footer.add(Box.createHorizontalGlue());
		add = new JButton();
		add.setAction(new DelegatedAction(e -> {
			FormWindow form = new FormWindow();
			Map<String,Field<?>> fields = new HashMap<String,Field<?>>();
			fields.put("attivita", new Field<String>(String.class, uiHelper.getMessage("field.label.attivita")));
			form.run(
					uiHelper.getMessage("dialog.title.create"),
					uiHelper.getMessage("button.label.create.submit"),
					fields,
					(map) -> {
						String activityName = (String)map.get("attivita").getValue();
						if (activityName != null && !activityName.isEmpty() &&
								!activityService.activityExists(activityName)) {
							Activity activity = new Activity();
							activity.setName(activityName);
							activity = activityService.createActivity(activity);
							render();
							return true;
						} else {
							return false;
						}
					});
			form.build();
			form.pack();
			form.setVisible(true);
		}));
		footer.add(add);
		settings = new JButton();
		settings.setAction(new DelegatedAction(e -> {
			settingsWindow.run(this);
		}));
		footer.add(settings);
		footer.add(Box.createHorizontalGlue());
		return footer;
	}
	
	private void build() {
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		JPanel header = buildHeader();
		content.add(header);
		list = new JPanel();
		list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		list.setLayout(new GridBagLayout());
		content.add(list);
		JPanel footer = buildFooter();
		content.add(footer);
	}
	
	private void setupWindow() {
		dispose();
		uiHelper.setLocale();
		Boolean enableTransparency = configurationHelper.get(ConfigKey.enableTransparency);
		displayStyle = configurationHelper.get(ConfigKey.displayStyle);
		if (enableTransparency != null && enableTransparency && guiUtils.isTransparencySupported()) {
			float hiddenOpacity = configurationHelper.get(ConfigKey.hiddenOpacity);
			int transitionDuration = configurationHelper.get(ConfigKey.fadeDuration);
			if (fadeMouseListener == null) {
				fadeMouseListener = new FadeMouseListener(this, hiddenOpacity, transitionDuration);
				addMouseListener(fadeMouseListener);
			}
			if (frameDragListener == null) {
				frameDragListener = new FrameDragListener(this);
				addMouseListener(frameDragListener);
				addMouseMotionListener(frameDragListener);
			}
			setUndecorated(true);
		} else {
			if (fadeMouseListener != null) {
				removeMouseListener(fadeMouseListener);
				fadeMouseListener = null;
			}
			if (frameDragListener != null) {
				removeMouseListener(frameDragListener);
				removeMouseMotionListener(frameDragListener);
				frameDragListener = null;
			}
			setUndecorated(false);
			setOpacity(1f);
		}
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setAlwaysOnTop(configurationHelper.get(ConfigKey.alwaysOnTop));
		setVisible(true);
	}
	
	private void updateLabels() {
		setTitle(uiHelper.getMessage("dialog.title.list"));
		title.setText(uiHelper.getMessage("dialog.title.list"));
		exit.setText(uiHelper.getMessage("button.label.exit"));
		deleteText = uiHelper.getMessage("button.label.delete");
		add.setText(uiHelper.getMessage("button.label.create"));
		settings.setText(uiHelper.getMessage("button.label.settings"));
	}

	public void updateConfig() {
		setupWindow();
		updateLabels();
		render();
	}
	
	public void run() {
		addWindowListener(new WindowCloseListener(e -> {
			exit();
		}));
		build();
		updateConfig();
	}
}
