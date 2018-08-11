package com.gsd.jme3.screens;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.EventListenerList;

import com.gsd.jme3.main.Main;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.textfield.TextFieldControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;

@SuppressWarnings("deprecation")
public class ScreenControler implements ScreenController {

	private EventListenerList eventListenerList = new EventListenerList();

	private Main app;
	private Nifty nifty;

	private Element progressBarElement;
	private TextRenderer textRenderer;
	public boolean load = false;
	public boolean audio = true;

	// private Element textInput;
	private Element chatArea;
	private Element textField;
	private boolean fullScreen = false;

	public static ScreenControler instance;

	public ScreenControler(Main app, Nifty nifty) {
		this.app = app;
		this.nifty = nifty;
		instance = this;
	}

	public void setProgress(final float progress, String loadingText) {
		final int MIN_WIDTH = 32;

		Element element = nifty.getScreen("loadlevel").findElementByName(
				"loadingtext");

		progressBarElement = nifty.getScreen("loadlevel").findElementByName(
				"progressbar");
		textRenderer = element.getRenderer(TextRenderer.class);

		int pixelWidth = (int) (MIN_WIDTH + (progressBarElement.getParent()
				.getWidth() - MIN_WIDTH) * progress);
		progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
		progressBarElement.getParent().layoutElements();

		textRenderer.setText(loadingText);
	}

	public void showLoadingMenu() {
		nifty.gotoScreen("loadlevel");
		load = true;
	}

	public void quitGame() {
		app.stop();
	}

	public void audioOptions() {
		if (audio) {
			app.audioSource.stop();
			audio = false;
			System.out.println("Audio Apagado ");
		} else {
			app.setAudio();
			app.audioSource.play();
			audio = true;
			System.out.println("Audio Prendido ");
		}

	}

	public void fullScreen() {

		fullScreen = !app.getContext().getSettings().isFullscreen();

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice devices = env.getDefaultScreenDevice();

		app.getContext().getSettings().setResolution( //
				devices.getDisplayMode().getWidth(), //
				devices.getDisplayMode().getHeight());
		app.getContext().getSettings().setFrequency( //
				devices.getDisplayMode().getRefreshRate());

		app.getContext().getSettings().setFullscreen(fullScreen);
		app.restart();

	}

	public void gotoScreen(String screen) {
		System.out.println("Estoy cambiando screen");
		nifty.gotoScreen(screen);
	}

	@Override
	public void onStartScreen() {
	}

	@Override
	public void onEndScreen() {
	}

	@Override
	public void bind(Nifty nifty, Screen screen) {
		progressBarElement = nifty.getScreen("loadlevel").findElementByName(
				"progressbar");
		textField = nifty.getScreen("end").findElementByName("text_input");
		chatArea = nifty.getScreen("end").findElementByName("text_area");
	}

	public void sendMessage() {
		String text = textField.getControl(TextFieldControl.class).getText();
		if (text.isEmpty()) {
			return;
		}
		sendMessage(text);
		textField.getControl(TextFieldControl.class).setText("");
		textField.setFocus();
		fireActionEvent(new ActionEvent(this, 0, text));
	}

	public void sendMessage(String text) {
		String textFieldText = textField.getControl(TextFieldControl.class)
				.getText();
		String lastText = chatArea.getRenderer(TextRenderer.class)
				.getOriginalText();

		if (lastText.isEmpty()) {
			chatArea.getRenderer(TextRenderer.class).setText(text);
		} else {
			chatArea.getRenderer(TextRenderer.class).setText(
					text + "\n" + lastText);
		}
		textField.getControl(TextFieldControl.class).setText(textFieldText);
	}

	public void addActionListener(ActionListener listener) {
		eventListenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		eventListenerList.remove(ActionListener.class, listener);
	}

	public ActionListener[] getActionListeners() {
		return eventListenerList.getListeners(ActionListener.class);
	}

	private void fireActionEvent(ActionEvent evt) {
		ActionListener[] actionListeners = getActionListeners();

		for (ActionListener actionListener : actionListeners) {
			actionListener.actionPerformed(evt);
		}
	}

}
