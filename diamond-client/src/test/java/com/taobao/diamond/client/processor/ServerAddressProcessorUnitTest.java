package com.taobao.diamond.client.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import com.taobao.diamond.client.DiamondConfigure;

public class ServerAddressProcessorUnitTest {

	@Test
	public void test_�����ȡ�����ӱ��ػ�ȡ() throws Exception {
		DiamondConfigure diamondConfigure = getDiamondConfig();
		// ���ô���ĵ�ַ�Ͷ˿ڣ����´������ȡ����
		diamondConfigure.setConfigServerAddress("localhost");
		diamondConfigure.setConfigServerPort(8880);
		File tmpFile = File.createTempFile("serverAddrProcess", "tmp");
		diamondConfigure.setFilePath(tmpFile.getParent());
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, Executors.newSingleThreadScheduledExecutor());
		processor.setClusterType("diamond");
		File serverAddressFile = new File(diamondConfigure.getFilePath(),
				"ServerAddress");
		Assert.assertTrue(!serverAddressFile.exists());
		// ������д�뱾���ļ�
		createAndWriteLocalFile(serverAddressFile, "server");
		try {
			processor.start();

			Assert.assertTrue(serverAddressFile.exists());
			Assert.assertEquals("server", diamondConfigure.getDomainNameList()
					.get(0));
		} finally {
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			if (serverAddressFile.exists()) {
				serverAddressFile.delete();
			}
		}
	}

	@Test
	public void test_����ʧ��() throws Exception {
		DiamondConfigure diamondConfigure = getDiamondConfig();
		// ���ô���ĵ�ַ�Ͷ˿ڣ����´������ȡ����
		diamondConfigure.setConfigServerAddress("localhost");
		diamondConfigure.setConfigServerPort(8880);
		File tmpFile = File.createTempFile("serverAddrProcess", "tmp");
		diamondConfigure.setFilePath(tmpFile.getParent());
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, Executors.newSingleThreadScheduledExecutor());
		processor.setClusterType("diamond");
		// ����û�У�get��ʧ�ܣ�����ʧ��
		try {
			processor.start();
			Assert.fail();

		} catch (RuntimeException e) {

		} finally {
			tmpFile.delete();
		}

	}

	@Test
	public void test_ͬ���������ȡ��ַ() throws Exception {
		// diamond server address (ip)
		String expectedServerAddress = "";

		DiamondConfigure diamondConfigure = getDiamondConfig();
		File tmpFile = File.createTempFile("serverAddrProcess", "tmp");
		diamondConfigure.setFilePath(tmpFile.getParent());
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, Executors.newSingleThreadScheduledExecutor());
		processor.setClusterType("diamond");
		File serverAddressFile = new File(diamondConfigure.getFilePath(),
				"ServerAddress");
		Assert.assertTrue(!serverAddressFile.exists());
		try {
			processor.start();
			// ���ճ�������ȡ��ַ�б�
			processor.synAcquireServerAddress();

			Assert.assertTrue(serverAddressFile.exists());
			Assert.assertTrue(compareAddress(serverAddressFile,
					expectedServerAddress));
		} finally {
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			if (serverAddressFile.exists()) {
				serverAddressFile.delete();
			}
		}

	}

	@Test
	public void test_�첽�������ȡ��ַ() throws Exception {
		DiamondConfigure diamondConfigure = getDiamondConfig();
		File tmpFile = File.createTempFile("serverAddrProcess", "tmp");
		diamondConfigure.setFilePath(tmpFile.getParent());
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, Executors.newSingleThreadScheduledExecutor());
		processor.setClusterType("diamond");
		processor.setAsynAcquireIntervalInSec(10);
		File serverAddressFile = new File(diamondConfigure.getFilePath(),
				"ServerAddress");
		Assert.assertTrue(!serverAddressFile.exists());
		try {
			// diamond server address (ip)
			String serverAddr = "";
			processor.start();
			processor.synAcquireServerAddress();

			Assert.assertTrue(serverAddressFile.exists());
			Assert.assertTrue(compareAddress(serverAddressFile, serverAddr));

			// �ı�ServerAddress������
			modifyServerAddressFile(serverAddressFile);
			Assert.assertFalse(compareAddress(serverAddressFile, serverAddr));

			Thread.sleep(15000);
			Assert.assertTrue(compareAddress(serverAddressFile, serverAddr));
		} finally {
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			if (serverAddressFile.exists()) {
				serverAddressFile.delete();
			}
		}
	}

	@Test
	public void testServerAddresses_�����ļ���ȡ�ļ�() {
		DiamondConfigure diamondConfigure = getDiamondConfig();
		diamondConfigure.addDomainName("serverAddress1");
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, null);
		processor.storeServerAddressesToLocal();
		diamondConfigure.getDomainNameList().clear();
		processor.reloadServerAddresses();
		Assert.assertEquals(1, diamondConfigure.getDomainNameList().size());
		Assert.assertEquals("serverAddress1", diamondConfigure
				.getDomainNameList().get(0));

		diamondConfigure.getDomainNameList().add("serverAddress2");
		processor.storeServerAddressesToLocal();
		diamondConfigure.getDomainNameList().clear();
		diamondConfigure.getPushitDomainNameList().clear();
		processor.reloadServerAddresses();
		Assert.assertEquals(2, diamondConfigure.getDomainNameList().size());
		Assert.assertEquals(2, diamondConfigure.getPushitDomainNameList()
				.size());
		Assert.assertEquals("serverAddress1", diamondConfigure
				.getDomainNameList().get(0));
		Assert.assertEquals("serverAddress2", diamondConfigure
				.getDomainNameList().get(1));
	}

	@Test
	public void test_���ȴӱ��ػ�ȡ��ַ() throws Exception {
		DiamondConfigure diamondConfigure = getDiamondConfig();
		diamondConfigure.setLocalFirst(true);
		File tmpFile = File.createTempFile("serverAddrProcess", "tmp");
		diamondConfigure.setFilePath(tmpFile.getParent());
		ServerAddressProcessor processor = new ServerAddressProcessor(
				diamondConfigure, Executors.newSingleThreadScheduledExecutor());
		processor.setClusterType("diamond");

		File serverAddressFile = new File(diamondConfigure.getFilePath(),
				"ServerAddress");
		try {
			createAndWriteLocalFile(serverAddressFile, "serverAddress");

			processor.start();
			Assert.assertTrue(compareAddress(serverAddressFile, "serverAddress"));
		} finally {
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
			if (serverAddressFile.exists()) {
				serverAddressFile.delete();
			}
		}

	}

	private DiamondConfigure getDiamondConfig() {
		DiamondConfigure diamondConfigure = new DiamondConfigure("diamond");
		return diamondConfigure;
	}

	private boolean compareAddress(File serverAddressFile, String expected)
			throws FileNotFoundException, IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(serverAddressFile)));
			String line = reader.readLine();
			while (line != null) {
				if (expected.equals(line)) {
					return true;
				}
				line = reader.readLine();
			}
			return false;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void modifyServerAddressFile(File serverAddressFile)
			throws Exception {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(serverAddressFile));
			// ip
			writer.println("");
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void createAndWriteLocalFile(File file, String content) {
		PrintWriter pw = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			pw = new PrintWriter(new FileWriter(file));
			pw.println(content);
			pw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

}
