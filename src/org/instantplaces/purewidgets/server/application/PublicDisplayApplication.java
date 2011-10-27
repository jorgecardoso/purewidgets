/**
 * 
 */
package org.instantplaces.purewidgets.server.application;

import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.servlet.http.HttpServletRequest;
import org.instantplaces.purewidgets.server.PMF;
import org.instantplaces.purewidgets.server.RemoteStorage;
import org.instantplaces.purewidgets.server.widgetmanager.ServerServerCommunicator;
import org.instantplaces.purewidgets.shared.Log;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetManager;
import org.instantplaces.purewidgets.shared.widgets.Widget;
import com.google.appengine.api.datastore.Key;

/**
 * @author Jorge C. S. Cardoso
 *
 */
@PersistenceCapable
public class PublicDisplayApplication  {

	/**
	 * The URL query string parameter name that holds the application id
	 */
	private static final String APP_NAME_PARAMETER = "appname";
	
	/**
	 * The URL query string parameter name that holds the place id
	 */
	private static final String PLACE_NAME_PARAMETER = "placename";	
	
	/**
	 * If the URL does not contain the application id, this application id will be used as default
	 */
	private static final String DEFAULT_APP_NAME = "DefaultApplication";

	/**
	 * If the URL does not contain the application id, this application id will be used as default
	 */
	private static final String DEFAULT_PLACE_NAME = "DefaultPlace";
	
	
	@SuppressWarnings("unused")
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	/**
	 * The application id.
	 */
	@Persistent
	private String appId;
	
	/**
	 * The place id.
	 */
	@Persistent
	private String placeId;
	
	@Persistent
	private RemoteStorage remoteStorage;
	
	/**
	 * The PersistenceManager used to access the datastore.
	 */
	private PersistenceManager persistenceManager;
	
	/**
	 * The ApplicationLifeCycle object.
	 */
	private ApplicationLifeCycle applicationLifeCycle;
	
	/**
	 * The ServerCommunicator object used to communicate with the InteractionManager.
	 */
	private ServerServerCommunicator ssCommunicator;
	
	private boolean firstTime;
	
	protected PublicDisplayApplication (String placeId, String appId, PersistenceManager pm, ApplicationLifeCycle acl) {
		this.placeId = placeId;
		this.appId = appId;
		this.persistenceManager = pm;
		this.applicationLifeCycle = acl;
		remoteStorage = new RemoteStorage();
	}
	
	
	private void init() {
		Log.info(this, "Initing application " + this.appId);
		
		//WidgetManager.get().setWidgetList(remoteStorage.loadWidgets(this.applicationLifeCycle, persistenceManager));
		
		
		ssCommunicator = new ServerServerCommunicator(persistenceManager, this.remoteStorage, this.placeId, this.appId);
		WidgetManager.get().setServerCommunication(ssCommunicator);
	}
	
	public void run() {
		Log.info(this, "Running application " + this.appId);
	
    	/*
    	 * Setup is called once for each application only.
    	 */
		if (this.firstTime) {
			Log.debug(this, "Running application for the first time");
			this.applicationLifeCycle.setup();
		}
    	
		this.applicationLifeCycle.start();
		
		ssCommunicator.askForInputFromServer();
		
		
		this.applicationLifeCycle.finish();
		//remoteStorage.saveWidgets(WidgetManager.get().getWidgetList(), persistenceManager);
		persistenceManager.makePersistent(this);
		
		persistenceManager.close();
	}
	
	public static void load(HttpServletRequest req, ApplicationLifeCycle acl) {
		Log.debug(PublicDisplayApplication.class.getCanonicalName(), "Loading application ");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		String appId = req.getParameter(APP_NAME_PARAMETER);
		if (null == appId) {
			appId = DEFAULT_APP_NAME;
			Log.warn(PublicDisplayApplication.class.getCanonicalName(), "Could not read '"+APP_NAME_PARAMETER+"' parameter from query string. Using default appname.");
		}
		Log.debug(PublicDisplayApplication.class.getCanonicalName(), "Using application name: " + appId);
		
		String placeId = req.getParameter(PLACE_NAME_PARAMETER);
		if (null == placeId) {
			placeId = DEFAULT_PLACE_NAME;
			Log.warn(PublicDisplayApplication.class.getCanonicalName(), "Could not read '"+PLACE_NAME_PARAMETER+"' parameter from query string. Using default placename.");
		}
		Log.debug(PublicDisplayApplication.class.getCanonicalName(), "Using place name: " + placeId);
				
		
		// Load application
		Query query = pm.newQuery(PublicDisplayApplication.class);
		query.setFilter("appId == appIdParam && placeId == placeIdParam");
		query.declareParameters("String appIdParam, String placeIdParam");
		
	    PublicDisplayApplication application = null;
	    try {
	        
			@SuppressWarnings("unchecked")
			List<PublicDisplayApplication> results = (List<PublicDisplayApplication>) query.execute(appId, placeId);
	        
	        if (!results.isEmpty()) {
	        	application = results.get(0);
	        	Log.info(PublicDisplayApplication.class.getCanonicalName(), "Loaded application " + application.appId);

	        	if (results.size() > 1) {
	        		Log.warn(PublicDisplayApplication.class.getCanonicalName(), "But found more matching applications in the DS");
	        		for (PublicDisplayApplication app : results) {
	        			Log.warn(PublicDisplayApplication.class.getCanonicalName(), "Application: " + app.appId + " was also in the DS");
	        		}
	        	}
	        	application.firstTime = false;
	        	application.setPersistenceManager(pm);
	        	application.setApplicationLifeCycle(acl);
	        	application.init();
	        	
	        } else {
	        	Log.debug(PublicDisplayApplication.class.getCanonicalName(), "Application not found. Creating new.");
	        	application = new PublicDisplayApplication(placeId, appId, pm, acl);
	        	application.firstTime = true;
	        	application.setPersistenceManager(pm);
	        	application.setApplicationLifeCycle(acl);
	        	application.init();
	        	

	        }
	    } catch (Exception e) {
	    	Log.error(PublicDisplayApplication.class.getCanonicalName(), "Could not access data store.");
	    	e.printStackTrace();
	    }  finally {
	        query.closeAll();
	    }	    
	    acl.loaded(application);
	    application.run();
		//return application;
	}
	
	/**
	 * Saves a name/value pair in the DS. If the name already exists, its value will be
	 * replaced by the new one. If not, a new pair is created.
	 * 
	 * @param name The name to store.
	 * @param value The value to store.
	 */
	public void setString(String name, String value) {
		this.remoteStorage.setString(name, value);
	}

	/**
	 * Retrieves a value from the DS, given its name.
	 * 
	 * @param name The name of the value to retrieve.
	 * @return The value associated with the name, or null if the name does not exist.
	 */
	public String getString(String name) {
		return this.remoteStorage.getString(name);
	}
	
	public void setLong(String name, long value) {
		this.remoteStorage.setLong(name, value);
	}
	
	public long getLong(String name) {
		return this.remoteStorage.getLong(name);
	}
	
	public void addWidget(Widget w) {
		WidgetManager.get().addWidget(w);
	}

	private void setPersistenceManager(PersistenceManager pm) {
		this.persistenceManager = pm;
	}

	@SuppressWarnings("unused")
	private PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	private void setApplicationLifeCycle(ApplicationLifeCycle applicationLifeCycle) {
		this.applicationLifeCycle = applicationLifeCycle;
	}

	@SuppressWarnings("unused")
	private ApplicationLifeCycle getApplicationLifeCycle() {
		return applicationLifeCycle;
	}
	
	
	
}