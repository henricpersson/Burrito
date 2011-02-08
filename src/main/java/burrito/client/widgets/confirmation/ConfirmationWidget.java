package burrito.client.widgets.confirmation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public abstract class ConfirmationWidget extends Composite {

	protected HorizontalPanel wrapper = new HorizontalPanel();
	protected Anchor startLink = new Anchor();
	protected Label confirmationMessage = new Label();
	protected Anchor confirmLink = new Anchor();
	protected Anchor cancelLink = new Anchor();
	protected Label separator = new Label();

	public ConfirmationWidget() {
		wrapper.addStyleName("k5-ConfirmationWidget-wrapper");
		startLink.addStyleName("k5-ConfirmationWidget-startlink");
		startLink.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				initConfirmationDialog();			
			}

		});
		confirmationMessage.addStyleName("k5-ConfirmationWidget-confirmationmessage");
		confirmLink.addStyleName("k5-ConfirmationWidget-confirm");
		confirmLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				confirmAction();
			}
		});
		cancelLink.addStyleName("k5-ConfirmationWidget-cancel");
		cancelLink.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				initInitialDialog();
			}
		});
		
		separator.addStyleName("k5-ConfirmationWidget-separator");
	}
	
	public void setStartLinkText(String text) {
		startLink.setText(text);
	}
	
	public void setConfirmationMessage(String text) {
		confirmationMessage.setText(text);
	}
	
	public void setConfirmLinkText(String text){
		confirmLink.setText(text);
	}
	
	public void setSeparatorText(String text) {
		separator.setText(text);
	}
	
	public void setCancelLinkText(String text){
		cancelLink.setText(text);
	}

	
	private void initConfirmationDialog() {
		wrapper.clear();
		wrapper.add(confirmationMessage);
		wrapper.add(confirmLink);
		wrapper.add(separator);
		wrapper.add(cancelLink);
	}

	private void initInitialDialog(){
		wrapper.clear();
		wrapper.add(startLink);
	}
	
	public abstract void confirmAction();

	public void render() {
		initInitialDialog();
		initWidget(wrapper);
	}
}
