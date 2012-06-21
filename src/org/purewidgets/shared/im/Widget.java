/**
 * package doc widget
 */
package org.purewidgets.shared.im;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.purewidgets.shared.events.ActionEvent;
import org.purewidgets.shared.events.ActionListener;
import org.purewidgets.shared.events.WidgetInputEvent;
import org.purewidgets.shared.events.WidgetInputListener;
import org.purewidgets.shared.events.ReferenceCodeListener;
import org.purewidgets.shared.logging.Log;


/**
 * 
 * This code (as all code in the shared package) is shared between the server and client.
 * Some functions only make sense for the server, other functions only make sense
 * because of the client, but putting them together lets us reuse much code.
 * <p>
 *
 * A widget has a <code>widgetId</code> that identifies the widget in the application (so it
 * must be a unique id within the application) and one or more <code>WidgetOptions</code>
 * that correspond to items of the widget that can be separately selected by the
 * user. 
 * Each <code>WidgetOption</code> will be assigned a reference code by the interaction
 * manager service. Reference codes are the human-readable references used by users to
 * select an option or a widget for input. When the set of reference codes
 * assigned to a widget changes (because the application created a new widget with the 
 * same widgetid, for example) the InteractionManager will notify the widget by calling
 * onReferenceCodesUpdated().
 * 
 * Widgets can have (and usually will) ActionListeners attached. An application
 * registers ActionListeners so that it will be notified of any event generated by the 
 * widget (usually in response to user input). 
 * 
 * Widgets may also have an InputListener. This works in a kind of Chain of Responsability 
 * pattern: If the Widget has an InputListener it will forward any input to this listener
 * and will not try to handle it or generate ActionEvents. This allows the client code
 * to register a GuiWidget as an InputListener for the base widget so that it (the GuiWidget)
 * will be responsible for handling input and generating the ActionEvents.
 * The same happens with the ReferenceCodeListener.
 * 
 * Subclasses of Widget can override the handleReferenceCodesUpdate() method to deal
 * with changed in the widget's reference codes although this will usually not be of
 * interest for the server part of the application. The best way to deal with changes in 
 * the reference codes in on the GuiWidget's subclass. 
 * 
 * Subclasses of Widget can override the handleInput() to check for the validity of
 * the input and trigger an ActionEvent (ideally through the triggerActionEvent() method).
 *
 * 
 * TODO: See if this is still necessary: 
 * On the server, Widgets are persisted so that an application can know which widgets it has so
 * subclasses should be annotated if they need to save properties. 
 * 
 * 
 * @author Jorge C. S. Cardoso
 * 
 */

@JsonAutoDetect(fieldVisibility=Visibility.ANY, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE)
public class Widget implements Comparable<Widget> {

	public static String CONTROL_TYPE_IMPERATIVE_SELECTION = "imperative_selection";
	public static String CONTROL_TYPE_ENTRY = "entry";
	public static String CONTROL_TYPE_UPLOAD = "upload";
	public static String CONTROL_TYPE_DOWNLOAD = "download";
	public static String CONTROL_TYPE_PRESENCE = "presence";
	//TODO: composite


	/**
	 * The placeId where the application that has this widget is running.
	 * Applications don't usually have to deal directly with this.
	 */
	private String placeId;

	/**
	 * The applicationId of the application that has this widget. Applications
	 * don't usually have to deal directly with this.
	 */
	private String applicationId;

	/**
	 * The id of this widget.
	 */

	protected String widgetId;
	
	
	/**
	 * The type of control that this widget implements
	 */
	private String controlType;
	

	/**
	 * A short description (label) for the widget. The descriptions
	 * can be used to generate a more informative GUI by other system applications.
	 *
	 */
	private String shortDescription;
	
	/**
	 * A long description for the widget. The descriptions
	 * can be used to generate a more informative GUI by other system applications.
	 */

	private String longDescription;
	
	
	/**
	 * The list of options of this widget
	 */
	protected ArrayList<WidgetOption> widgetOptions = new ArrayList<WidgetOption>();

	/**
	 * The list of parameters for this widget.
	 */
	private ArrayList<WidgetParameter> widgetParameters;
	
	/**
	 * The ActionListeners registered to receive high-level events from this
	 * widget.
	 */

	@JsonIgnore
	protected List<ActionListener> actionListeners = new ArrayList<ActionListener>();

	/**
	 * The InputListener for this widget. 
	 */

	@JsonIgnore
	protected WidgetInputListener inputListener;

	/**
	 * The ReferenceCodeListener for this widget. Widgets are themselves
	 * ReferenceCodeListeners but also propagate reference codes to other
	 * listeners (e.g. GuiWidgets)
	 */
	@JsonIgnore
	protected ReferenceCodeListener referenceCodeListener;

	
	/**
	 * A Widget can be composed by other widgets.
	 */
	@JsonIgnore
	protected ArrayList<Widget> dependentWidgets;


	protected Widget() {
		actionListeners = new ArrayList<ActionListener>();
		widgetOptions = new ArrayList<WidgetOption>();
		this.dependentWidgets = new ArrayList<Widget>();
		this.widgetParameters = new ArrayList<WidgetParameter>();
	}
	
	/**
	 * Creates a new Widget with the specified widgetId and WidgetOptions. The widgetId should generally not be null. If it is, 
	 * the hash of the object will be used as the id but this will prevent applications from identifying widgets
	 * from their id (on the server applications need to use the id, because the widget object will not be the 
	 * same across sessions). If the options is null, one WidgetOptions will be created with the same id as the widget's id.
	 * 
	 * 
	 * @param widgetId The id for the widget.
	 * @param options The list of WidgetOptions.
	 */
	public Widget(String widgetId, String shortDescription, String longDescription, ArrayList<WidgetOption> options) {
		this(widgetId, CONTROL_TYPE_IMPERATIVE_SELECTION, shortDescription, longDescription, options, null);
	}

	
	/**
	 * Creates a widget and registers it on the interaction manager service.
	 * 
	 * @param widgetId
	 * @param options
	 * @param autoSendToServer
	 * 
	 */
	public Widget(String widgetId, String controlType, String shortDescription, String longDescription, ArrayList<WidgetOption> options, ArrayList<WidgetParameter> parameters) {
		this();
		
		this.controlType = controlType;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		
		this.setWidgetId(widgetId);

		if (options == null) {
			WidgetOption wo = new WidgetOption(widgetId);
			this.addWidgetOption(wo);
		} else {
			this.setWidgetOptions(options);
		}
		
		/*
		 * Make sure widgetparameters is never null
		 */
		if ( null != parameters ) {
			this.widgetParameters = parameters;
		}

	}

	
	
	public void addActionListener(ActionListener handler) {
		actionListeners.add(handler);
	}
	
	public void addDependentWidget( Widget widget ) {
		this.dependentWidgets.add(widget);
	}
	
	/**
	 * Adds an option to this widget. This change is propagated immediately to
	 * the WidgetManager.
	 * 
	 * @see org.instantplaces.purewidgets.shared.widgets.WidgetInterface#addWidgetOption(org.instantplaces.purewidgets.shared.widgets.WidgetOptionInterface)
	 */
	public void addWidgetOption(WidgetOption option) {
		
		if (!this.widgetOptions.contains(option)) {
			this.widgetOptions.add(option);
		}
		
	}

	public String getApplicationId() {
		return applicationId;
	}

	public ArrayList<Widget> getDependentWidget() {
		return this.dependentWidgets;
	}
	


	public String getPlaceId() {
		return placeId;
	}

	/**
	 * @return The id of the widget.
	 * 
	 */
	public String getWidgetId() {
		return this.widgetId;
	}

	/**
	 * Returns the list of options of this widget.
	 * 
	 * @return The ArrayList of WidgetOption.
	 * @see org.instantplaces.purewidgets.shared.widgets.WidgetInterface#getWidgetOptions()
	 */
	public ArrayList<WidgetOption> getWidgetOptions() {
		return this.widgetOptions;
	}


	/**
	 * 
	 * @see org.purewidgets.shared.events.WidgetInputListener#onInput(org.purewidgets.shared.events.WidgetInputEvent)
	 */
	public final void onInput(ArrayList<WidgetInputEvent> ie) {
		Log.debugFinest(this, "Received event: " + ie);
		
		// You want null? You can't handle null...
		if (ie == null) {
			return;
		}

		/*
		 * On the client, input is handled by the GuiWidget so we cascade the input to the InputListener (which is set to the GuiWidget).
		 * 
		 * On the server we just ask the subclass to handle the input by calling handleInput(). The handleInput() implementation in this
		 * class just triggers an ActionEvent. Subclasses may want to override this to validate input and create specific ActionEvents.
		 */
		if (null != this.inputListener) {
			/*
			 * On the client, cascade to the GuiWidget
			 */
			this.inputListener.onInput(ie);
			
		} 
		//else {
			/*
			 * On the server, handle the input
			 */
			this.handleInput(ie);
		//}
	}

	/**
	 * 
	 * @see org.purewidgets.shared.events.ReferenceCodeListener#onReferenceCodesUpdated()
	 */
	public final void onReferenceCodesUpdated() {
		Log.debugFinest(this, "Received onReferenceCodesUpdated");
		
		/*
		 * Cascade the input to the input listener for possible feedback
		 */
		if (null != this.referenceCodeListener) {
			Log.debugFinest(this, "Cascading onReferenceCodesUpdated to "
					+ this.referenceCodeListener);
			this.referenceCodeListener.onReferenceCodesUpdated();
		}  else {
			this.handleReferenceCodesUpdate();
		}

	}

	public void removeActionListener(ActionListener handler) {
		actionListeners.remove(handler);

	}

	public void removeDependentWidget( Widget widget ) {
		this.dependentWidgets.remove( widget );
	}

	
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@JsonIgnore
	public void setInputListener(WidgetInputListener inputListener) {
		this.inputListener = inputListener;

	}



	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	@JsonIgnore
	public void setReferenceCodeListener(
			ReferenceCodeListener referenceCodeListener) {
		this.referenceCodeListener = referenceCodeListener;
		

	}


	/**
	 * @param widgetID
	 */
	private void setWidgetId(String id) {
		/**
		 * A widget has always an unique ID within the application. If the user
		 * didn't pass one, one is generated automatically.
		 */
		if (id == null) {
			this.widgetId = String.valueOf(this.hashCode());
			Log.warn(this, "Null id, assigning object hashCode: " + this.widgetId);
		} else {
			this.widgetId =  id;//com.google.gwt.http.client.URL.decode(id);
		}
	}
	

	/**
	 * Sets this widget's options to the specified ArrayList. The existing
	 * options are discarded. This change is propagated immediately to the
	 * WidgetManager.
	 * 
	 * @see org.instantplaces.purewidgets.shared.widgets.WidgetInterface#setWidgetOptions(java.util.ArrayList)
	 */
	protected void setWidgetOptions(ArrayList<WidgetOption> options) {
		this.widgetOptions = options;
	}

	

	/**
	 * Just a helper method that sends an ActionEvent to all ActionListeners registered.
	 * 
	 * @param ae The ActionEvent to send
	 */
	public final void fireActionEvent(ActionEvent<?> ae) {
		for (ActionListener handler : actionListeners) {
			handler.onAction(ae);
		}
	}


	/**
	 * Handles the received input. Concrete widgets should override this to
	 * validate input.
	 * 
	 * @param ie
	 */
	protected void handleInput(ArrayList <WidgetInputEvent> ie) {

		ActionEvent<Widget> ae;
		
		for (WidgetInputEvent inputEvent : ie) {
			ae = new ActionEvent<Widget>(inputEvent, this, null);
			
			this.fireActionEvent(ae);
		}
		

		
	}


	/**
	 * Concrete widgets may override this if they need to handle updated on the
	 * reference codes.
	 */
	protected void handleReferenceCodesUpdate() {
		//Log.debug(this, "handleReferenceCodesUpdate");
	}


	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}


	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}


	/**
	 * @return the longDescription
	 */
	public String getLongDescription() {
		return longDescription;
	}


	/**
	 * @param longDescription the longDescription to set
	 */
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}


	


	/**
	 * @return the controlType
	 */
	public String getControlType() {
		return controlType;
	}




	@Override
	public int compareTo(Widget other) {
		return this.widgetId.compareTo(other.getWidgetId());
	}

	/**
	 * @return the widgetParameters
	 */
	public ArrayList<WidgetParameter> getWidgetParameters() {
		return widgetParameters;
	}



}
