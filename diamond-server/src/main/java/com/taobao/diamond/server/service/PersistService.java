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

import java.sql.Timestamp;

import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;

/**
 * ���ݿ�����ṩConfigInfo�����ݿ�Ĵ�ȡ����
 * 
 * @author boyan
 * @since 1.0
 */

public interface PersistService {

	/**
	 * ����������Ϣ
	 * 
	 * @param time
	 * @param configInfo
	 */
	public void addConfigInfo(final Timestamp time, final ConfigInfo configInfo);

	/**
	 * ����dataId��groupɾ��������Ϣ
	 * 
	 * @param dataId
	 * @param group
	 */
	public void removeConfigInfo(final String dataId, final String group);

	/**
	 * ��������IDɾ��������Ϣ
	 * 
	 * @param id
	 */
	public void removeConfigInfoByID(final long id);

	/**
	 * ������������
	 * 
	 * @param time
	 * @param configInfo
	 */
	public void updateConfigInfo(final Timestamp time,
			final ConfigInfo configInfo);

	/**
	 * ����dataId��group��ѯ������Ϣ
	 * 
	 * @param dataId
	 * @param group
	 * @return
	 */
	public ConfigInfo findConfigInfo(final String dataId, final String group);

	/**
	 * ��������ID��ѯ������Ϣ
	 * 
	 * @param id
	 * @return
	 */
	public ConfigInfo findConfigInfoByID(long id);

	/**
	 * ��ҳ��ѯ���е�������Ϣ
	 * 
	 * @param pageNo
	 *            ҳ��
	 * @param pageSize
	 *            ÿҳ��С
	 * @return
	 */
	public Page<ConfigInfo> findAllConfigInfo(final int pageNo,
			final int pageSize);

	/**
	 * ����dataId��ѯ������Ϣ
	 * 
	 * @param pageNo
	 *            ҳ��
	 * @param pageSize
	 *            ÿҳ��С
	 * @param dataId
	 *            dataId
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoByDataId(final int pageNo,
			final int pageSize, final String dataId);

	/**
	 * ����dataId��groupģ����ѯ������Ϣ
	 * 
	 * @param pageNo
	 *            ҳ��
	 * @param pageSize
	 *            ÿҳ��С
	 * @param dataId
	 * @param group
	 * @return
	 */
	public Page<ConfigInfo> findConfigInfoLike(final int pageNo,
			final int pageSize, final String dataId, final String group);

}
