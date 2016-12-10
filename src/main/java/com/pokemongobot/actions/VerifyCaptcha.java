package com.pokemongobot.actions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.regex.Pattern;



import com.google.protobuf.ByteString;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;

import POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import POGOProtos.Networking.Requests.Messages.CheckChallenge.CheckChallengeMessage;
import POGOProtos.Networking.Responses.CheckChallengeResponseOuterClass.CheckChallengeResponse;


@SuppressWarnings("restriction")
public class VerifyCaptcha {
	public static final String USER_AGENT =
			"Mozilla/5.0 (Windows NT 10.0; WOW64)" +
			" AppleWebKit/537.36 (KHTML, like Gecko) " +
			"Chrome/54.0.2840.99 Safari/537.36";

	static{
		URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				if (protocol.equals("unity")) {
					return new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(URL url) throws IOException {
							return new URLConnection(url) {
								@Override
								public void connect() throws IOException {
									System.out.println("Received token: " + url.toString()
											.split(Pattern.quote(":"))[1]);
								}
							};
						}
					};
				}
				return null;
			}
		});
	}
	
	public static void completeCaptcha(final PokemonGo api, final String challengeURL, Logger logger){
		javafx.application.Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		        //if you change the UI, do it here !
				
				JFXPanel panel = new JFXPanel();
				//Create WebView and WebEngine to display the captcha webpage

				WebView view = new WebView();
				WebEngine engine = view.getEngine();

				//Set UserAgent so captcha shows in the WebView
				engine.setUserAgent(USER_AGENT);
				engine.load(challengeURL);

				final JFrame frame = new JFrame("Solve Captcha");

				engine.locationProperty().addListener(new ChangeListener<String>() {
					
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (newValue.startsWith("unity:")) {
							String token = newValue.split(Pattern.quote(":"))[1];
							try {
								//Close this window, not valid anymore
								frame.setVisible(false);
								if (api.verifyChallenge(token)) {
									logger.info("Captcha was correctly solved!");
								} else {
									logger.info("Captcha was incorrectly solved! Retry.");
									
									//Removes the current challenge to allow the CheckChallengeMessage to send
									api.updateChallenge(null, false);
									CheckChallengeMessage message = CheckChallengeMessage.newBuilder().build();
									AsyncServerRequest request = new AsyncServerRequest(RequestType.CHECK_CHALLENGE, message);
									ByteString responseData =
											AsyncHelper.toBlocking(api.getRequestHandler().sendAsyncServerRequests(request));
									CheckChallengeResponse response = CheckChallengeResponse.parseFrom(responseData);
									
									String newChallenge = response.getChallengeUrl();
									if (newChallenge != null && newChallenge.length() > 0) {
										//New challenge URL, open a new window for that
										api.updateChallenge(newChallenge, true);
										completeCaptcha(api, newChallenge, logger);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
				
				//Applies the WebView to this panel
				panel.setScene(new Scene(view));
				frame.getContentPane().add(panel);
				frame.setSize(500, 500);
				frame.setVisible(true);		    	
		    }
		});
	}
	

}
