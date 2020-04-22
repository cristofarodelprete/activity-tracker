package it.priestly.activitytracker.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
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
	
	@SuppressWarnings("unchecked")
	private Component buildFieldComponent(Field<?> field) {
		if (field.getOptions() != null) {
			Field<Object> typedField = (Field<Object>)field;
			List<String> options = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			int i = 0;
			Integer selected = null;
			for (Map.Entry<Object,String> entry : typedField.getOptions().entrySet()) {
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
		    		typedField.setValue(null);
		    	} else {
		    		typedField.setValue(values.get(idx));
		    	}
		    });
		    return component;
		} else if (field.getType().equals(Boolean.class)) {
			Field<Boolean> typedField = (Field<Boolean>)field;
			JCheckBox component = new JCheckBox();
			component.setSelected(typedField.getValue() != null ? typedField.getValue() : false);
			component.addChangeListener((SimpleChangeListener) e -> {
		    	typedField.setValue(component.isSelected());
		    });
		    return component;
		} else if (field.getType().equals(BigInteger.class)) {
			Field<BigInteger> typedField = (Field<BigInteger>)field;
			JTextField component = new JTextField(10);
			Border defaultBorder = component.getBorder();
		    component.setText(typedField.getValue() != null ? typedField.getValue().toString() : null);
		    component.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
		    	BigInteger value = null;
		    	String text = component.getText();
		    	try {
		    		if (text != null && !text.trim().isEmpty()) {
		    			value = new BigInteger(text);
		    		}
		    		component.setBorder(defaultBorder);
		    	} catch (NumberFormatException ex) {
		    		component.setBorder(new LineBorder(Color.RED));
		    	}
		    	typedField.setValue(value);
		    });
		    return component;
		} else if (field.getType().equals(BigDecimal.class)) {
			Field<BigDecimal> typedField = (Field<BigDecimal>)field;
			JTextField component = new JTextField(10);
			Border defaultBorder = component.getBorder();
			component.setText(typedField.getValue() != null ? typedField.getValue().toString() : null);
		    component.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
		    	BigDecimal value = null;
		    	String text = component.getText();
		    	try {
		    		if (text != null && !text.trim().isEmpty()) {
		    			value = new BigDecimal(text);
		    		}
		    		component.setBorder(defaultBorder);
		    	} catch (NumberFormatException ex) {
		    		component.setBorder(new LineBorder(Color.RED));
		    	}
		    	typedField.setValue(value);
		    });
		    return component;
		} else if (field.getType().equals(String.class)) {
			Field<String> typedField = (Field<String>)field;
			JTextField component = new JTextField(10);
		    component.setText(typedField.getValue());
		    component.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
		    	typedField.setValue(component.getText());
		    });
		    return component;
		}
		return null;
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
