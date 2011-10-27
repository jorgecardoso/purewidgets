/**
 * 
 */
package org.jorgecardoso.purewidgets.demo.client.publicyoutubeplayer;

import org.instantplaces.purewidgets.client.widgets.GuiButton;
import org.instantplaces.purewidgets.client.widgets.youtube.Video;
import org.instantplaces.purewidgets.shared.events.ActionEvent;
import org.instantplaces.purewidgets.shared.events.ActionListener;

import com.google.gwt.core.client.GWT;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author "Jorge C. S. Cardoso"
 *
 */
public class VideoActionEntry extends Composite implements ActionListener {

	private static VideoActionEntryUiBinder uiBinder = GWT
			.create(VideoActionEntryUiBinder.class);

	interface VideoActionEntryUiBinder extends
			UiBinder<Widget, VideoActionEntry> {
	}

	
	@UiField
    Label title;
	
	@UiField
	VerticalPanel verticalPanel;
	
	@UiField
	HorizontalPanel horizontalPanel;

	@UiField
	Image image;
	
	private Video video;
	
	private GuiButton guiButton;
	
	private VideoActionListener videoEventListener;
	private String action;
	
	
	public VideoActionEntry(Video video, String actionLabel) {
		initWidget(uiBinder.createAndBindUi(this));
		this.video = video;
		
		title.setText( video.getTitle() );
		image.setUrl( video.getThumbnail() );
		//image.setHeight("100px");
		guiButton = createButton(video.getId(), actionLabel);
		horizontalPanel.add( guiButton );
	}
	
	public void highlight(boolean h) {
		if ( h ) { 
			this.verticalPanel.addStyleName("highlight");
		} else {
			this.verticalPanel.removeStyleName("highlight");
		}
	}
	
	public void dispose() {
		if ( null != guiButton ) {
			guiButton.removeFromServer();
		}
	}
	
	/**
	 * 
	 */
	private GuiButton createButton(String videoId, String label) {
		GuiButton btn = new GuiButton(this.encodeLabel(label)+"-"+videoId, label);
		
		btn.setSize("175px", "100px");
		btn.setVolatile(true);
		btn.getFeedbackSequencer().setFeedbackFinalDelay(5000);
		btn.addActionListener(this);
		return btn;
	}

	
	private String encodeLabel(String label) {
		return label.replace(' ', '_');
	}

	@Override
	public void onAction(ActionEvent<?> e) {
		if ( null != this.videoEventListener ) {
			this.videoEventListener.onVideoAction(e, this.video, this.action);
		}
	}


	/**
	 * @return the videoEventListener
	 */
	public VideoActionListener getVideoEventListener() {
		return videoEventListener;
	}


	/**
	 * @param videoEventListener the videoEventListener to set
	 */
	public void setVideoEventListener(VideoActionListener videoEventListener, String action) {
		this.action = action;
		this.videoEventListener = videoEventListener;
	}

	/**
	 * @return the video
	 */
	public Video getVideo() {
		return video;
	}


}