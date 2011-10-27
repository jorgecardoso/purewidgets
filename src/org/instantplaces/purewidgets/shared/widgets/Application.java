/**
 * 
 */
package org.instantplaces.purewidgets.shared.widgets;

import java.util.ArrayList;

/**
 * @author "Jorge C. S. Cardoso"
 *
 */
public class Application {

	private String placeId;
	
	private String applicationId;
	
	private ArrayList<Widget> widgets;

	public Application(String placeId, String appId) {
		this.placeId = placeId;
		this.applicationId = appId;
	}
	/**
	 * @return the placeId
	 */
	public String getPlaceId() {
		return placeId;
	}

	/**
	 * @param placeId the placeId to set
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	/**
	 * @return the appId
	 */
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * @param appId the appId to set
	 */
	public void setApplicationId(String appId) {
		this.applicationId = appId;
	}

	/**
	 * @return the widgets
	 */
	public ArrayList<Widget> getWidgets() {
		return widgets;
	}

	/**
	 * @param widgets the widgets to set
	 */
	public void setWidgets(ArrayList<Widget> widgets) {
		this.widgets = widgets;
	}
	
	
	
}