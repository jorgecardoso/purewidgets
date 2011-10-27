package org.instantplaces.purewidgets.shared.widgetmanager;

import org.instantplaces.purewidgets.shared.widgets.Widget;




public interface ServerCommunicator {

	public void addWidget(Widget widget);
	public void deleteWidget(Widget widget);
	
	public void getPlaceApplicationsList(boolean active);
	public void getPlaceApplicationsList();
	
	public void setServerListener(ServerListener listener);
	
	/**
	 * Enables or disables the automatic input requests
	 * @param automatic
	 */
	public void setAutomaticInputRequests(boolean automatic);
	
	
}