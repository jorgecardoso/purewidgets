/**
 * 
 */
package org.jorgecardoso.purewidgets.demo.placeinteraction.client;

import java.util.Date;
import org.instantplaces.purewidgets.client.application.PublicDisplayApplication;
import org.instantplaces.purewidgets.client.application.PublicDisplayApplicationLoadedListener;
import org.instantplaces.purewidgets.shared.Log;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetManager;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.UiType;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.application.ApplicationListUi;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.place.PlaceListUi;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.widget.WidgetListUi;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author "Jorge C. S. Cardoso"
 * 
 */
public class PlaceInteractionWebpage implements PublicDisplayApplicationLoadedListener, EntryPoint {

	public static String userIdentity = "Anonymous1984";

	public static SightingServiceAsync sightingService;
	
	public static LoginInfo loginInfo = null;
	
	private Anchor signInLink = new Anchor("Sign In");
	
	private Label labelId;
	
	private PlaceListUi placeListUi;
	
	private ApplicationListUi applicationListUi;
	
	private WidgetListUi widgetListUi;
	
	private UiType uiType;

	@Override
	public void onModuleLoad() {
		/*
		 * Check/set the user's identity.
		 * 
		 * We generate a random identity name, starting with "Anonymous" and set
		 * a cookie with it so that we can retrieve it later if the user uses
		 * the webpage again.
		 */
		userIdentity = Cookies.getCookie("userIdentity");
		if (null == userIdentity) {
			userIdentity = "Anonymous" + ((int) (Math.random() * 10000));
			/*
			 * The cookie is valid for one week
			 */
			Date future = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000); // 7
																							// days
																							// in
																							// the
																							// future
			Cookies.setCookie("userIdentity", userIdentity, future);
		}
		
		/*
		 * Put the user id at the bottom of the page, and a link so that the
		 * user can sign in
		 */
		labelId = new Label("Your identity is " + userIdentity);
		RootPanel.get("user").add(labelId);
		RootPanel.get("user").add(signInLink);

		/*
		 * Create the sightinh service to post input to instant places
		 */
		sightingService = GWT.create(SightingService.class);
		((ServiceDefTarget) sightingService).setServiceEntryPoint("/sighting");

		PublicDisplayApplication.load(this, "PlaceInteractionWebpage", false);
		
		WidgetManager.get().setAutomaticInputRequests(false);
		this.uiType = UiType.Desktop;
	}

	/**
	 * Show a different screen based on the history token
	 * @param historyToken
	 */
	protected void show(String historyToken) {
		if (null == historyToken || historyToken.length() == 0) {
			this.showPlaces();
		} else {
			int indexOfDash = historyToken.indexOf("-");
		    if (  indexOfDash < 0 ) { // no dash: this is a place name
				this.showApps( historyToken );
			} else  {
				String placeName = historyToken.substring(0, indexOfDash);
				String applicationName = historyToken.substring(indexOfDash+1);
				this.showWidgets(placeName, applicationName);
			}
		}
	}

	/**
	 *  Show the place list screen
	 */
	private void showPlaces() {
		Log.debug(this, "Showing places");
		if ( null != this.applicationListUi ) {
			this.applicationListUi.stop();
		}
		if ( null != this.widgetListUi ) {
			this.widgetListUi.stop();
		}
		if ( null == this.placeListUi ) {
			this.placeListUi = new PlaceListUi(this.uiType); // TODO: Check UiType
			this.placeListUi.addSelectionHandler(new SelectionHandler<String>() {
				@Override
				public void onSelection(SelectionEvent<String> event) {
					PlaceInteractionWebpage.this.onPlaceSelected(event.getSelectedItem());
				}
			});
		}
		RootPanel.get("features").clear();
		RootPanel.get("features").add(this.placeListUi);
		this.placeListUi.start();
	}

	
	/**
	 * Show the application list (and widgets) screen
	 */
	private void showApps(final String placeId) {
		Log.debug(this, "Showing applications");
		if ( null != this.placeListUi ) {
			this.placeListUi.stop();
		} 
		if ( null != this.widgetListUi ) {
			this.widgetListUi.stop();
		}
		
		if ( null == this.applicationListUi || !this.applicationListUi.getPlaceId().equals(placeId) ) {
			this.applicationListUi = new ApplicationListUi(this.uiType, placeId); // TODO: Check the uiType
			this.applicationListUi.addSelectionHandler(new SelectionHandler<String>() {
				@Override
				public void onSelection(SelectionEvent<String> event) {
					PlaceInteractionWebpage.this.onApplicationSelected(placeId, event.getSelectedItem());
				}
			});
		}
		RootPanel.get("features").clear();
		RootPanel.get("features").add(this.applicationListUi);
		this.applicationListUi.start();
	}

	/**
	 * Show the widget list for a given application
	 */
	private void showWidgets( String placeName, String applicationName) {
		Log.debug(this, "Showing widgets");
		if ( null != this.placeListUi ) {
			this.placeListUi.stop();
		} 
		if ( null != this.applicationListUi ) {
			this.applicationListUi.stop();
		}
		if ( null == this.widgetListUi || !this.widgetListUi.getPlaceName().equals(placeName) || !this.widgetListUi.getApplicationName().equals(applicationName) ) {
			this.widgetListUi = new WidgetListUi(this.uiType, placeName, applicationName); // TODO: Check the uiType
		}
		RootPanel.get("features").clear();
		RootPanel.get("features").add(this.widgetListUi);
		this.widgetListUi.start();
	}

	
	

	public void onPlaceSelected(String placeId) {
		Log.debug(this, "User selected place " + placeId);
		History.newItem(placeId);
	}
	
	public void onApplicationSelected(String placeName, String applicationName) {
		Log.debug(this, "User selected application" + applicationName);
		
		History.newItem(placeName+"-"+applicationName);
	}

	@Override
	public void onApplicationLoaded() {

		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String historyToken = event.getValue();
				show(historyToken);
			}
		});

		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
			@Override
			public void onFailure(Throwable error) {
				Log.debug(this, "error; " + error.getMessage());
			}

			@Override
			public void onSuccess(LoginInfo result) {
				loginInfo = result;
				Log.debug(this, loginInfo.getLoginUrl());
				if (loginInfo.isLoggedIn()) {
					Log.debug(this, loginInfo.getEmailAddress());
					userIdentity = loginInfo.getEmailAddress();
					//todo: remove this
					int at = userIdentity.indexOf('@');
					String toReplace = userIdentity.substring(at-5, at);
					userIdentity = userIdentity.replaceFirst(toReplace, "...");
					labelId.setText("Your identity is: " + userIdentity);
					signInLink.setText("Sign out");
					signInLink.setHref(loginInfo.getLogoutUrl());
				} else {
					signInLink.setText("Sign in");
					signInLink.setHref(loginInfo.getLoginUrl());

				}
			}
		});
		
		this.show(History.getToken());
		
	}


	

}
