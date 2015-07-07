/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.server.service.ConfigService;
import com.taobao.diamond.server.service.DiskService;

public class ConfigServlet extends HttpServlet {

	private static final long serialVersionUID = 4339468526746635388L;

	private ConfigController configController;

	private ConfigService configService;

	private DiskService diskService;

	@Override
	public void init() throws ServletException {

		super.init();
		WebApplicationContext webApplicationContext = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		configService = (ConfigService) webApplicationContext
				.getBean("configService");
		this.diskService = (DiskService) webApplicationContext
				.getBean("diskService");
		configController = new ConfigController();
		this.configController.setConfigService(configService);
		this.configController.setDiskService(diskService);
	}

	public void forward(HttpServletRequest request,
			HttpServletResponse response, String page, String basePath,
			String postfix) throws IOException, ServletException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher(basePath + page + postfix);
		requestDispatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String probeModify = request
				.getParameter(Constants.PROBE_MODIFY_REQUEST);
		if (!StringUtils.hasLength(probeModify))
			throw new IOException("��Ч��probeModify");
		String page = this.configController.getProbeModifyResult(request,
				response, probeModify);
		forward(request, response, page, "/jsp/", ".jsp");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String group = request.getParameter("group");
		String dataId = request.getParameter("dataId");

		if (!StringUtils.hasLength(dataId)) {
			throw new IOException("��Ч��dataId");
		}

		String page = this.configController.getConfig(request, response,
				dataId, group);
		if (page.startsWith("forward:")) {
			page = page.substring(8);
			forward(request, response, page, "", "");
		} else {
			forward(request, response, page, "/jsp/", ".jsp");
		}

	}

}
