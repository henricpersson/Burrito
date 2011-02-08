package burrito.client.widgets.selection;

import java.util.List;

import burrito.client.widgets.selection.SelectionListLabelCreator;
import burrito.client.widgets.validation.HasValidators;
import burrito.client.widgets.validation.InputFieldValidator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import burrito.client.widgets.messages.CommonMessages;

public class SelectionList<T> extends Composite implements HasChangeHandlers,
		HasValidators {

	private Grid wrapper = new Grid(1, 3);
	private ListBox listBox;
	private List<T> model;
	private SelectionListLabelCreator<T> labelCreator;
	private String nullSelectLabel;
	private boolean required;
	private Label validationError;
	private T waitingToBeSet;
	private Label requiredStar;
	private CommonMessages messages = GWT.create(CommonMessages.class);

	/**
	 * Creates an empty selection list with no values to select from. Using this
	 * constructor is useful if you need to load the model asynchronously. If
	 * you use this constructor you must make sure you call setModel(),
	 * setLabelCreator() and render().
	 * 
	 * @param nullSelectLabel
	 */
	public SelectionList(boolean required) {
		this.required = required;
		listBox = createListBox();
		wrapper.setWidget(0, 0, listBox);
		validationError = new Label();
		validationError.addStyleName("validationError");
		setValidationError(null);
		requiredStar = new Label("*");
		if (required) {
			wrapper.setWidget(0, 1, requiredStar);
		}
		initWidget(wrapper);
		setStyleName("k5-SelectionBox");
	}

	private ListBox createListBox() {
		return new ListBox();
	}

	/**
	 * Creates a selection list which is immediately rendered
	 * 
	 * @param model
	 * @param labelCreator
	 */
	public SelectionList(List<T> model,
			SelectionListLabelCreator<T> labelCreator, boolean required) {
		this.required = required;
		this.model = model;
		this.labelCreator = labelCreator;
		render();
	}

	/**
	 * Sets the model that this selection list works upon
	 * 
	 * @param model
	 */
	public void setModel(List<T> model) {
		this.model = model;
	}

	/**
	 * Sets a label creator on this selection list
	 * 
	 * @param labelCreator
	 */
	public void setLabelCreator(SelectionListLabelCreator<T> labelCreator) {
		this.labelCreator = labelCreator;
	}

	/**
	 * Call this method after having called setModel and setLabelCreator
	 */
	public void render() {
		if (model == null) {
			throw new IllegalStateException(
					"No model set. Make sure you call setModel() before render()");
		}
		if (labelCreator == null) {
			throw new IllegalStateException(
					"No labelCreator set. Make sure you call setLabelCreator() before render()");
		}
		listBox.clear();
		if (nullSelectLabel != null) {
			listBox.addItem(nullSelectLabel);
		}
		for (T obj : model) {
			String label = labelCreator.createLabel(obj);
			listBox.addItem(label);
			String style = labelCreator.getStyleName(obj);
			if (style != null) {
				OptionElement elem = (OptionElement) listBox.getElement()
						.getLastChild();
				elem.setClassName(style);
			}

		}
		if (waitingToBeSet != null) {
			try {
				setValue(waitingToBeSet);
			} finally {
				// if something goes wrong (unexpected exception) we still need
				// to reset the waitingToBeSet value. Otherwise we might end up
				// in a situation where the value can't be changed at all.
				waitingToBeSet = null;
			}
		}
	}

	/**
	 * Gets the value associated with the selected value
	 * 
	 * @return
	 */
	public T getValue() {
		if (!listBox.isEnabled()) {
			return null;
		}
		if (waitingToBeSet != null) {
			// Value has not yet been set
			return waitingToBeSet;
		}
		if (model == null) {
			return null;
		}
		int i = listBox.getSelectedIndex();
		if (nullSelectLabel != null) {
			if (i == 0) {
				return null;
			}
			// the null select label will push down values by one
			i--;
		}
		return model.get(i);
	}

	public void setValue(T obj) {
		if (obj == null) {
			listBox.setSelectedIndex(0);
			return;
		}
		if (model == null) {
			// model has not been set, wait for render()
			waitingToBeSet = obj;
			return;
		}
		int i = model.indexOf(obj);
		if (i == -1) {
			GWT.log("No such object: " + obj.toString());
			return;
		}
		if (nullSelectLabel != null) {
			i++;
		}
		listBox.setSelectedIndex(i);
	}

	@Override
	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return listBox.addChangeHandler(handler);
	}

	/**
	 * Sets the label to display on top of the list, when no value has been
	 * selsected
	 * 
	 * @param nullSelectLabel
	 */
	public void setNullSelectLabel(String nullSelectLabel) {
		this.nullSelectLabel = nullSelectLabel;
	}

	/**
	 * Sets whether this selection list should be disabled or enabled. In
	 * disabled state, all getValue() calls will return null
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		listBox.setEnabled(enabled);
	}

	/**
	 * Returns all values in this selection list
	 * 
	 * @return
	 */
	public List<T> getValues() {
		return model;
	}

	@Override
	public void addInputFieldValidator(InputFieldValidator validator) {
		throw new IllegalStateException(
				"Can not add validators to a file upload panel");
	}

	@Override
	public void setValidationError(String validationMessage) {
		if (validationMessage == null) {
			validationError.setText(null);
			wrapper.clearCell(0, 2);
		} else {
			validationError.setText(validationMessage);
			wrapper.setWidget(0, 2, validationError);
		}
	}

	@Override
	public boolean validate() {
		setValidationError(null);
		if (!required) {
			return true;
		}
		if (this.getValue() == null) {
			setValidationError(messages.validationRequiredField());
			return false;
		}
		return true;
	}
}
