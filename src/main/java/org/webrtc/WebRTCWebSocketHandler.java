package org.webrtc;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.webrtc.common.SignalingWebSocket;

import javax.servlet.http.HttpServletRequest;

public class WebRTCWebSocketHandler extends WebSocketHandler {
	
	public WebSocket doWebSocketConnect(HttpServletRequest request,	String protocol) {
		return new SignalingWebSocket();
	}

}