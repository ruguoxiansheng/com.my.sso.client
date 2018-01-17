package com.my.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.my.util.LocalSessions;
import com.my.util.MyHttpUtils;

@Controller
@RequestMapping(value = "/client")
public class ClientController {

	@Autowired
	private Environment env;

	private MyHttpUtils myHttpUtils = new MyHttpUtils();

	@RequestMapping(value = "/auth/check")
	public String authCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 接受来自认证中心的token
		String token = request.getParameter("token");
		if (token == null) {
			return "/login";
		}

		// 去server端验证token的真伪
		String tokenInfoStr = "";
		try {
			tokenInfoStr = checkToken(token, request);
		} catch (Exception e) {
			return "/login";
		}
		if (StringUtils.isEmpty(tokenInfoStr)) {
			return "/login";
		}

		JSONObject tokenInfo = JSONObject.parseObject(tokenInfoStr);
		// 如有效简历本地会话
		HttpSession session = request.getSession(true);
		String localSessionId = session.getId();
		LocalSessions.addSession(localSessionId, session);

		// 采用cookie的方式记录下两个sessionId
		Cookie localSessionCookie = new Cookie(request.getParameter("returnURL") + "SessionId", session.getId());
		localSessionCookie.setPath("/");
		Cookie globalSessionCookie = new Cookie("globalSessionId", tokenInfo.getString("globalSessionId"));
		globalSessionCookie.setPath("/");
		response.addCookie(globalSessionCookie);
		response.addCookie(localSessionCookie);

		// 验证token之后，重定向到请求的页面
		response.sendRedirect("http://localhost:8078/thymeleaf/" + request.getParameter("returnURL"));
		return null;
	}

	@RequestMapping(value = "/auth/logout")
	public Long authLogout(JSONObject request) throws Exception {

		return null;
	}

	// 这个方法有出入
	private String checkToken(String token, HttpServletRequest request) throws ClientProtocolException, IOException {
		// 向认证中心发送验证token请求
		// 去server端的接口/server/auth/verify验证token的有效性
		String verifyURL = "http://" + env.getProperty("sso.server") + env.getProperty("sso.server.verify");
		// serverName作为本应用标识
		String tokenInfo = "";
		MyHttpUtils myHttpUtils = new MyHttpUtils();
		JSONObject reqObj = new JSONObject();
		reqObj.put("token", token);
		tokenInfo = myHttpUtils.httpPostJsonObj(verifyURL, reqObj, "utf-8");

		return tokenInfo;

	}
}
