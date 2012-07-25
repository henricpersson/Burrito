package burrito.client.crud.widgets;

import java.util.List;

import burrito.client.crud.CrudServiceAsync;
import burrito.client.crud.generic.CrudEntityDescription;
import burrito.client.crud.generic.CrudEntityList;
import burrito.client.crud.generic.fields.ManyToOneRelationField;
import burrito.client.crud.labels.CrudLabelHelper;
import burrito.client.crud.labels.CrudMessages;
import burrito.client.widgets.panels.table.PageMetaData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchSelectWidget extends DialogBox {

	private static CrudMessages labels = GWT.create(CrudMessages.class);
	
	private List<CrudEntityDescription> items;
	private SelectHandler onSelectHandler;
	
	private FlexTable searchResult;
	private TextBox searchBox;
	
	//Delayed search to fix server hammering.
	private Timer searchTimer;

	private final CrudServiceAsync service;
	private final ManyToOneRelationField relationField;
	
	public SearchSelectWidget(CrudServiceAsync service, ManyToOneRelationField relationField) {
		this.service = service;
		this.relationField = relationField;
		
		String relatedEntityName = relationField.getRelatedEntityName();
		final String entityDisplayName = CrudLabelHelper.getStringEntityNameSingular(relatedEntityName).toLowerCase();

		setText(labels.selectOrChange(entityDisplayName));
		
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(new Label(labels.searchListHelpText(entityDisplayName)));

		
		searchResult = new FlexTable();
		searchResult.addStyleName("searchResult");
		searchResult.addClickHandler(new ClickHandler() {
		    public void onClick(ClickEvent event) {
		        Cell cell = searchResult.getCellForEvent(event);
		        int receiverRowIndex = cell.getRowIndex();
		        
		        CrudEntityDescription item = items.get(receiverRowIndex);
		        
		        if (onSelectHandler != null) {
		        	onSelectHandler.onSelect(item.getId());
		        }
		    }
		});
		
		searchBox = new TextBox();
		searchBox.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				final String searchValue = searchBox.getValue();
				
				if (searchValue.length() < 3) {
					searchResult.removeAllRows();
					return;
				}
				
				if (searchTimer != null) {
					searchTimer.cancel();
					searchTimer = null;
				}
				
				searchTimer = new Timer() {
					@Override
					public void run() {
						searchFor(searchValue);
					}
				};
				searchTimer.schedule(500);
			}
		});
		searchBox.addStyleName("k5-SearchSelectWidget-searchbox");
		
		Button cancelButton = new Button(labels.cancel());
		cancelButton.addStyleName("cancelButton");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		verticalPanel.add(searchBox);
		FlowPanel searchResultWrapper = new FlowPanel();
		searchResultWrapper.addStyleName("searchResultWrapper");
		searchResultWrapper.add(searchResult);
		verticalPanel.add(searchResultWrapper);
		verticalPanel.add(cancelButton);
		
		setWidget(verticalPanel);
		addStyleName("searchListSelect");
	}

	private void searchFor(String searchValue) {
		PageMetaData<String> p = new PageMetaData<String>();
		p.setItemsPerPage(1000000);
		p.setSortKey(relationField.getSearchSortField());
		
		service.listEntities(searchValue, relationField.getRelatedEntityName(), p , new AsyncCallback<CrudEntityList>() {
			
			@Override
			public void onSuccess(CrudEntityList result) {
				searchResult.removeAllRows();
				
				items = result.getItems();
				for (CrudEntityDescription item : items) {
					String displayString = item.getDisplayString();
					
					int count = searchResult.getRowCount();
					searchResult.setText(count, 0, displayString);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
			}
		});
	}
	
	public interface SelectHandler {
		public void onSelect(Long id);
	}
	
	public void setSelectHandler(SelectHandler onSelectHandler) {
		this.onSelectHandler = onSelectHandler;
	}

	public void focus() {
		this.searchBox.setFocus(true);
	}
	
	
}
