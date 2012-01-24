/**
 *
 */
package org.instantplaces.purewidgets.server.widgetmanager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


import javax.jdo.PersistenceManager;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.instantplaces.purewidgets.client.json.GenericJson;
import org.instantplaces.purewidgets.client.widgetmanager.ClientServerCommunicator;
import org.instantplaces.purewidgets.client.widgetmanager.json.WidgetListJson;
import org.instantplaces.purewidgets.server.RemoteStorage;
import org.instantplaces.purewidgets.server.interactionmanager.InteractionServiceImpl;
import org.instantplaces.purewidgets.shared.Log;
import org.instantplaces.purewidgets.shared.exceptions.InteractionManagerException;
import org.instantplaces.purewidgets.shared.widgetmanager.ServerCommunicator;
import org.instantplaces.purewidgets.shared.widgetmanager.ServerListener;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetInput;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetInputList;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetList;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetManager;
import org.instantplaces.purewidgets.shared.widgets.Widget;

import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * @author Jorge C. S. Cardoso
 *
 */
public class ServerServerCommunicator implements ServerCommunicator {	
	
	// TODO: We should verify that widgets were really added to the InteractionManager,
	// just like in the clientServeCommunicator. 
	// We should keep a (persistent) list of widgets to add and only remove  a widget
	// from this list when we received a confirmation from the server.
	
	/**
	 * The address of the InteractionManager server.
	 */
	private static final String INTERACTION_SERVER = "http://im-instantplaces.appspot.com";



	
	/**
	 * The name of the name/value pair that stores the last input time stamp
	 * received from the server. 
	 */
	private static final String TIMESTAMP_NAME = "lastTimeStamp";

	/**
	 * The applicationId on which this ServerCommunicator will be used.
	 */
	private  String appId = "jorge";
	
	/**
	 * The application URL constructed with the INTERACTION_SERVER, placeId and appId
	 */
	private  String applicationUrl;
	
	
	private InteractionServiceImpl interactionService;
	
	
	/**
	 * The placeId on which this ServerCommunicator will be used. 
	 */
	private  String placeId = "dsi";

	private RemoteStorage remoteStorage;

	/**
	 * The ServerListener that will receive server events (i.e. the WidgetManager)
	 *
	 */
	private ServerListener serverListener;

	public ServerServerCommunicator(PersistenceManager pm, RemoteStorage remoteStorage, String placeId, String appId) {
		this.placeId = placeId;
		this.appId = appId;
		this.applicationUrl = INTERACTION_SERVER +	"/place/" + placeId + "/application/"+ appId;
		
		interactionService = new InteractionServiceImpl();
		this.remoteStorage = remoteStorage;// RemoteStorage.get();
	}

	/* (non-Javadoc)
	 * @see org.instantplaces.purewidgets.shared.widgetmanager.ServerCommunicator#addWidget(org.instantplaces.purewidgets.shared.widgets.WidgetInterface)
	 */
	@Override
	public void addWidget(Widget widget) {
		//WidgetRepresentation widgetRepresentation = WidgetRepresentation.fromWidget(widget);
		//widgetRepresentation.applicationId = APP;
		//widgetRepresentation.placeId = PLACE;
		widget.setApplicationId(appId);
		widget.setPlaceId(placeId);

		WidgetList wl = new WidgetList();
		ArrayList<Widget> widgets = new ArrayList<Widget>();
		widgets.add(widget);
		wl.setWidgets(widgets);
		
		ObjectMapper mapper = new ObjectMapper();
		String json = null;

		try {
			json = mapper.writeValueAsString(wl);//widgetRepresentation);
		} catch (JsonGenerationException e1) {
			Log.error(e1.getMessage());
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			Log.error(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			Log.error(e1.getMessage());
			e1.printStackTrace();
		}

		/*WidgetJSON widgetJSON = WidgetJSON.create(widget);
		widgetJSON.setApplicationId(APP);
		widgetJSON.setPlaceId(PLACE);*/
		Log.debug("Adding " + json + " to server");
		String response = null;
		try {
			response = interactionService.postWidget(json,
					WidgetManager.getWidgetsUrl(this.placeId, this.appId, this.appId) );
		} catch (InteractionManagerException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
		Log.debug(response);

		wl = null;
		try {
			
			 wl = mapper.readValue(response, WidgetList.class);
			for ( Widget w : wl.getWidgets() ) {
				if ( null != this.serverListener ) {
					Log.debug(widget.toDebugString());
					this.serverListener.onWidgetAdd(w);
				}
			}
		} catch (JsonParseException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
		// read response and call serverlistener widget add
	}
	
	
	/**
	 * Checks input from the InteractionManager service
	 */
	public void askForInputFromServer() {
		ObjectMapper mapper = new ObjectMapper();
	
			String lastTimeStamp = this.getLastTimeStampAsString();
			if ( null == lastTimeStamp ) {
				lastTimeStamp = "0";
			}
			String url = applicationUrl + "/input?output=json&from=" + lastTimeStamp + "&appid="+appId;
			
			Log.warn("Contacting application server for input..." + url);
			String response = null;
			
			try {
				response = interactionService.get(url);
			} catch (InteractionManagerException e) {
				Log.error( e.getMessage());
				e.printStackTrace();
				return;
			}
			Log.warn(response);
			
			

			try {
				WidgetInputList inputList = mapper.readValue(response, WidgetInputList.class);
				/*
				 * Update our most recent input timeStamp so that in the next round we ask only
				 * for newer input
				 */
				for (WidgetInput widgetInput : inputList.getInputs() ) {
					/*
					 * Save the new timeStamp locally
					 */
					if (toLong(widgetInput.getTimeStamp()) > this.getLastTimeStampAsLong()) {
						this.setTimeStamp(toLong(widgetInput.getTimeStamp()));
					}
				}
				
				/*
				 * Notify the widgetManager
				 */
				if (this.serverListener != null) {
					this.serverListener.onWidgetInput(inputList.getInputs());
				}
			} catch (JsonParseException e) {
				Log.error(e.getMessage());
				e.printStackTrace();
			} catch (JsonMappingException e) {
				Log.error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.error(e.getMessage());
				e.printStackTrace();
			} 	
	}
	
	
	@Override
	public void deleteAllWidgets(boolean volatileOnly) {
		// TODO Auto-generated method stub
		
	}


	/*
	 private String getBaseURL(Widget w) {
		   return  "/place/" + w.getPlaceId() + "/application/" + w.getApplicationId() + 
		   	"/widget";
	   }*/
	   
	   /*private  String getURL(Widget w) {
		   return  getBaseURL(w) + "/"+ w.getWidgetId() + "?output=json";
	   }*/
	   
	   
	/* (non-Javadoc)
	 * @see org.instantplaces.purewidgets.shared.widgetmanager.ServerCommunicator#deleteWidget(org.instantplaces.purewidgets.shared.widgets.WidgetInterface)
	 */
	@Override
	public void deleteWidget(Widget widget) {
		Log.debug(this, "Removing widget:" + widget.getWidgetId() );
		
		/*
		 * Create the URL for the DELETE method. Widget ids are passed on the 
		 * 'widget' url parameter
		 */
		StringBuilder widgetsUrlParam = new StringBuilder();
		widgetsUrlParam.append( WidgetManager.getWidgetsUrl(this.placeId, this.appId, this.appId) ).append("&widgets=");
		
		
		try {
			widgetsUrlParam.append(URLEncoder.encode(widget.getWidgetId(), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			Log.error("Could not URLencode." + e1.getMessage());
			e1.printStackTrace();
			return;
		}
			
		String response;
		
		try {
			response = interactionService.deleteWidget(widgetsUrlParam.toString());
			
		} catch (Exception e) {
			Log.error( e.getMessage() );
			e.printStackTrace();
			return;
		}
		Log.warn(response);
		
	
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			WidgetList widgetList = mapper.readValue(response, WidgetList.class);
			
			for ( Widget w : widgetList.getWidgets() ) {
				/*
				 * Notify the widgetManager
				 */
				if (this.serverListener != null) {
					this.serverListener.onWidgetDelete(w);
				}
			}
		} catch (JsonParseException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} 	
		
		

	}

	
	
	/**
	 * Enables or disables the automatic input requests
	 * @param automatic
	 */
	@Override
	public void setAutomaticInputRequests(boolean automatic) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.instantplaces.purewidgets.shared.widgetmanager.ServerCommunicator#setServerListener(org.instantplaces.purewidgets.shared.widgetmanager.ServerListener)
	 */
	@Override
	public void setServerListener(ServerListener listener) {
		this.serverListener = listener;

	}
	
	private long getLastTimeStampAsLong() {
		try {
			return Long.parseLong(getLastTimeStampAsString());
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
		return 0;
	}
	
	private String getLastTimeStampAsString() {
		return remoteStorage.getString(TIMESTAMP_NAME);
	}	
	

	private void setTimeStamp(long timeStamp) {
		Log.debug("Storing timestamp: " + timeStamp);
		
		remoteStorage.setString(TIMESTAMP_NAME, ""+timeStamp);
	}

	private long toLong(String value) {
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public void getWidgetsList(String placeId, String applicationId) {
		ObjectMapper mapper = new ObjectMapper();
		
		
		String url = WidgetManager.getWidgetsUrl(placeId, applicationId, this.appId);
		
		Log.warn("Contacting application server for input..." + url);
		String response = null;
		
		try {
			response = interactionService.get(url);
		} catch (InteractionManagerException e) {
			Log.error( e.getMessage());
			e.printStackTrace();
			return;
		}
		Log.warn(response);
	

		try {
			WidgetList widgetList = mapper.readValue(response, WidgetList.class);
			
			/*
			 * Notify the widgetManager
			 */
			if (this.serverListener != null) {
				this.serverListener.onWidgetsList(placeId, applicationId, widgetList.getWidgets());
			}
		} catch (JsonParseException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		} 	
		
	}

	@Override
	public void getApplicationsList(String placeId, boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getApplicationsList(String placeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPlacesList() {
		// TODO Auto-generated method stub
		
	}	
}
