package org.purewidgets.server.im.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.purewidgets.shared.im.WidgetInput;

/**
 * 
 * The JSON data transfer object for an ArrayList of WidgetInput objects.
 * 
 * @author "Jorge C. S. Cardoso"
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.ANY, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown=true)
public class WidgetInputListJson extends GenericJson {

	private ArrayList<WidgetInputJson> inputs;

	private WidgetInputListJson() {
		
	}
	
	public static WidgetInputListJson create(ArrayList<WidgetInput> widgetInputs) {
		WidgetInputListJson widgetInputListJson =  new WidgetInputListJson();
		
		ArrayList<WidgetInputJson> widgetInputsJson = new ArrayList<WidgetInputJson>();
		for ( WidgetInput widgetInput : widgetInputs ) {
			widgetInputsJson.add(WidgetInputJson.create(widgetInput));
		}
		
		widgetInputListJson.setInputs(widgetInputsJson);
		
		return widgetInputListJson;
	}
	
	public ArrayList<WidgetInput> getWidgetInputList() {
		ArrayList<WidgetInput> widgetInputs = new ArrayList<WidgetInput>();
		
		for ( WidgetInputJson widgetInputJson : this.inputs ) {
			widgetInputs.add( widgetInputJson.getWidgetInput() );
		}
		return widgetInputs;
	}


	/**
	 * @param inputs the inputs to set
	 */
	private void setInputs(ArrayList<WidgetInputJson> inputs) {
		this.inputs = inputs;
	}
	
}
