/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.processor;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.CacheData;
import com.taobao.diamond.io.FileSystem;
import com.taobao.diamond.io.Path;
import com.taobao.diamond.io.watch.StandardWatchEventKind;
import com.taobao.diamond.io.watch.WatchEvent;
import com.taobao.diamond.io.watch.WatchKey;
import com.taobao.diamond.io.watch.WatchService;
import com.taobao.diamond.utils.FileUtils;
import com.taobao.diamond.utils.JSONUtils;


public class LocalConfigInfoProcessor {
    private static final Log log = LogFactory.getLog(LocalConfigInfoProcessor.class);
    private ScheduledExecutorService singleExecutor = Executors.newSingleThreadScheduledExecutor();;

    private volatile Map<String/* address */, Map<String/* dataId */, String/* group */>> localMap = null;

    private final Map<String/* filePath */, Long/* version */> existFiles = new HashMap<String, Long>();

    private volatile boolean isRun;
    private final String localAddress = getHostAddress();
    private String rootPath = null;


    static String getHostAddress() {
        String address = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> ads = ni.getInetAddresses();
                while (ads.hasMoreElements()) {
                    InetAddress ip = ads.nextElement();
                    if (!ip.isLoopbackAddress() && ip.isSiteLocalAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("��ȡ������ַʧ��", e);
        }
        return address;
    }


    boolean containThisHostConfig() {
        if (null == localMap) {
            return false;
        }
        return localMap.containsKey(this.localAddress);
    }


    boolean isQualified(String dataId, String group) {
        Map<String, String> map2 = localMap.get(this.localAddress);
        if (null == map2) {
            throw new RuntimeException("��֤����������Ϣ�ļ����ʴ���");
        }
        String qualifiedGroup = map2.get(dataId);
        if (group.equals(qualifiedGroup)) {
            return true;
        }
        return false;
    }


    /**
     * ��ȡ��������
     * 
     * @param cacheData
     * @param force
     *            ǿ�ƻ�ȡ����û�б����ʱ�򲻷���null
     * @return
     * @throws IOException
     */
    public String getLocalConfigureInfomation(CacheData cacheData, boolean force) throws IOException {
        if (null == localMap) {
            if (cacheData.isUseLocalConfigInfo()) {
                cacheData.setLastModifiedHeader(Constants.NULL);
                cacheData.setMd5(Constants.NULL);
                cacheData.setLocalConfigInfoFile(null);
                cacheData.setLocalConfigInfoVersion(0L);
                cacheData.setUseLocalConfigInfo(false);
            }
            return null;
        }
        String realGroup = getGroupByAddress(cacheData.getDataId(), cacheData.getGroup());
        String filePath = getFilePath(cacheData.getDataId(), realGroup);
        if (!existFiles.containsKey(filePath)) {
            if (cacheData.isUseLocalConfigInfo()) {
                cacheData.setLastModifiedHeader(Constants.NULL);
                cacheData.setMd5(Constants.NULL);
                cacheData.setLocalConfigInfoFile(null);
                cacheData.setLocalConfigInfoVersion(0L);
                cacheData.setUseLocalConfigInfo(false);
            }
            return null;
        }
        if (force) {
        	log.info("�����ӱ��ػ�ȡ��������, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
        	
            String content = FileUtils.getFileContent(filePath);
            return content;
        }
        // �ж��Ƿ�����û�б��������null
        if (!filePath.equals(cacheData.getLocalConfigInfoFile())
                || existFiles.get(filePath) != cacheData.getLocalConfigInfoVersion()) {
            String content = FileUtils.getFileContent(filePath);
            cacheData.setLocalConfigInfoFile(filePath);
            cacheData.setLocalConfigInfoVersion(existFiles.get(filePath));
            cacheData.setUseLocalConfigInfo(true);
            
            if (log.isInfoEnabled()) {
                log.info("�����������ݷ����仯, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
            }
            
            return content;
        }
        else {
            cacheData.setUseLocalConfigInfo(true);
            
            if (log.isInfoEnabled()) {
            	log.debug("������������û�з����仯, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
            }
            
            return null;
        }
    }


    public boolean containsNewLocalConfigureInfomation(CacheData cacheData) {
        if (null == localMap) {
            return false;
        }
        String realGroup = getGroupByAddress(cacheData.getDataId(), cacheData.getGroup());
        String filePath = getFilePath(cacheData.getDataId(), realGroup);
        if (!existFiles.containsKey(filePath)) {
            return false;
        }
        if (!filePath.equals(cacheData.getLocalConfigInfoFile())
                || existFiles.get(filePath) != cacheData.getLocalConfigInfoVersion()) {
            return true;
        }
        return false;
    }


    String getGroupByAddress(String dataId, String clientGroup) {
        Map<String, String> mappingGroups = localMap.get(this.localAddress);
        if (mappingGroups != null) {
            String mappingGroup = mappingGroups.get(dataId);
            if (mappingGroup != null) {
                return mappingGroup;
            }
            else {
                return defaultGroupOrClientGroup(clientGroup);
            }
        }
        else {
            return defaultGroupOrClientGroup(clientGroup);
        }
    }


    String defaultGroupOrClientGroup(String clientGroup) {
        if (clientGroup != null)
            return clientGroup;
        else
            return Constants.DEFAULT_GROUP;
    }


    String getFilePath(String dataId, String group) {
        StringBuilder filePathBuilder = new StringBuilder();
        filePathBuilder.append(rootPath).append("/").append(Constants.BASE_DIR).append("/").append(group).append("/")
            .append(dataId);
        File file = new File(filePathBuilder.toString());
        return file.getAbsolutePath();
    }


    public synchronized void start(String rootPath) {
        if (this.isRun) {
            return;
        }
        this.rootPath = rootPath;
        this.isRun = true;
        if (this.singleExecutor == null || singleExecutor.isTerminated()) {
            singleExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        initDataDir(rootPath);
        startCheckLocalDir(rootPath);
    }


    private void initDataDir(String rootPath) {
        try {
            File flie = new File(rootPath);
            flie.mkdir();
        }
        catch (Exception e) {
        }
    }


    public synchronized void stop() {
        if (!this.isRun) {
            return;
        }
        this.isRun = false;
        this.singleExecutor.shutdownNow();
        this.singleExecutor = null;
    }


    private void startCheckLocalDir(final String filePath) {
        final WatchService watcher = FileSystem.getDefault().newWatchService();

        Path path = new Path(new File(filePath));
        // ע���¼�
        watcher.register(path, true, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE,
            StandardWatchEventKind.ENTRY_MODIFY);
        // ��һ�����У�����check
        checkAtFirst(watcher);
        singleExecutor.execute(new Runnable() {
            public void run() {
                log.debug(">>>>>>�Ѿ���ʼ���Ŀ¼<<<<<<");
                // ����ѭ���ȴ��¼�
                while (isRun) {
                    // ƾ֤
                    WatchKey key;
                    try {
                        key = watcher.take();
                    }
                    catch (InterruptedException x) {
                        continue;
                    }
                    // reset�������Ч������ѭ��,��Ч�����Ǽ�����Ŀ¼��ɾ��
                    if (!processEvents(key)) {
                        log.error("reset unvalid,��ط���ʧЧ");
                        break;
                    }
                }
                log.debug(">>>>>>�˳����Ŀ¼<<<<<<");
                watcher.close();

            }

        });
    }


    private void checkAtFirst(final WatchService watcher) {
        watcher.check();
        WatchKey key = null;
        while ((key = watcher.poll()) != null) {
            processEvents(key);
        }
    }


    /**
     * ���������¼�
     * 
     * @param key
     * @return
     */
    @SuppressWarnings( { "unchecked" })
    private boolean processEvents(WatchKey key) {
        /**
         * ��ȡ�¼�����
         */
        for (WatchEvent<?> event : key.pollEvents()) {
            // �¼�������
            // WatchEvent.Kind<?> kind = event.kind();

            // ͨ��context�����õ������¼���path
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            Path eventPath = ev.context();

            String realPath = eventPath.getAbsolutePath();
            if (ev.kind() == StandardWatchEventKind.ENTRY_CREATE || ev.kind() == StandardWatchEventKind.ENTRY_MODIFY) {

                String grandpaDir = null;
                try {
                    grandpaDir = FileUtils.getGrandpaDir(realPath);
                }
                catch (Exception e1) {

                }
                if (!Constants.BASE_DIR.equals(grandpaDir) && Constants.MAP_FILE.equals(eventPath.getName())) {
                    try {
                        localMap =
                                (Map<String, Map<String, String>>) JSONUtils.deserializeObject(FileUtils
                                    .getFileContent(realPath), Map.class);
                    }
                    catch (Exception e) {
                        log.error("JSON�����л�ʧ��" + realPath, e);
                        localMap = null;
                        continue;
                    }
                    if (log.isInfoEnabled()) {
                        log.info(Constants.MAP_FILE + "�ļ��ı�");
                        log.info("MapFile���ڱ����Ĳ���Ϊ��" + localMap.get(localAddress));
                    }
                }
                else {
                    if (!Constants.BASE_DIR.equals(grandpaDir)) {
                        log.error("��Ч���ļ�������Ŀ¼: " + realPath);
                        continue;
                    }
                    existFiles.put(realPath, System.currentTimeMillis());
                    if (log.isInfoEnabled()) {
                        log.info(realPath + "�ļ�����ӻ����");
                    }
                }
            }
            else if (ev.kind() == StandardWatchEventKind.ENTRY_DELETE) {
                String grandpaDir = null;
                try {
                    grandpaDir = FileUtils.getGrandpaDir(realPath);
                }
                catch (Exception e1) {

                }
                if (!Constants.BASE_DIR.equals(grandpaDir) && Constants.MAP_FILE.equals(eventPath.getName())) {
                    /**
                     * ��Constants.MAP_FILE�ļ���ɾ��ʱ��ɾ��localMap��ӳ���ļ�,
                     */
                    localMap = null;
                    if (log.isInfoEnabled()) {
                        log.info(Constants.MAP_FILE + "�ļ���ɾ��");
                    }
                }
                else {
                    if (Constants.BASE_DIR.equals(grandpaDir)) {
                        // ɾ�������ļ�
                        existFiles.remove(realPath);
                        if (log.isInfoEnabled()) {
                            log.info(realPath + "�ļ�����ɾ��");
                        }
                    }
                    else {
                        // ɾ������Ŀ¼
                        Set<String> keySet = new HashSet<String>(existFiles.keySet());
                        for (String filePath : keySet) {
                            if (filePath.startsWith(realPath)) {
                                existFiles.remove(filePath);
                                if (log.isInfoEnabled()) {
                                    log.info(filePath + "�ļ���ɾ��");
                                }
                            }
                        }

                    }

                }
            }
        }
        return key.reset();
    }
}
