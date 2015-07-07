/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.server.exception.ConfigServiceException;
import com.taobao.diamond.server.utils.SystemConfig;

/**
 * ���̲�������
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class DiskService {

	private static final Log log = LogFactory.getLog(DiskService.class);

	private ServletContext servletContext;

	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * �޸ı�ǻ���
	 */
	private final ConcurrentHashMap<String, Boolean> modifyMarkCache = new ConcurrentHashMap<String, Boolean>();

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void saveToDisk(ConfigInfo configInfo) throws IOException {
		if (configInfo == null)
			throw new IllegalArgumentException("configInfo����Ϊ��");
		if (!StringUtils.hasLength(configInfo.getDataId())
				|| StringUtils.containsWhitespace(configInfo.getDataId()))
			throw new IllegalArgumentException("��Ч��dataId");

		if (!StringUtils.hasLength(configInfo.getGroup())
				|| StringUtils.containsWhitespace(configInfo.getGroup()))
			throw new IllegalArgumentException("��Ч��group");

		if (!StringUtils.hasLength(configInfo.getContent()))
			throw new IllegalArgumentException("��Ч��content");

		final String basePath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR);
		createDirIfNessary(basePath);
		final String groupPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + configInfo.getGroup());
		createDirIfNessary(groupPath);

		String group = configInfo.getGroup();

		String dataId = configInfo.getDataId();

		dataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);

		final String dataPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group + "/" + dataId);
		File targetFile = createFileIfNessary(dataPath);

		File tempFile = File.createTempFile(group + "-" + dataId, ".tmp");
		FileOutputStream out = null;
		PrintWriter writer = null;
		try {
			out = new FileOutputStream(tempFile);
			BufferedOutputStream stream = new BufferedOutputStream(out);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					stream, Constants.ENCODE)));
			configInfo.dump(writer);
			writer.flush();
		} catch (Exception e) {
			log.error("��������ļ�ʧ��, tempFile:" + tempFile + ",targetFile:"
					+ targetFile, e);
		} finally {
			if (writer != null)
				writer.close();
		}

		String cacheKey = generateCacheKey(configInfo.getGroup(),
				configInfo.getDataId());
		// ���
		if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
			try {
				// �ļ����ݲ�һ��������²Ž��п���
				if (!FileUtils.contentEquals(tempFile, targetFile)) {
					try {
						// TODO ����ʹ�ð汾��, ��֤�ļ�һ��д�ɹ� , ����֪���ļ�û��д�ɹ���
						FileUtils.copyFile(tempFile, targetFile);
					} catch (Throwable t) {
						log.error("��������ļ�ʧ��, tempFile:" + tempFile
								+ ", targetFile:" + targetFile, t);
						SystemConfig.system_pause();
						throw new RuntimeException();

					}
				}
				tempFile.delete();
			} finally {
				// ������
				this.modifyMarkCache.remove(cacheKey);
			}
		} else
			throw new ConfigServiceException("������Ϣ���ڱ��޸�");
	}

	public boolean isModified(String dataId, String group) {
		return this.modifyMarkCache.get(generateCacheKey(group, dataId)) != null;
	}
	
	public ConcurrentHashMap<String, Boolean> getModifyMarkCache() {
        return this.modifyMarkCache;
    }

	/**
	 * ���ɻ���key�����ڱ���ļ��Ƿ����ڱ��޸�
	 * 
	 * @param group
	 * @param dataId
	 * 
	 * @return
	 */
	public final String generateCacheKey(String group, String dataId) {
		return group + "/" + dataId;
	}

	public void removeConfigInfo(String dataId, String group)
			throws IOException {
		if (!StringUtils.hasLength(dataId)
				|| StringUtils.containsWhitespace(dataId))
			throw new IllegalArgumentException("��Ч��dataId");

		if (!StringUtils.hasLength(group)
				|| StringUtils.containsWhitespace(group))
			throw new IllegalArgumentException("��Ч��group");

		final String basePath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR);
		createDirIfNessary(basePath);
		final String groupPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group);
		final File groupDir = new File(groupPath);
		if (!groupDir.exists()) {
			return;
		}
		// ���ﲢû��ȥ�ж�groupĿ¼�Ƿ�Ϊ�ղ�ɾ����Ҳ����˵����groupĿ¼��û���κ�dataId�ļ���Ҳ����Ȼ�����ڴ�����
		String fnDataId = SystemConfig.encodeDataIdForFNIfUnderWin(dataId);
		final String dataPath = WebUtils.getRealPath(servletContext,
				Constants.BASE_DIR + "/" + group + "/" + fnDataId);
		File dataFile = new File(dataPath);
		if (!dataFile.exists()) {
			return;
		}
		String cacheKey = generateCacheKey(group, dataId);
		// ���
		if (this.modifyMarkCache.putIfAbsent(cacheKey, true) == null) {
			try {
				if (!dataFile.delete())
					throw new ConfigServiceException("ɾ�������ļ�ʧ��");
			} finally {
				this.modifyMarkCache.remove(cacheKey);
			}
		} else
			throw new ConfigServiceException("�����ļ����ڱ��޸�");
	}

	public void removeConfigInfo(ConfigInfo configInfo) throws IOException {
		removeConfigInfo(configInfo.getDataId(), configInfo.getGroup());
	}

	private void createDirIfNessary(String path) {
		final File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	private File createFileIfNessary(String path) throws IOException {
		final File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				log.error("�����ļ�ʧ��, path=" + path, e);
			}
		}
		return file;
	}

}
