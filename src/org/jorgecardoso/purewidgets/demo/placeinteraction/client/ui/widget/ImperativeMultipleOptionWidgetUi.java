package org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.widget;

import org.instantplaces.purewidgets.client.widgets.ReferenceCodeFormatter;
import org.instantplaces.purewidgets.shared.widgetmanager.WidgetOption;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.MultipleOptionImperativeClickHandler;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.UiType;
import org.jorgecardoso.purewidgets.demo.placeinteraction.client.ui.popup.PopupUi;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class ImperativeMultipleOptionWidgetUi extends Composite {

	
	@UiTemplate("ImperativeMultipleOptionWidgetDesktop.ui.xml")
	interface ImperativeMultipleOptionUiDesktopUiBinder extends UiBinder<Widget, ImperativeMultipleOptionWidgetUi> {	}
	private static ImperativeMultipleOptionUiDesktopUiBinder desktopUiBinder = GWT.create(ImperativeMultipleOptionUiDesktopUiBinder.class);
	
	@UiTemplate("ImperativeMultipleOptionWidgetSmartphone.ui.xml")
	interface ImperativeMultipleOptionUiSmartphoneUiBinder extends UiBinder<Widget, ImperativeMultipleOptionWidgetUi> {	}
	private static ImperativeMultipleOptionUiSmartphoneUiBinder smartphoneUiBinder = GWT.create(ImperativeMultipleOptionUiSmartphoneUiBinder.class);
	
	
	/*
	 * The ui type we will generate
	 */
	private UiType uiType;
	
	@UiField Panel mainHorizontalPanel;
	@UiField Image iconImage;
	@UiField Label descriptionLabel;
	@UiField Button actionButton;
	@UiField ListBox optionsListBox;
	
	/*
	 * Indicates whether we should load the widget icon. This is determined according to the
	 * template being used.
	 */
	private boolean loadWidgetIcon;
	
	private org.instantplaces.purewidgets.shared.widgets.Widget pureWidget;

	public ImperativeMultipleOptionWidgetUi(UiType uiType, org.instantplaces.purewidgets.shared.widgets.Widget widget) {
		this.uiType = uiType;
		this.pureWidget = widget;
		initWidget(this.getUiBinder(uiType).createAndBindUi(this));
		
		this.initUi();
	}
	
	private void initUi() {
		String description = this.pureWidget.getLongDescription();
		if ( null == description || description.trim().length() == 0) {
			description = this.pureWidget.getShortDescription();
		}
		this.descriptionLabel.setText(description);
		this.actionButton.setText(this.pureWidget.getShortDescription());
		
		if ( this.loadWidgetIcon ) {
			if ( true ) { /* TODO: check icon */
				this.iconImage.removeFromParent(); // remove icon
			}
		} else {
			this.iconImage.removeFromParent();
		}
		
		this.optionsListBox.setVisibleItemCount(Math.min(4, this.pureWidget.getWidgetOptions().size()));
		for (WidgetOption wo : this.pureWidget.getWidgetOptions() ) {
			this.optionsListBox.addItem(wo.getShortDescription() + " " + ReferenceCodeFormatter.format(wo.getReferenceCode()));
		}
		
		this.actionButton.addClickHandler(new MultipleOptionImperativeClickHandler(this.pureWidget.getPlaceId(), this.pureWidget.getApplicationId(), 
				this.pureWidget.getWidgetId(), this.pureWidget.getWidgetOptions(), this.optionsListBox, new PopupUi(this.uiType)));
//		
	}

	private UiBinder<Widget, ImperativeMultipleOptionWidgetUi> getUiBinder(UiType uiType) {
		switch ( uiType ) {
		
		case Desktop:
			this.loadWidgetIcon = true;
			return desktopUiBinder;
		case Smartphone:
			this.loadWidgetIcon = false;
			return smartphoneUiBinder;
		default:
			return desktopUiBinder;
		}
	}
	

	

}