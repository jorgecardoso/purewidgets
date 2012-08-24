package org.purewidgets.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.purewidgets.client.application.PDApplication;
import org.purewidgets.client.feedback.CumulativeInputFeedbackPanel;
import org.purewidgets.client.feedback.FeedbackDisplay;
import org.purewidgets.client.feedback.FeedbackSequencer;
import org.purewidgets.client.feedback.InputFeedback;
import org.purewidgets.client.feedback.InputFeedbackListener;
import org.purewidgets.client.feedback.MessagePattern;
import org.purewidgets.client.im.WidgetManager;
import org.purewidgets.client.storage.LocalStorage;
import org.purewidgets.shared.events.ActionEvent;
import org.purewidgets.shared.events.ActionListener;
import org.purewidgets.shared.events.WidgetInputEvent;
import org.purewidgets.shared.events.WidgetInputListener;
import org.purewidgets.shared.events.ReferenceCodeListener;
import org.purewidgets.shared.im.Widget;
import org.purewidgets.shared.im.WidgetOption;
import org.purewidgets.shared.logging.Log;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Composite;

/**
 * 

* @author Jorge C. S. Cardoso
*
 */
public class PdWidget extends Composite implements  WidgetInputListener, ReferenceCodeListener, InputFeedbackListener {
	
	private static final String REFERENCE_CODES_STORAGE_ID = "referenceCodes";
	private static final int INPUT_EVENT_OLD_AGE = 1000*60*5; // 5 minutes
	protected static final String DEPENDENT_STYLENAME_DISABLED_WIDGET = "disabled";

	/**
	 * The default pattern to apply to generate the input feedback message.
	 * Consists simply on showing the user's nickname.
	 * 
	 * Available fields:
	 * %U% - user nickname
	 * %P[i]% - Input parameter i
	 * %WS% - widget short description
	 * %WL% - widget long description
	 * %WOS% - widget option short description
	 * %WOL% - widget option long description
	 * %WOR% - widget option reference code
	 */
	protected final String DEFAULT_USER_INPUT_FEEDBACK_PATTERN = "%U%";
	protected final String DEFAULT_USER_SHARED_TITLE_INPUT_FEEDBACK_PATTERN = "%U%";
	protected final String DEFAULT_USER_SHARED_INFO_INPUT_FEEDBACK_PATTERN = "%WS%";
	/**
	 * The widget that supports this guiwidget.
	 */
	protected Widget widget;
	
	protected List<ActionListener> actionListeners = new ArrayList<ActionListener>();

	/**
	 * The panel used to display user input info feedback.
	 */
	protected CumulativeInputFeedbackPanel inputFeedbackDisplay;

	/**
	 * The feedback sequencer for internal feedback.
	 */
	private FeedbackSequencer feedbackSequencer;

	/**
	 * The pattern to apply to generate the input feedback message.
	 */
	private String userInputFeedbackPattern = DEFAULT_USER_INPUT_FEEDBACK_PATTERN;

	/**
	 * The pattern to apply to generate the input feedback message.
	 */
	private String userSharedTitleInputFeedbackPattern = DEFAULT_USER_SHARED_TITLE_INPUT_FEEDBACK_PATTERN;
	
	/**
	 * The pattern to apply to generate the input feedback message.
	 */
	private String userSharedInfoInputFeedbackPattern = DEFAULT_USER_SHARED_INFO_INPUT_FEEDBACK_PATTERN;
	
	
	
	/**
	 * The visual state of the widget.
	 */
	protected boolean enabled;

	/**
	 * The input state of the widget
	 * TODO: Merge with the enabled flag
	 */
	protected boolean inputEnabled = true;
	
	
	private LocalStorage localStorage;
	private String widgetId;
	
	/**
	 * Subclasses that implement widgets with several options should not call
	 * this constructor. Although subclasses can call super(widgetID), this
	 * should be avoided because it would mean calling the addWidget twice on
	 * the InteractionManager.
	 * 
	 * @param widgetID
	 *            The id of this widget (must be unique within the application).
	 * @param suggestedReferenceCode
	 *            The suggested reference code
	 */
	public PdWidget(String widgetId) {
		this.widgetId = widgetId;
		this.inputFeedbackDisplay = new CumulativeInputFeedbackPanel(this);
		this.feedbackSequencer = new FeedbackSequencer(this.inputFeedbackDisplay, this);
		this.localStorage = new LocalStorage(PDApplication.getCurrent().getApplicationId()+"-"+this.widgetId);
	}

	public boolean isDisplaying() {
		com.google.gwt.user.client.ui.Widget current = this;
		try { // we were getting javascript exceptions (Cannot read property 'display' of undefined) 
			//in development mode, hence the try-catch:
			  
			/*
			 * If the widget is not on the DOM than it is not visible.
			 */
			if ( !current.isAttached() ) {
				return false;
			}
			
			/*
			 * If any ancestor is not visible than this widget is not visible either
			 */
			while ( current != null ) {
				if ( !current.isVisible() ) {
					return false;
				}
				current = current.getParent();
			}
		} catch (Exception e) { 
			Log.warn(this, "Could not determine visibility status", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Adds an ActionListener to this widget. ActionListeners will be notified
	 * of any actions triggered by this widget, usually in response to user input.
	 * 
	 * @param handler The ActionListener to add.
	 */
	public void addActionListener(ActionListener handler) {
		
		this.actionListeners.add(handler);
	}
	

	/**
	 * Adds an optionID to this widget.
	 * 
	 * @param optionID The widgetOption to add.
	 */
	public void addWidgetOption(WidgetOption option) {
		this.widget.addWidgetOption(option);
	}
	
	/**
	 * 
	 * @return The widget's bottom y coordinate
	 */
	public int getBottom() {
		return this.getElement().getAbsoluteBottom();
	}

	public FeedbackSequencer getFeedbackSequencer() {
		return feedbackSequencer;
	}
	
	
	/**
	 * 
	 * @return The height of the widget, in pixels.
	 */
	public int getHeight() {
		return this.getOffsetHeight();
	}	

	/**
	 * 
	 * @return The widget's leftmost x coordinate
	 */
	public int getLeft() {
		return this.getElement().getAbsoluteLeft();
	}
	

	

	
	/**
	 * 
	 * @return The widget's rightmost x coordinate
	 */
	public int getRight() {
		return this.getElement().getAbsoluteRight();
	}

	/**
	 * 
	 * @return The widget's top y coordinate
	 */
	public int getTop() {
		return this.getElement().getAbsoluteTop();
	}
	
	/**
	 * Returns this widget's id. There is no setId() because the id of a
	 * widget cannot be changed after it is set in the constructor.
	 * 
	 * 
	 * @return This widget's WidgetID.
	 * @see org.instantplaces.purewidgets.shared.widgets.WidgetInterface#getId()
	 */
	public String getWidgetId() {
		return this.widget.getWidgetId();
	}

	/**
	 * Returns the list of options of this widget.
	 * 
	 * @return The ArrayList of WidgetOption.
	 */
	public ArrayList<WidgetOption> getWidgetOptions() {
		return this.widget.getWidgetOptions();
	}

	/**
	 * 
	 * @return The width of the widget, in pixels.
	 */
	public int getWidth() {
		return this.getOffsetWidth();
	}
	
	protected InputFeedback<? extends PdWidget> handleInput(WidgetInputEvent ie) {
		InputFeedback<PdWidget> feedback = new InputFeedback<PdWidget>(this, ie);
		feedback.setType(InputFeedback.Type.ACCEPTED);
		
		ActionEvent<PdWidget> ae = new ActionEvent<PdWidget>(ie, this, null);
		feedback.setActionEvent(ae);
		this.generateUserInputFeedbackMessage(ie, feedback);
		return feedback;
	}
	
	/**
	 * Called by the FeedbackSequencer to notify that a feedback display has ended.
	 * 
	 * This method will always be called with the same InputFeedback that
	 * the previous start method and will always after an inputFeedbackStarted call.
	 * 
	 * @param feedback The feedback to stop showing. 
	 */
	@Override
	public final void inputFeedbackEnded(InputFeedback<? extends PdWidget> feedback, boolean noMore) {
		
	}
	
	/**
	 * Triggers the processing of the specified InputFeedback.
	 * 
	 * @param feedback The feedback to process.
	 */
	@Override
	public final void inputFeedbackStarted(InputFeedback<? extends PdWidget> feedback) {
		Log.debugFinest(this, "Input feedback started: " + feedback.toString());
		
		/*
		 * If feedback for an accepted input has started, trigger the
		 * application event.
		 */
		if (feedback.getType() == InputFeedback.Type.ACCEPTED) {			
				this.fireActionEvent(feedback.getActionEvent()); // widget specific object
		}
	}
	
	/**
	 * Returns the visual state of the widget.
	 * 
	 * @return The state of the widget.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public boolean isInputEnabled() {
		return this.inputEnabled;
	}
	

	
	
	
	/**
	 * Event triggered by the InteractionManager to signal
	 * input to this widget.
	 * 
	 * 
	 * @param ie An InputEvent object.
	 */
	@Override
	public final void onInput(ArrayList<WidgetInputEvent> inputList) {
		Log.debugFinest(this, "Received input event list ");
		
		for ( WidgetInputEvent ie : inputList ) {
			
			
			/*
			 * If the input is old, just trigger the application event
			 */
			if ( ie.getAge() > INPUT_EVENT_OLD_AGE ) {
				/*
				 * Ask the widget to validate the input and generate an appropriate feedback
				 */
				InputFeedback<? extends PdWidget> feedback = handleInput(ie);
				this.fireActionEvent( feedback.getActionEvent() );
				
				
			/*
			 * If the input is recent, generate feedback for it
			 */
			} else {
				
				InputFeedback<? extends PdWidget> feedback = new InputFeedback<PdWidget>(this, ie);
				if( this.isInputEnabled() ) {
					
					/*
					 * If this widget is enabled than ask it to handle the input, validating it 
					 */
					feedback = handleInput(ie);
				} else {
					/*
					 * If the widget is not enabled than the input is not accepted.
					 */
					feedback.setType(InputFeedback.Type.NOT_ACCEPTED);
				}
			
				/*
				 * Schedule the feedback to appear. Widgets may return null as an indication that no feedback should be displayed
				 */
				if ( null != feedback ) {
					
					this.feedbackSequencer.add(feedback);
					
				}
			}
		}

	}

	
	
	@Override
	public void widgetVisibilityChanged() {
//		Log.debug(this, "Widget visibility changed, transfering feedback.");
//		/*
//		 * Transfer the feedback to the bottom panel
//		 */
//		this.feedbackSequencer.stop();
//		for ( InputFeedback inputfeedback : this.feedbackSequencer.getInput() ) {
//			sharedFeedbackSequencer.add(inputfeedback);
//		}
//		this.feedbackSequencer.clear();
	}
	
	/**
	 * Concrete widgets should override this method to update their Gui with the correct reference codes.
	 */
	@Override
	public void onReferenceCodesUpdated() {
		
		ArrayList<String> refCodes = new ArrayList<String>();
		for ( WidgetOption widgetOption : this.widget.getWidgetOptions() ) {
			refCodes.add(widgetOption.getReferenceCode());
		}
		this.getLocalStorage().saveList(REFERENCE_CODES_STORAGE_ID, refCodes);
	}
	
	
	/**
	 * Removes an ActionListener from this widget.
	 * 
	 * @param handler The ActionListener to remove.
	 */
	public void removeActionListener(ActionListener handler) {
		this.actionListeners.remove(handler);
	}
	
	public void removeFromServer() {
		Log.debugFinest(this, "Removing widget from widgetmanager: " + this);
		WidgetManager.get().removeWidget(this.widget);
		
		this.localStorage.removeItem(REFERENCE_CODES_STORAGE_ID);
		
		for (Widget w : this.widget.getDependentWidget() ) {
			Log.debugFinest(this, "Removing dependent widgets from widgetmanager: " + w);
			WidgetManager.get().removeWidget(w);
		}
	
	}

	
	

	
	

	
	public final void sendToServer() {
		Log.debugFinest(this, "Adding widget to widgetmanager: " + this.getWidgetId());
		WidgetManager.get().addWidget(this.widget);
		
		for (Widget w : this.widget.getDependentWidget() ) {
			Log.debugFinest(this, "Adding dependent widgets to widgetmanager: " + w.getWidgetId());
			WidgetManager.get().addWidget(w);
		}
		
	}
	
	
	//
//	/**
//	 * @param alignment
//	 */
//	public void setInputFeedbackPanelAlignment(Align alignment) {
//		this.inputFeedbackPanelAlignment = alignment;
//	}
//
//	/**
//	 * @return the inputInfoPanelAlignment
//	 */
//	protected Align getInputFeedbackPanelAlignment() {
//		return inputFeedbackPanelAlignment;
//	}
//
//	/**
//	 * @param inputFeedbackPanelReferencePoint
//	 *            the inputInfoPanelReferencePoint to set
//	 */
//	protected void setInputFeedbackPanelReferencePoint(
//			Align inputFeedbackPanelReferencePoint) {
//		this.inputFeedbackPanelReferencePoint = inputFeedbackPanelReferencePoint;
//	}
//
//	/**
//	 * @return the inputInfoPanelReferencePoint
//	 */
//	protected Align getInputFeedbackPanelReferencePoint() {
//		return inputFeedbackPanelReferencePoint;
//	}
//
//	/**
//	 * @param inputFeedbackDisplay
//	 *            the inputInfoPanel to set
//	 */
//	public void setInputFeedbackDisplay(FeedbackDisplay inputFeedbackDisplay) {
//		this.inputFeedbackDisplay = inputFeedbackDisplay;
//	}
//
//	/**
//	 * @return the inputInfoPanel
//	 */
//	public FeedbackDisplay getInputFeedbackDisplay() {
//		return inputFeedbackDisplay;
//	}
//
//	/**
//	 * Sets the auto-disable feature to disable input automatically after an
//	 * input has been received. Input will be disabled during the specified
//	 * number of seconds. After that it will be enabled again.
//	 * 
//	 * @param seconds
//	 *            The number of milliseconds during which input should be
//	 *            disabled. A value of 0 (zero) disables the auto-disable
//	 *            feature.
//	 */
//	public void setAutoDisable(int milliseconds) {
//		this.autoDisable = milliseconds;
//	}
//
//	/**
//	 * Returns the duration of the auto-disable feature.
//	 * 
//	 * @return The duration of the auto-disable feature in milliseconds. 0 means
//	 *         it is not enabled.
//	 */
//	public int getAutoDisable() {
//		return this.autoDisable;
//	}
//
//	/**
//	 * @param feedbackSequencer
//	 *            the feedbackSequencer to set
//	 */
//	public void setFeedbackSequencer(FeedbackSequencer feedbackSequencer) {
//		this.feedbackSequencer = feedbackSequencer;
//	}
//
//	/**
//	 * @return the feedbackSequencer
//	 */
//	public FeedbackSequencer getFeedbackSequencer() {
//		return feedbackSequencer;
//	}
//
	/**
	 * Sets the visual feedback of the widget to enabled or disabled. This
	 * settings affects only the visual state. The widget may still receive
	 * input even if its visual state indicates otherwise.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			this.removeStyleDependentName(PdWidget.DEPENDENT_STYLENAME_DISABLED_WIDGET);// .setStyleName(this.getStyleName());
		} else {
			this.getWidget().addStyleDependentName(PdWidget.DEPENDENT_STYLENAME_DISABLED_WIDGET); //.setStyleName(					this.getStyleName() + this.DISABLED_STYLENAME_SUFFIX);
		}
	}
	
	
	public void setFeedbackSequencer(FeedbackSequencer feedbackSequencer) {
		this.feedbackSequencer = feedbackSequencer;
	}
	
	/**
	 * Enables or disables this Widget's processing of InputEvents. If disabled,
	 * the Widget will no longer fire ActionEvents.
	 * 
	 * Subclasses should obey this rule and call fireActionEvent (which does
	 * this checking) to fire ActionEvents to their listeners.
	 * 
	 * @param inputEnabled
	 */
	public void setInputEnabled(boolean inputEnabled) {

		this.inputEnabled = inputEnabled;

		/*
		 * If we're showing feedback don't mess with it...
		 */
		if (!this.feedbackSequencer.isShowing()) {
			this.setEnabled(inputEnabled);
		}
		// this.hideInfoPanel();
	}


	
	/*
	 * Getters / Setters
	 */

	/**
	 * Sets the widget associated with this GuiWidget.
	 * 
	 * @param widget
	 */
	public void setWidget(Widget widget) {
		//Log.debug("AbstractGuiWidget " + this.toDebugString());
		this.widget = widget; 
		this.widget.setInputListener(this);
		this.widget.setReferenceCodeListener(this);
		
		Log.debug(this, "Loading reference codes from local storage.");
		ArrayList<String> refCodes = this.getLocalStorage().loadList(REFERENCE_CODES_STORAGE_ID);
		Log.debug(this, "Reference codes: " + refCodes.toString());
		if ( null != refCodes ) {
			int i = 0;
			for (WidgetOption widgetOption : this.widget.getWidgetOptions()) {
				if ( refCodes.size() > i ) {
					widgetOption.setReferenceCode(refCodes.get(i++));
				}
			}
		}
	}


	
	/**
	 * Fires an ActionEvent to all registered listeners of this Widget.
	 * 
	 * 
	 * @param ae
	 *            The ActionEvent to fire. If null, a default ActionEvent will
	 *            be created and sent.
	 */
	protected void fireActionEvent(ActionEvent<? extends PdWidget> ae) {
		if ( null == ae ) {
			return;
		}
		
		//Log.debug(this, "Firing " + ae.toDebugString());
		for ( ActionListener al : this.actionListeners ) {
			//Log.debug(this, "   on " + al.toString());
			al.onAction(ae);
		}
		
	}
	
	
	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return this.widget.getShortDescription();
	}


	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.widget.setShortDescription(shortDescription);
	}


	/**
	 * @return the longDescription
	 */
	public String getLongDescription() {
		return this.widget.getLongDescription();
	}


	/**
	 * @param longDescription the longDescription to set
	 */
	public void setLongDescription(String longDescription) {
		this.widget.setLongDescription(longDescription);
	}

	/**
	 * @return the userInputFeedbackPattern
	 */
	public String getUserInputFeedbackPattern() {
		return userInputFeedbackPattern;
	}

	/**
	 * @param userInputFeedbackPattern the userInputFeedbackPattern to set
	 */
	public void setUserInputFeedbackPattern(String userInputFeedbackPattern) {
		this.userInputFeedbackPattern = userInputFeedbackPattern;
	}
	
	
	
	protected void generateUserInputFeedbackMessage(WidgetInputEvent inputEvent, InputFeedback inputFeedback) {

		inputFeedback.setInfo( replaceParameters(this.userInputFeedbackPattern, inputEvent) );
		inputFeedback.setSharedFeedbackTitle( replaceParameters(this.userSharedTitleInputFeedbackPattern, inputEvent) );
		inputFeedback.setSharedFeedbackInfo( replaceParameters(this.userSharedInfoInputFeedbackPattern, inputEvent) );
	}
	
	
	private String replaceParameter(String inputString, String parameter, String replacement) {
		Log.debugFinest(this, "Replacing " + parameter +" in " + inputString + " with " + replacement );
	    String parameterPattern = parameter+"\\(([\\d]+)\\)";
	    	 
	    RegExp reg = RegExp.compile(parameterPattern);
	    MatchResult matcher = reg.exec(inputString);
	    
	    if ( matcher.getGroupCount() > 1 ) {
	    	int nChars = Integer.parseInt(matcher.getGroup(1));
	    	inputString = reg.replace(inputString, noNull(replacement, nChars) );
	    } else {
	    	inputString = inputString.replaceAll(parameter, noNull(replacement) );
	    }
	    return inputString;
	}
	
	private String replaceParameters(String inputString, WidgetInputEvent inputEvent) {
		 /* %U% - user nickname
		  * %P[i]% - Input parameter i
		 * %WS% - widget short description
		 * %WL% - widget long description
		 * %WOS% - widget option short description
		 * %WOL% - widget option long description
		 * %WOR% - widget option reference code
		 */
		
		
		// nickname
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_USER_NICKNAME, inputEvent.getNickname());
		
		
	    // widget short description
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_WIDGET_SHORT_DESCRIPTION, this.getShortDescription());
		
		
	    // widget long description
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_WIDGET_LONG_DESCRIPTION, this.getLongDescription());
	   
	   	
	    // widget option short description
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_WIDGET_OPTION_SHORT_DESCRIPTION, inputEvent.getWidgetOption().getShortDescription() );
	  
		
	    // widget option long description
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_WIDGET_OPTION_LONG_DESCRIPTION, inputEvent.getWidgetOption().getLongDescription() );

	    // widget option reference code
		inputString = replaceParameter(inputString, MessagePattern.PATTERN_WIDGET_OPTION_REFERENCE_CODE, inputEvent.getWidgetOption().getReferenceCode() );
		
			
		Log.debugFinest(this, "Replacing widget parameters: " + inputString);
		if ( null != inputEvent.getParameters() && inputEvent.getParameters().size() > 0 ) {
			for (int i = 0; i < inputEvent.getParameters().size(); i++ ) {

				inputString = replaceParameter(inputString, MessagePattern.getInputParameterPattern(i), inputEvent.getParameters().get(i) );
			}
		
		}
		return inputString;
	}
	
	private String noNull(String s) {
		if (null == s) {
			return "";
		}
		return s;
	}
	
	private String noNull(String s, int nChars) {
		s = noNull(s);
		if ( s.length() > nChars ) {
			s = s.substring(0, nChars) + "...";
		}
		return s;
	}

	/**
	 * @return the localStorage
	 */
	public LocalStorage getLocalStorage() {
		return localStorage;
	}

	/**
	 * @param localStorage the localStorage to set
	 */
	public void setLocalStorage(LocalStorage localStorage) {
		this.localStorage = localStorage;
	}

	/**
	 * @return the userSharedTitleInputFeedbackPattern
	 */
	public String getUserSharedTitleInputFeedbackPattern() {
		return userSharedTitleInputFeedbackPattern;
	}

	/**
	 * @param userSharedTitleInputFeedbackPattern the userSharedTitleInputFeedbackPattern to set
	 */
	public void setUserSharedTitleInputFeedbackPattern(String userSharedTitleInputFeedbackPattern) {
		this.userSharedTitleInputFeedbackPattern = userSharedTitleInputFeedbackPattern;
	}

	/**
	 * @return the userSharedInfoInputFeedbackPattern
	 */
	public String getUserSharedInfoInputFeedbackPattern() {
		return userSharedInfoInputFeedbackPattern;
	}

	/**
	 * @param userSharedInfoInputFeedbackPattern the userSharedInfoInputFeedbackPattern to set
	 */
	public void setUserSharedInfoInputFeedbackPattern(String userSharedInfoInputFeedbackPattern) {
		this.userSharedInfoInputFeedbackPattern = userSharedInfoInputFeedbackPattern;
	}


}
