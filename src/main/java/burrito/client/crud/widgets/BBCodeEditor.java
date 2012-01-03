package burrito.client.crud.widgets;


import burrito.client.crud.widgets.SelectableTextArea.SelectedText;
import burrito.client.widgets.services.BBCodeService;
import burrito.client.widgets.services.BBCodeServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BBCodeEditor extends VerticalPanel  {

	private TabPanel tabPanel = new TabPanel();
	
	private VerticalPanel rawPanel = new VerticalPanel();
	private HTML preview = new HTML();
	
	private HorizontalPanel buttonPanel = new HorizontalPanel();
	private SelectableTextArea rawEditor = new SelectableTextArea();
	
	private BBCodeServiceAsync bbCodeService = GWT.create(BBCodeService.class);
	
	public BBCodeEditor(String value) {
		initButtons();
		this.rawEditor.setText(value);
	}
	
	public BBCodeEditor() {
		initButtons();
	}
	
	private void initButtons() {
		Button buttonBold = new Button("Bold");
		buttonBold.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SelectedText selectedText = rawEditor.getSelectedTextObj();
				selectedText.text = "[b]" + selectedText.text + "[/b]";
				rawEditor.setSelectedText(selectedText);
			}
		});
		buttonPanel.add(buttonBold);
		
		Button buttonItalic = new Button("Italic");
		buttonItalic.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SelectedText selectedText = rawEditor.getSelectedTextObj();
				selectedText.text = "[i]" + selectedText.text + "[/i]";
				rawEditor.setSelectedText(selectedText);
			}
		});
		buttonPanel.add(buttonItalic);
		
		Button buttonImg = new Button("Bild");
		buttonImg.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ImagePickerPopup imagePicker = new ImagePickerPopup(800, 800, false);
				imagePicker.center();
				imagePicker.show();
				imagePicker.addSaveHandler(new ImagePickerPopup.SaveHandler() {
					
					public void saved(String value) {
						SelectedText selectedText = rawEditor.getSelectedTextObj();
						selectedText.text = "[img]" + value + "[/img]";
						rawEditor.setSelectedText(selectedText);
					}
				});
			}
		});
		buttonPanel.add(buttonImg);

		Button buttonUrl = new Button("Url");
		buttonUrl.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				BBUrlPanel urlPanel = new BBUrlPanel(rawEditor);
				urlPanel.show();
			}
		});
		buttonPanel.add(buttonUrl);
		
		Button buttonYoutube = new Button("Youtube");
		buttonYoutube.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				 BBYoutubePanel youtubePanel = new BBYoutubePanel(rawEditor);
				 youtubePanel.show();
			}
		});
		buttonPanel.add(buttonYoutube);
		
		
		rawPanel.add(buttonPanel);
		rawPanel.add(rawEditor);
		
		preview.setSize("960px", "700px");
		rawEditor.setSize("960px", "700px");
		tabPanel.setSize("960px", "700px");
		
		tabPanel.add(rawPanel, "RAW");
		tabPanel.add(preview, "Preview");
		tabPanel.selectTab(0);
		
		tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				
				bbCodeService.generateBBCodePreview(rawEditor.getText(), new AsyncCallback<String>() {
					@Override
					public void onSuccess(String html) {
						preview.setHTML(html);					
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Could not create preview! " + caught.getMessage());
					}
				});
			}
		});
		
		add(tabPanel);
	}

	public void setText(String value) {
		this.rawEditor.setText(value);
	}

	public String getText() {
		return this.rawEditor.getText();
	}
}
