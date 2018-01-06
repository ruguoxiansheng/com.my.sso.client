package com.my.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(value = "/client")
public class ClientController {
	@Autowired
	private Environment env;

	@RequestMapping(value = "/auth/check")
	public Long authCheck(HttpServletRequest request) throws Exception {
		// 接受来自认证中心的token
		String token = request.getParameter("token");
		if (token == null) {
			throw new Exception("没有token!属于数据异常！");
		}

		// 去server端验证token的真伪
		checkToken(token, request);
		return null;
	}

	@RequestMapping(value = "/auth/logout")
	public Long authLogout(JSONObject request) throws Exception {

		return null;
	}

	// 这个方法有出入
	private String checkToken(String token, HttpServletRequest request) {
		// 向认证中心发送验证token请求
		// String verifyURL = "http://" + server +
		// PropertiesConfigUtil.getProperty("sso.server.verify");

		// 去server端的接口/server/auth/verify验证token的有效性
		String verifyURL = "http://" + env.getProperty("sso.server") + env.getProperty("sso.server.verify");
		HttpClient httpClient = new DefaultHttpClient();
		// serverName作为本应用标识
		HttpGet httpGet = new HttpGet(verifyURL + "?token=" + token + "&localId=" + request.getSession().getId());
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.OK.value()) {
				String result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
				// 解析json数据
				ObjectMapper objectMapper = new ObjectMapper();
				VerifyBean verifyResult = objectMapper.readValue(result, VerifyBean.class);
				// 验证通过,应用返回浏览器需要验证的页面
				if (verifyResult.getRet().equals("0")) {
					Auth auth = new Auth();
					auth.setUserId(verifyResult.getUserId());
					auth.setUsername(verifyResult.getUsername());
					auth.setGlobalId(verifyResult.getGlobalId());
					request.getSession().setAttribute("auth", auth);
					// 建立本地会话，返回到请求页面
					String returnURL = request.getParameter("returnURL");
					return "redirect:http://" + returnURL;
				}
			}
		} catch (Exception e) {
			// 返回登录页面
			String loginURL = "/login";
			return "redirect:" + loginURL;
		}

		return null;

	}
}
