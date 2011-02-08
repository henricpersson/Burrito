package burrito.client.widgets.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burrito.client.widgets.layout.VerticalSpacer;
import burrito.client.widgets.validation.HasValidators;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class EditForm extends Composite {

	

	public static interface LoadedCallback {
		/**
		 * Called when a job has finished
		 */
		void done();
	}

	public static interface SaveCallback {
		/**
		 * The save operation succeeded
		 */
		void success();

		void partialSuccess(String warning);

		/**
		 * The save operation failed for some reason
		 * 
		 * @param message
		 *            an error message
		 */
		void failed(String message);

	}

	public static interface SaveCancelListener {
		/**
		 * Called when the cancel button is pressed
		 */
		void onCancel();

		/**
		 * Called when the save button has been pressed and a succesful response
		 * has been received
		 */
		void onSave();

		/**
		 * Called when the save button has been pressed and there was a partial
		 * fail
		 * 
		 * @param warning
		 */
		void onPartialSave(String warning);

	}

	static class InfoMessage extends Label {

		public InfoMessage() {
			super();
			addStyleName("k5-InfoMessage");
		}

		@Override
		public void setText(String text) {
			super.setText(text);
			if (text == null) {
				setVisible(false);
			} else {
				setVisible(true);
			}
		}
	}

	private EditFormMessages messages = GWT.create(EditFormMessages.class);
	private DockPanel dock = new DockPanel();
	private VerticalPanel main = new VerticalPanel();
	private DeckPanel wrapper = new DeckPanel();
	private List<HasValidators> validateables = new ArrayList<HasValidators>();
	private Label loading = new Label(messages.loading());
	private Button save = new Button(messages.save());
	private Button cancel = new Button(messages.cancel());
	private InfoMessage infoMessage = new InfoMessage();
	private SaveCancelListener saveCancelListener;
	private List<EditFormChangeHandler> changeHandlers;
	private HashMap<Widget,List<Widget>> companionWidgetsMap = new HashMap<Widget,List<Widget>>();
	private KeyDownHandler saveEnablerKeyDownAction = new KeyDownHandler() {

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_TAB) {
				return;
			}
			handleChange();
		}
	};
	private ChangeHandler saveEnablerChangeHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent event) {
			handleChange();
		}
	};
	private ClickHandler saveEnablerClickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			handleChange();
		}
	};

	private boolean newForm = false;

	public EditForm() {
		dock.add(main, DockPanel.CENTER);
		HorizontalPanel hp = new HorizontalPanel();
		// start with save button disabled
		save.setEnabled(false);
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				save();
			}
		});
		cancel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				saveCancelListener.onCancel();
			}
		});
		hp.add(save);
		hp.add(cancel);
		dock.add(hp, DockPanel.SOUTH);
		dock.add(infoMessage, DockPanel.NORTH);
		wrapper.add(dock);
		wrapper.add(loading);
		wrapper.showWidget(0);
		initWidget(wrapper);
	}

	/**
	 * Gets the save button
	 * 
	 * @return
	 */
	public Button getSaveButton() {
		return save;
	}

	/**
	 * Gets the cancel button
	 * 
	 * @return
	 */
	public Button getCancelButton() {
		return cancel;
	}

	/**
	 * Sets the cancel listener for this form
	 * 
	 * @param cancelListener
	 */
	public void setSaveCancelListener(SaveCancelListener cancelListener) {
		this.saveCancelListener = cancelListener;
	}

	/**
	 * Loads this edit form from an id
	 * 
	 * @param id
	 * @param loadedCallback
	 */
	public abstract void doLoad(String id, LoadedCallback loadedCallback);

	public final void load(String id) {
		save.setEnabled(false);
		for (HasValidators v : validateables) {
			v.setValidationError(null);
		}
		infoMessage.setText(null);
		// show loading widget
		wrapper.showWidget(1);
		if (id == null) {
			doLoadNew();
			wrapper.showWidget(0);
			DeferredCommand.addCommand(new Command() {

				@Override
				public void execute() {
					focus();
					save.setEnabled(true);
				}
			});
		} else {
			doLoad(id, new LoadedCallback() {

				@Override
				public void done() {
					wrapper.showWidget(0);
					focus();
					save.setEnabled(true);
				}
			});
		}
	}

	public abstract void doLoadNew();

	protected void save() {
		infoMessage.setText(null);
		for (HasValidators v : validateables) {
			if (!v.validate()) {
				return;
			}
		}
		save.addStyleName("saving");
		save.setEnabled(false);
		SaveCallback callback = new SaveCallback() {

			@Override
			public void success() {
				save.removeStyleName("saving");
				// infoMessage.setText(messages.yourChangesHaveBeenSaved());
				if (saveCancelListener != null) {
					saveCancelListener.onSave();
				}
			}

			@Override
			public void partialSuccess(String warning) {
				save.setEnabled(true);
				save.removeStyleName("saving");
				if (saveCancelListener != null) {
					saveCancelListener.onPartialSave(warning);
				}
			}

			@Override
			public void failed(String message) {
				save.setEnabled(true);
				save.removeStyleName("saving");
				infoMessage.setText(messages.anErrorHasOccured(message));
			}
		};
		if (newForm) {
			doSaveNew(callback);
		} else {
			doSave(callback);
		}

	}

	/**
	 * Called when the save button has been clicked. Be sure to calls
	 * callback.done() when the asynchronous action has been performed. This
	 * method is called when all validatable fields ({@link HasValidators}) have
	 * been validated.
	 * 
	 * @param saveCallback
	 */
	public abstract void doSave(SaveCallback saveCallback);

	/**
	 * Called when the save button is pressed in an {@link EditForm} that
	 * handles a new object
	 * 
	 * @param saveCallback
	 */
	public abstract void doSaveNew(SaveCallback saveCallback);

	public void add(Widget widget, String label, String description) {
		if (widget instanceof HasValidators) {
			validateables.add((HasValidators) widget);
		}
		if (widget instanceof HasKeyDownHandlers) {
			((HasKeyDownHandlers) widget)
					.addKeyDownHandler(saveEnablerKeyDownAction);
		}
		if (widget instanceof HasChangeHandlers) {
			((HasChangeHandlers) widget)
					.addChangeHandler(saveEnablerChangeHandler);
		}
		if (widget instanceof CheckBox) {
			((CheckBox) widget).addClickHandler(saveEnablerClickHandler);
		}

		List<Widget> companionWidgets = new ArrayList<Widget>();

		if (label != null) {
			Label l = new Label(label);
			l.addStyleName("k5-EditForm-label");
			if (!widget.isVisible()) l.setVisible(false);
			main.add(l);
			companionWidgets.add(l);
		}

		if (description != null) {
			Label desc = new Label(description);
			desc.addStyleName("k5-EditForm-description");
			if (!widget.isVisible()) desc.setVisible(false);
			main.add(desc);
			companionWidgets.add(desc);
		}

		main.add(widget);

		VerticalSpacer spacer = new VerticalSpacer(10);
		main.add(spacer);
		companionWidgets.add(spacer);

		companionWidgetsMap.put(widget, companionWidgets);
	}

	public void setWidgetVisible(Widget widget, boolean visible) {
		widget.setVisible(visible);
		for (Widget companionWidget : companionWidgetsMap.get(widget)) {
			companionWidget.setVisible(visible);
		}
	}

	/**
	 * Method called when the form is loaded and visible. Normally, the first
	 * input field in the form is focused.
	 */
	public abstract void focus();

	public void loadNew() {
		newForm = true;
		load(null);
	}

	/**
	 * Clears all fields
	 */
	public void clear() {
		main.clear();
		validateables.clear();
	}

	/**
	 * Adds a value change handler that receives events whenever one of the
	 * components within this {@link EditForm} has changed its value.
	 */
	public void addEditFormChangeHandler(
			EditFormChangeHandler handler) {
		if (changeHandlers == null) {
			changeHandlers = new ArrayList<EditFormChangeHandler>();
		}
		changeHandlers.add(handler);
	}

	private void handleChange() {
		if (!save.isEnabled()) {
			save.setEnabled(true);
		}
		if (changeHandlers != null) {
			for (EditFormChangeHandler h : changeHandlers) {
				h.onChange();
			}
		}
	}

}
