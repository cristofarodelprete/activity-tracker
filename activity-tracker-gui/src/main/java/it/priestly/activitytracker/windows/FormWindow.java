package it.priestly.activitytracker.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.Field;
import it.priestly.activitytracker.utils.SimpleActionListener;
import it.priestly.activitytracker.utils.SimpleChangeListener;
import it.priestly.activitytracker.utils.SimpleDocumentListener;
import it.priestly.activitytracker.utils.SpringLayoutUtils;
import it.priestly.activitytracker.utils.WindowCloseListener;

public class FormWindow extends JDialog {
	private static final long serialVersionUID = -8330419378178165024L;

	private Map<String,Field<?>> formFields;
	
	private Function<Map<String,Field<?>>,Boolean> submit;

	private String button;
	
	private JComboBox<?> buildComboBox(Field<Object> field) {
		List<String> options = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		int i = 0;
		Integer selected = null;
		for (Map.Entry<Object,String> entry : field.getOptions().entrySet()) {
			options.add(entry.getValue());
			values.add(entry.getKey());
			if (entry.getKey() == null && field.getValue() == null ||
					entry.getKey() != null && entry.getKey().equals(field.getValue())) {
				selected = i;
			}
			i++;
		}
		JComboBox<?> component = new JComboBox<>(options.toArray(new String[0]));
		if (selected != null) component.setSelectedIndex(selected);
	    component.addActionListener((SimpleActionListener) e -> {
	    	int idx = component.getSelectedIndex();
	    	if (idx < 0) {
	    		field.setValue(null);
	    	} else {
	    		field.setValue(values.get(idx));
	    	}
	    });
	    return component;
	}
	
	private JCheckBox buildCheckBox(Field<Boolean> field) {
		JCheckBox component = new JCheckBox();
		component.setSelected(field.getValue() != null ? field.getValue() : false);
		component.addChangeListener((SimpleChangeListener) e -> {
	    	field.setValue(component.isSelected());
	    });
		return component;
	}

	private JTextField buildTextField(Field<String> field) {
		JTextField component = new JTextField(10);
	    component.setText(field.getValue());
	    component.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
	    	field.setValue(component.getText());
	    });
	    return component;
	}
	
	private <T> JTextField buildTypedTextField(Field<Object> field, Class<T> typeClass) {
		try {
			Constructor<T> fromString = typeClass.getConstructor(String.class);
			JTextField component = new JTextField(10);
			Border defaultBorder = component.getBorder();
		    component.setText(field.getValue() != null ? field.getValue().toString() : null);
		    component.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
		    	T value = null;
		    	String text = component.getText();
		    	try {
		    		if (text != null && !text.trim().isEmpty()) {
						value = fromString.newInstance(text);
		    		}
		    		component.setBorder(defaultBorder);
			    	field.setValue(value);
		    	} catch (Exception ex) {
		    		component.setBorder(new LineBorder(Color.RED));
		    	}
		    });
		    return component;
		} catch (NoSuchMethodException | SecurityException ex) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Component buildFieldComponent(Field<?> field) {
		if (field.getOptions() != null) {
			return buildComboBox((Field<Object>)field);
		} else if (field.getType().equals(Boolean.class)) {
			return buildCheckBox((Field<Boolean>)field);
		} else if (field.getType().equals(String.class)) {
			return buildTextField((Field<String>)field);
		} else {
			return buildTypedTextField((Field<Object>)field, field.getType());
		}
	}
	
	private JPanel buildContent() {
		JPanel form = new JPanel();
		SpringLayout layout = new SpringLayout();
		form.setLayout(layout);
		int n = 0;
		for (Field<?> field : formFields.values()) {
			Component component = buildFieldComponent(field);
			if (component != null) {
			    JLabel label = new JLabel(field.getLabel() + ": ", JLabel.TRAILING);
			    form.add(label);
			    label.setLabelFor(component);
			    form.add(component);
			    n++;
			}
		}
		SpringLayoutUtils.makeCompactGrid(form, n, 2, 5, 5, 5, 5);
		return form;
	}
	
	protected void addExtraActions(JPanel footer) {
		
	}
	
	private JPanel buildFooter() {
		JPanel footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.LINE_AXIS));
		footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		footer.add(Box.createHorizontalGlue());
		addExtraActions(footer);
		JButton create = new JButton();
		create.setAction(new DelegatedAction(e -> {
			if (submit.apply(formFields)) {
				FormWindow.this.dispatchEvent(new WindowEvent(FormWindow.this, WindowEvent.WINDOW_CLOSING));
			}
		}));
		create.setText(button);
		footer.add(create);
		footer.add(Box.createHorizontalGlue());
		return footer;
	}
	
	public void build() {
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(buildContent());
		content.add(buildFooter());
	}

	public FormWindow(String title, String button, Map<String,Field<?>> formFields, Function<Map<String,Field<?>>,Boolean> submit) {
		super(new JFrame(title));
		this.formFields = formFields;
		this.button = button;
		this.submit = submit;
		addWindowListener(new WindowCloseListener(e -> {
			dispose();
		}));
		setResizable(false);
		setLocationRelativeTo(null);
	}

}
