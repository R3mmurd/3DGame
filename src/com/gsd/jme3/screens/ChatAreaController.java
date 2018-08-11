package com.gsd.jme3.screens;

import java.util.Properties;
import java.util.Random;

import javax.sound.midi.ControllerEventListener;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.ChatTextSendEvent;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.ScrollPanel;
import de.lessvoid.nifty.controls.ScrollPanel.AutoScroll;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.xml.xpp3.Attributes;

/**
 * The ChatControlDialogController registers a new control with Nifty that
 * represents the whole Dialog. This gives us later an appropriate
 * ControlBuilder to actual construct the Dialog (as a control).
 * 
 * @author void
 */
public class ChatAreaController implements Controller {
	private ScrollPanel scrollPanel;
	private Element textArea;
	private Screen screen;

	@Override
	public void bind(Nifty nifty, Screen screen,
			Element element, Properties parameter,
			Attributes controlDefinitionAttributes) {
		this.screen = nifty.getScreen("end");
		scrollPanel = element.findNiftyControl("scroll_panel",
				ScrollPanel.class);
		textArea = element.findElementByName("text_area");
	}

	@Override
	public void onStartScreen() {
	}

	@Override
	public void onFocus(boolean getFocus) {
	}

	@Override
	public boolean inputEvent(NiftyInputEvent inputEvent) {
		return false;
	}

	public void setAutoScroll(AutoScroll auto) {
		scrollPanel.setAutoScroll(auto);
	}

	public AutoScroll getAutoScroll() {
		return scrollPanel.getAutoScroll();
	}

	public void append(String text) {
		setText(getText() + text);
	}

	public void setText(String text) {
		textArea.getRenderer(TextRenderer.class).setText(text);
		screen.layoutLayers();
		textArea.setHeight(textArea.getRenderer(TextRenderer.class)
				.getTextHeight());
	}

	public String getText() {
		return textArea.getRenderer(TextRenderer.class).getOriginalText();
	}

	@Override
	public void init(Properties arg0, Attributes arg1) {
		// TODO Auto-generated method stub
		
	}
}
