/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.utils;

import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

import com.taobao.diamond.common.Constants;


public class ContentUtils {

    public static void verifyIncrementPubContent(String content) {

        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("����/ɾ�����ݲ���Ϊ��");
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\r' || c == '\n') {
                throw new IllegalArgumentException("����/ɾ�����ݲ��ܰ����س��ͻ���");
            }
            if (c == Constants.WORD_SEPARATOR.charAt(0)) {
                throw new IllegalArgumentException("����/ɾ�����ݲ��ܰ���(char)2");
            }
        }
    }


    public static String getContentIdentity(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("����û�а����ָ���");
        }
        return content.substring(0, index);
    }


    public static String getContent(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("����û�а����ָ���");
        }
        return content.substring(index + 1);
    }
}
