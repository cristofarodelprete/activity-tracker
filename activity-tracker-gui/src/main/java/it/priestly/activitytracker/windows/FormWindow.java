package it.priestly.activitytracker.windows;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import it.priestly.activitytracker.utils.DelegatedAction;
import it.priestly.activitytracker.utils.SimpleDocumentListener;
import it.priestly.activitytracker.utils.SpringLayoutUtils;
import it.priestly.activitytracker.utils.WindowCloseListener;

public class FormWindow extends JDialog {
	private static final long serialVersionUID = -8330419378178165024L;

	private Map<String,String> formLabels;
	
	private Map<String,String> formValues;
	
	private Function<Map<String,String>,Boolean> submit;

	private String button;
	
	private JPanel buildContent() {
		JPanel form = new JPanel();
		SpringLayout layout = new SpringLayout();
		form.setLayout(layout);
		for (Map.Entry<String,String> entry : formLabels.entrySet()) {
		    JLabel l = new JLabel(entry.getValue() + ": ", JLabel.TRAILING);
		    form.add(l);
		    JTextField textField = new JTextField(10);
		    textField.setText(formValues.get(entry.getKey()));
		    textField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
		    	formValues.put(entry.getKey(), textField.getText());
		    });
		    l.setLabelFor(textField);
		    form.add(textField);
		}
		SpringLayoutUtils.makeCompactGrid(form, formLabels.size(), 2, 5, 5, 5, 5);
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
			if (submit.apply(formValues)) {
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

	public FormWindow(String title, String button, Map<String,String> formLabels, Function<Map<String,String>,Boolean> submit) {
		this(title, button, formLabels, null, submit);
	}

	public FormWindow(String title, String button, Map<String,String> formLabels, Map<String,String> formValues, Function<Map<String,String>,Boolean> submit) {
		super(new JFrame(title));
		this.formLabels = formLabels;
		this.formValues = formValues != null ? formValues : new HashMap<String,String>();
		this.button = button;
		this.submit = submit;
		addWindowListener(new WindowCloseListener(e -> {
			dispose();
		}));
		setResizable(false);
		setLocationRelativeTo(null);
	}

}
