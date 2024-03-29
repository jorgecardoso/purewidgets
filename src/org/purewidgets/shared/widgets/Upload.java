package org.purewidgets.shared.widgets;

import java.util.ArrayList;

import org.purewidgets.shared.events.ActionEvent;
import org.purewidgets.shared.events.WidgetInputEvent;
import org.purewidgets.shared.im.Widget;

/**
 * An Upload widget that allows users to send files to an application.
 * 
 * @author "Jorge C. S. Cardoso"
 *
 */
public class Upload  extends Widget {
	
	/**
	 * Creates a new Upload widget with the given id and label.
	 * 
	 * @param widgetId the widget id.
	 * @param label the label for the widget.
	 */
	public Upload(String widgetId, String label) {
		/*
		 * Use the label as the short description
		 */
		super(widgetId, Widget.CONTROL_TYPE_UPLOAD, label, "", null, null);
	}
	
	/**
	 * Handles input directed at this widget.
	 */
	@Override
	public void handleInput(ArrayList<WidgetInputEvent> inputEventList) {
		
		/*
		 * If the upload received a file, trigger an application event
		 */
		for (WidgetInputEvent ie : inputEventList) {
			if ( null != ie.getParameters() && ie.getParameters().size() > 0) {
				ActionEvent<Upload> ae = new ActionEvent<Upload>(ie, this, ie.getParameters().get(0) );
				this.fireActionEvent(ae);
			} 
		}
	}
}
