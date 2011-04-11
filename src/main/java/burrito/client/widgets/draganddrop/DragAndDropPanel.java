/**
 * Copyright 2011 Henric Persson (henric.persson@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package burrito.client.widgets.draganddrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class DragAndDropPanel<T> extends SimplePanel {

	private VerticalPanel wrapper = new VerticalPanel();
	private VerticalPanel draggablePanel = new VerticalPanelWithSpacer();
	private DraggableWidgetCreator<T> widgetCreator;
	private AbsolutePanel boundaryPanel;

	public DragAndDropPanel(List<T> data, DraggableWidgetCreator<T> creator) {
		this.widgetCreator = creator;
		boundaryPanel = new AbsolutePanel();
		boundaryPanel.setSize("100%", "100%");
		setWidget(boundaryPanel);

		boundaryPanel.add(wrapper);

		wrapper.add(draggablePanel);
		render(data);
	}

	private void render(List<T> model) {
		PickupDragController dragController = new PickupDragController(
				boundaryPanel, false);
		dragController.setBehaviorDragStartSensitivity(5);
		dragController.setBehaviorMultipleSelection(false);
		dragController.addDragHandler(new SortingPanelDragHandler());

		VerticalPanelDropController dropController = new VerticalPanelDropController(
				draggablePanel);
		dragController.registerDropController(dropController);
		for (T modelObj : model) {
			Widget w = widgetCreator.createWidget(modelObj);
			ObjectWrapper label = new ObjectWrapper(modelObj, w);
			draggablePanel.add(label);
			dragController.makeDraggable(label);
		}
	}

	/**
	 * Called once an object has been (dragged and) dropped.
	 * 
	 * @param newOrder
	 */
	public abstract void onOrderChanged(List<T> newOrder);

	class SortingPanelDragHandler extends DragHandlerAdapter {

		@SuppressWarnings("unchecked")
		@Override
		public void onDragEnd(DragEndEvent event) {
			super.onDragEnd(event);
			List<T> newOrder;
			if (draggablePanel.getWidgetCount() == 0) {
				newOrder = Collections.emptyList();
			} else {
				newOrder = new ArrayList<T>();
				for (int i = 0; i < draggablePanel.getWidgetCount(); i++) {
					try {
						ObjectWrapper sourceObj = (ObjectWrapper) draggablePanel
								.getWidget(i);
						newOrder.add(sourceObj.getWrappedObject());
					} catch (ClassCastException e) {
						// Ignore elements that are not of the correct type.
					}
				}
			}
			onOrderChanged(newOrder);
		}

	}
	
	class ObjectWrapper extends FocusPanel {
		private T wrapped;
		
		public ObjectWrapper(T object, Widget w) {
			super();
			setWidget(w);
			this.wrapped = object;
		}
		
		public T getWrappedObject() {
			return wrapped;
		}
	}

}
