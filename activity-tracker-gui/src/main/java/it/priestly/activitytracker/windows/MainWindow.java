package it.priestly.activitytracker.windows;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.objects.Activity;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.support.FadeMouseListener;
import it.priestly.activitytracker.support.FrameDragListener;
import it.priestly.activitytracker.support.UiHelper;
import it.priestly.activitytracker.utils.ConfigurationHelper;
import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.GuiConstants;
import it.priestly.activitytracker.utils.MapUtils;
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
	
	private JPanel list = null;
	
	private Map<Activity, UpdateButtonAnimation> activityMap = new HashMap<Activity, UpdateButtonAnimation>();
	
	private FadeMouseListener fadeMouseListener = null;
	
	private FrameDragListener frameDragListener = null;

	private JButton settings = null;

	private JButton add = null;

	private JButton exit = null;

	private JLabel title = null;

	private String deleteText = null;
	
	private String zpad(int n, int d) {
		String s = Long.toString(n);
		while (s.length() < d) {
			s = "0" + s;
		}
		return s;
	}
	
	private String buttonPrinter(Activity activity) {
		String time = null;
		Duration duration = activity.getTotalAllocatedTime();
		if (!duration.isZero() && !duration.isNegative()) {
			long tmp = duration.getSeconds();
			int seconds = (int)(tmp % 60);
			tmp /= 60;
			int minutes = (int)(tmp % 60);
			tmp /= 60;
			int hours = (int)tmp;
			time = zpad(minutes, 2) + ":" + zpad(seconds, 2);
			if (hours > 0) {
				time = zpad(hours, 2) + ":" + time;
			}
		}
		if (time != null) {
			return activity.getName() + " (" + time + ")";
		} else {
			return activity.getName();
		}
	}
	
	public void render() {
		for (UpdateButtonAnimation animation : activityMap.values()) {
			animation.stop();
			activityMap.clear();
		}
		Iterable<Activity> activities = activityService.getActivities();
		list.removeAll();
		GridBagLayout layout = (GridBagLayout)list.getLayout();
		int y = 0;
		for (Activity activity : activities) {
			if (activity != null) {
				JButton button = new JButton();
				button.setAction(new DelegatedAction(e -> {
					activityService.switchToActivity(activity.getId());
					render();
				}));
				button.setText(buttonPrinter(activity));
				button.setFont(button.getFont().deriveFont(Font.BOLD));
				button.setForeground(Color.WHITE);
				button.setBackground(activity.isActive() ? Color.decode("#008000") : Color.decode("#800000"));
				layout.setConstraints(button, new GridBagConstraints(0, y, 1, 1, 1, 0,
						GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				list.add(button);
				if (activity.isActive()) {
					activityMap.put(activity, new UpdateButtonAnimation(button, activity, this::buttonPrinter));
				}
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
				y++;
			}
		}
		validate();
		pack();
		repaint();
		for (UpdateButtonAnimation animation : activityMap.values()) {
			new Thread(animation).start();
		}
	}
	
	private void exit() {
		if (!activityMap.isEmpty()) {
			String message = activityMap.size() > 1 ?
					uiHelper.getMessage("message.activitiesRunning", activityMap.size()) :
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
			form.run(
					uiHelper.getMessage("dialog.title.create"),
					uiHelper.getMessage("button.label.create.submit"),
					MapUtils.asMap(
						"attivita", new Field<String>(String.class, uiHelper.getMessage("field.label.attivita"))
					),
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
		boolean enableTransparency = configurationHelper.get(ConfigKey.enableTransparency);
		if (enableTransparency && GuiConstants.transparencySupported) {
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
