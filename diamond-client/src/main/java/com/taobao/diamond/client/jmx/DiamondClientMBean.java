/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.jmx;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface DiamondClientMBean {
    /**
     * ��ȡ�ͻ���ע�����������dataId
     * 
     * @return
     */
    Map<String, Set<String>> getDataIds();


    /**
     * ��ȡ��ǰʹ�õķ������б�
     * 
     * @return
     */
    Map<String, List<String>> getServerList();


    /**
     * ��÷�����dataId�б�
     * 
     * @return
     */
    Map<String, Set<String>> getPubDataIds();

    /**
     * ���dataId�����ʹ���
     */
    Map<String, Map<String, Integer>> getPopCount();
}
