/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.domain;

/**
 * ConfigInfo����չ��, ����ϰ汾��SDK���°汾��server���ڷ����л��ֶδ���������Ĳ���������
 * 
 * @author leiwen.zh
 * 
 */
public class ConfigInfoEx extends ConfigInfo {

	private static final long serialVersionUID = -1L;

	// ������ѯʱ, �������ݵ�״̬��, �����״̬����Constants.java��
	private int status;
	// ������ѯʱ, �������ݵ���Ϣ
	private String message;

	public ConfigInfoEx() {
		super();
	}

	public ConfigInfoEx(String dataId, String group, String content) {
		super(dataId, group, content);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ConfigInfoEx [status=" + status + ", message=" + message
				+ ", getId()=" + getId() + ", getDataId()=" + getDataId()
				+ ", getGroup()=" + getGroup() + ", getContent()="
				+ getContent() + ", getMd5()=" + getMd5() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString()
				+ ", getClass()=" + getClass() + "]";
	}

}
