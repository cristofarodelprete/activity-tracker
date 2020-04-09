package com.dgsspa.activitytracker.windows;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgsspa.activitytracker.animations.UpdateButtonAnimation;
import com.dgsspa.activitytracker.enums.ConfigKey;
import com.dgsspa.activitytracker.objects.Activity;
import com.dgsspa.activitytracker.services.ActivityService;
import com.dgsspa.activitytracker.services.ConfigurationService;
import com.dgsspa.activitytracker.support.FadeMouseListener;
import com.dgsspa.activitytracker.support.FrameDragListener;
import com.dgsspa.activitytracker.utils.DelegatedAction;
import com.dgsspa.activitytracker.utils.MapUtils;
import com.dgsspa.activitytracker.utils.UiHelper;
import com.dgsspa.activitytracker.utils.WindowCloseListener;

@Component
public class MainWindow extends JFrame implements InitializingBean {
	private static final long serialVersionUID = -150759974554189358L;
	
	@Autowired
	UiHelper uiHelper;
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	ActivityService activityService;
	
	private JPanel list = null;
	
	private UpdateButtonAnimation updateButtonAnimation = null;
	
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
	
	private void render() {
		if (updateButtonAnimation != null)
			updateButtonAnimation.stop();
		Iterable<Activity> activities = activityService.getActivities();
		list.removeAll();
		String deleteText = uiHelper.getMessage("button.label.delete");
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
					updateButtonAnimation = new UpdateButtonAnimation(button, activity, this::buttonPrinter);
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
		if (updateButtonAnimation != null)
			new Thread(updateButtonAnimation).start();
	}
	
	private JPanel buildHeader() {
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel title = new JLabel();
		title.setText(uiHelper.getMessage("dialog.title.list"));
		title.setFont(title.getFont().deriveFont(20f));
		header.add(title);
		header.add(Box.createRigidArea(new Dimension(5, 0)));
		header.add(Box.createHorizontalGlue());
		JButton exit = new JButton();
		exit.setAction(new DelegatedAction(e -> {
			activityService.switchOff();
			uiHelper.exit();
		}));
		exit.setText(uiHelper.getMessage("button.label.exit"));
		header.add(exit);
		return header;
	}
	
	private JPanel buildFooter() {
		JPanel footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.LINE_AXIS));
		footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    footer.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		footer.add(Box.createHorizontalGlue());
		JButton add = new JButton();
		add.setAction(new DelegatedAction(e -> {
			FormWindow form = new FormWindow(
					uiHelper.getMessage("dialog.title.create"),
					uiHelper.getMessage("button.label.create.submit"),
					MapUtils.asMap(
						"attivita", uiHelper.getMessage("field.label.attivita")
					),
					(map) -> {
						String activityName = map.get("attivita");
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
		add.setText(uiHelper.getMessage("button.label.create"));
		footer.add(add);
		JButton settings = new JButton();
		settings.setAction(new DelegatedAction(e -> {
			Map<ConfigKey,String> config = configurationService.getRawConfig();
			SettingsWindow form = new SettingsWindow(
					uiHelper.getMessage("dialog.title.settings"),
					uiHelper.getMessage("button.label.settings.submit"),
					Arrays.stream(ConfigKey.values()).collect(Collectors.toMap(
							k -> k,
							k -> uiHelper.getMessage("settings.label." + k.name())
					)),
					config,
					(cfg) -> {
						configurationService.setRawConfig(cfg);
						uiHelper.info(uiHelper.getMessage("message.restartRequired"));
					},
					uiHelper.getMessage("button.label.reset"),
					b -> {
						uiHelper.confirm(uiHelper.getMessage("message.confirmReset"), () -> {
							activityService.clearAll();
							render();
						});
					},
					uiHelper.getMessage("button.label.export"),
					b -> {
						byte[] data = activityService.exportAll();
						uiHelper.saveFile(b, data);
					}
			);
			form.build();
			form.pack();
			form.setVisible(true);
		}));
		settings.setText(uiHelper.getMessage("button.label.settings"));
		footer.add(settings);
		footer.add(Box.createHorizontalGlue());
		return footer;
	}
	
	private void build(Container content) {
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

	@Override
	public void afterPropertiesSet() throws Exception {
		String languageTag = configurationService.getConfig(ConfigKey.language);
		if (languageTag != null) uiHelper.setLocale(languageTag);
		setTitle(uiHelper.getMessage("dialog.title.list"));
		if (configurationService.getConfig(ConfigKey.enableTransparency, false)) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			if (!gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
				uiHelper.error("Translucency is not supported");
			}
			float hiddenOpacity = configurationService.getConfig(ConfigKey.hiddenOpacity, 0.2f);
			float transitionDuration = configurationService.getConfig(ConfigKey.fadeDuration, 250);
			addMouseListener(new FadeMouseListener(this, hiddenOpacity, transitionDuration));
			FrameDragListener frameDragListener = new FrameDragListener(this);
			addMouseListener(frameDragListener);
			addMouseMotionListener(frameDragListener);
			setUndecorated(true);
			setOpacity(1f);
		}
		setResizable(false);
		setAlwaysOnTop(true);
		addWindowListener(new WindowCloseListener(e -> {
			activityService.switchOff();
			uiHelper.exit();
		}));
		build(getContentPane());
		render();
		setVisible(true);
	}
}
