package com.taobao.diamond.client.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.configinfo.ConfigureInfomation;


public class DefaultDiamondSubscriber_diamondserverUnitTest {

    private DefaultDiamondSubscriber subscriber = null;
    private SubscriberListener listener = null;


    @Test
    public void test_��������_diamondserver�������ݻ��������() throws Exception {
        subscriber = new DefaultDiamondSubscriber(listener);

        final java.util.concurrent.atomic.AtomicBoolean invoked = new AtomicBoolean(false);
        this.subscriber.setSubscriberListener(new SubscriberListener() {

            public void receiveConfigInfo(ConfigureInfomation configureInfomation) {
                System.out.println("���յ�������Ϣ" + configureInfomation);
                invoked.set(true);
            }


            public Executor getExecutor() {

                return null;
            }
        });
        this.subscriber.addDataId("diamondserver", "test");

        int i = 0;
        while (!invoked.get()) {
            Thread.sleep(2000);
            System.out.println("�ȴ�֪ͨ " + i + " ��");
            i++;
        }
    }

}
