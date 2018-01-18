package com.my.factory;

import javax.servlet.http.HttpSession;

public class LocalSession implements AbstractSession {

	@Override
	public void addSession(String sessionId, HttpSession session) {
		sessions.put(sessionId, session);
	}

	@Override
	public void delSession(String sessionId) {
		sessions.remove(sessionId);

	}

	@Override
	public HttpSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}

}