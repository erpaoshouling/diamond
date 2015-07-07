package com.taobao.diamond.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ModelMap;

import com.google.common.base.Joiner;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.server.utils.GlobalCounter;
import com.taobao.diamond.utils.JSONUtils;

public class AdminControllerUnitTest extends AbstractControllerUnitTest {

	private AdminController adminController;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.adminController = new AdminController();
		adminController.setAdminService(adminService);
		adminController.setConfigService(configService);
	}

	@Test
	public void testPostConfig_listConfig_getConfigInfo_deleteConfig()
			throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();
		final String dataId = "notify";
		final String group = "boyan";
		final String content = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
		File file = new File(path + "/config-data/boyan/notify");
		try {
			// ����������Ϣ
			assertFalse(file.exists());
			mockServletContext(dataId, group, content);
			assertEquals("/admin/config/list", this.adminController.postConfig(
					request, response, dataId, group, content, modelMap));
			file = new File(path + "/config-data/boyan/notify");
			assertTrue(file.exists());
			assertContent(content, file);
			assertEquals(dataId, modelMap.get("dataId"));
			assertEquals(group, modelMap.get("group"));
			Page<ConfigInfo> page = (Page<ConfigInfo>) modelMap.get("page");
			assertEquals(1, page.getPagesAvailable());
			assertEquals(1, page.getPageNumber());
			assertTrue(page.getPageItems().contains(
					new ConfigInfo(dataId, group, content)));

			// ����������Ϣ
			modelMap = new ModelMap();
			assertEquals("/admin/config/edit",
					this.adminController.getConfigInfo(request, response,
							dataId, group, modelMap));
			ConfigInfo configInfo = (ConfigInfo) modelMap.get("configInfo");
			assertNotNull(configInfo);
			assertEquals(dataId, configInfo.getDataId());
			assertEquals(content, configInfo.getContent());
			assertEquals(group, configInfo.getGroup());

			// ɾ��������Ϣ
			long id = configInfo.getId();
			modelMap = new ModelMap();

			assertEquals("/admin/config/list",
					this.adminController.deleteConfig(request, response, id,
							modelMap));
			assertEquals("ɾ���ɹ�!", modelMap.get("message"));
			// ȷ���ļ�������
			file = new File(path + "/config-data/boyan/notify");
			assertFalse(file.exists());

		} finally {
			file.delete();
		}
	}

	@Test
	public void testDeleteConfigByDataId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();
		final String dataId = "notify";
		final String group = "leiwen";
		final String content = "test delete";
		File file = new File(path + "/config-data/leiwen/notify");
		try {
			// ����������Ϣ
			assertFalse(file.exists());
			mockServletContext(dataId, group, content);
			assertEquals("/admin/config/list", this.adminController.postConfig(
					request, response, dataId, group, content, modelMap));
			file = new File(path + "/config-data/leiwen/notify");
			assertTrue(file.exists());
			assertContent(content, file);
			assertEquals(dataId, modelMap.get("dataId"));
			assertEquals(group, modelMap.get("group"));
			Page<ConfigInfo> page = (Page<ConfigInfo>) modelMap.get("page");
			assertEquals(1, page.getPagesAvailable());
			assertEquals(1, page.getPageNumber());
			assertTrue(page.getPageItems().contains(
					new ConfigInfo(dataId, group, content)));

			// ɾ��������Ϣ
			modelMap = new ModelMap();

			assertEquals("/admin/config/list",
					this.adminController.deleteConfigByDataIdGroup(request,
							response, dataId, group, modelMap));
			assertEquals("ɾ���ɹ�!", modelMap.get("message"));
			// ȷ���ļ�������
			file = new File(path + "/config-data/leiwen/notify");
			assertFalse(file.exists());

		} finally {
			file.delete();
		}
	}

	@Test
	public void testUpdateConfig() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();
		final String dataId = "diamond";
		final String group = "leiwen";
		final String content = "test update";
		File file = new File(path + "/config-data/leiwen/diamond");
		try {
			// ����������Ϣ
			assertFalse(file.exists());
			mockServletContext(dataId, group, content);
			assertEquals("/admin/config/list", this.adminController.postConfig(
					request, response, dataId, group, content, modelMap));
			file = new File(path + "/config-data/leiwen/diamond");
			assertTrue(file.exists());
			assertContent(content, file);
			assertEquals(dataId, modelMap.get("dataId"));
			assertEquals(group, modelMap.get("group"));
			Page<ConfigInfo> page = (Page<ConfigInfo>) modelMap.get("page");
			assertEquals(1, page.getPagesAvailable());
			assertEquals(1, page.getPageNumber());
			assertTrue(page.getPageItems().contains(
					new ConfigInfo(dataId, group, content)));

			// ����������Ϣ
			modelMap = new ModelMap();
			final String newContent = "test update new";
			assertEquals("/admin/config/list",
					this.adminController.updateConfig(request, response,
							dataId, group, newContent, modelMap));
			assertEquals("�ύ�ɹ�!", modelMap.get("message"));
			// ȷ���ļ������Ѹ���
			file = new File(path + "/config-data/leiwen/diamond");
			assertTrue(file.exists());
			assertContent(newContent, file);

		} finally {
			file.delete();
		}
	}

	@Test
	public void testUpload() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockMultipartFile multipartFile = new MockMultipartFile("test.xml",
				"<root>hello</root>".getBytes());
		ModelMap modelMap = new ModelMap();
		mockServletContext("dataId1", "test", "<root>hello</root>");
		assertEquals("/admin/config/list", this.adminController.upload(request,
				response, "dataId1", "test", multipartFile, modelMap));

		File file = new File(path + "/config-data/test/dataId1");
		assertTrue(file.exists());
		assertContent("<root>hello</root>", file);
		file.delete();

		assertEquals("�ύ�ɹ�!", modelMap.get("message"));

	}

	@Test
	public void testReupload() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockMultipartFile multipartFile = new MockMultipartFile("test.xml",
				"<root>hello</root>".getBytes());
		ModelMap modelMap = new ModelMap();
		mockServletContext("dataId1", "test", "<root>hello</root>");
		this.configService.addConfigInfo("dataId1", "test", "old content");
		File file = new File(path + "/config-data/test/dataId1");
		assertTrue(file.exists());
		assertContent("old content", file);
		assertEquals("/admin/config/list", this.adminController.reupload(
				request, response, "dataId1", "test", multipartFile, modelMap));
		file = new File(path + "/config-data/test/dataId1");
		assertTrue(file.exists());
		assertContent("<root>hello</root>", file);
		file.delete();
	}

	/**
	 * ������������, ����mockServletContext��group + dataIdΪ����, �����������������¼�ĸ���Ϊ1,
	 * ��ʵ������������sdk�ĵ�Ԫ�����н���
	 * 
	 * ��������������������ɹ�, �����ݿ���ԭ��û������, �ɹ������������������
	 */
	@Test
	public void testBatchAdd_�ɹ�() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();

		String dataId = UUID.randomUUID().toString() + "-batchAddDataId";
		String group = "test";
		String content = "batchAddSuccess";
		String srcIp = "127.0.0.1";
		String srcUser = "leiwen.zh";
		// ����dataId��content���ַ���
		Map<String/* dataId */, String/* content */> dataId2ContentMap = new HashMap<String, String>();
		for (int i = 0; i < 1; i++) {
			dataId2ContentMap.put(dataId, content);
		}
		String allDataIdAndContent = this
				.generateBatchOpString(dataId2ContentMap);
		// ����
		mockServletContext(dataId, group, content);

		assertEquals("/admin/config/batch_result",
				this.adminController.batchAddOrUpdate(request, response,
						allDataIdAndContent, group, srcIp, srcUser, modelMap));

		// �����л�, ��֤���
		String json = (String) modelMap.get("json");
		Object resultObj = null;
		try {
			resultObj = JSONUtils.deserializeObject(json,
					new TypeReference<List<ConfigInfoEx>>() {
					});
			if (!(resultObj instanceof List<?>)) {
				throw new RuntimeException("�����л�ʧ��, ���Ͳ���List");
			}
		} catch (Exception e) {
			fail("�����л�ʧ��, ���Ͳ���List");
		}
		List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
		assertEquals(1, resultList.size());
		ConfigInfoEx result = resultList.get(0);

		assertEquals(dataId, result.getDataId());
		assertEquals(group, result.getGroup());
		assertEquals(content, result.getContent());
		assertEquals(Constants.BATCH_ADD_SUCCESS, result.getStatus());
		assertEquals("add success", result.getMessage());
	}

	/**
	 * ������������������³ɹ�, �����ݿ�ԭ��������, ��������
	 */
	@Test
	public void testBatchUpdate_�ɹ�() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();

		String dataId = UUID.randomUUID().toString() + "-batchUpdateDataId";
		String group = "test";
		String content = "batchUpdateSuccess";
		String newContent = "batchUpdateSuccess2";
		String srcIp = "127.0.0.1";
		String srcUser = "leiwen.zh";

		// ������һ��
		mockServletContext(dataId, group, content);
		this.adminController.postConfig(request, response, dataId, group,
				content, modelMap);

		// �����������µ��ַ���
		Map<String/* dataId */, String/* content */> dataId2ContentMap = new HashMap<String, String>();
		for (int i = 0; i < 1; i++) {
			dataId2ContentMap.put(dataId, newContent);
		}
		String allDataIdAndContent = this
				.generateBatchOpString(dataId2ContentMap);

		Thread.sleep(1000);

		// ��������
		assertEquals("/admin/config/batch_result",
				this.adminController.batchAddOrUpdate(request, response,
						allDataIdAndContent, group, srcIp, srcUser, modelMap));

		// �����л�, ��֤���
		String json = (String) modelMap.get("json");
		Object resultObj = null;
		try {
			resultObj = JSONUtils.deserializeObject(json,
					new TypeReference<List<ConfigInfoEx>>() {
					});
			if (!(resultObj instanceof List<?>)) {
				throw new RuntimeException("�����л�ʧ��, ���Ͳ���List");
			}
		} catch (Exception e) {
			fail("�����л�ʧ��, ���Ͳ���List");
		}
		List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
		assertEquals(1, resultList.size());
		ConfigInfoEx result = resultList.get(0);

		assertEquals(dataId, result.getDataId());
		assertEquals(group, result.getGroup());
		assertEquals(newContent, result.getContent());
		assertEquals(Constants.BATCH_UPDATE_SUCCESS, result.getStatus());
		assertEquals("update success", result.getMessage());
	}

	/**
	 * �����������������ѯ�ɹ�, �����ݿ�ԭ��������
	 */
	@Test
	public void testBatchQuery_�ɹ�() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();

		String dataId = UUID.randomUUID().toString() + "-batchQueryDataId";
		String group = "test";
		String content = "batchQuerySuccess";

		// ����
		mockServletContext(dataId, group, content);
		this.adminController.postConfig(request, response, dataId, group,
				content, modelMap);

		// ����������ѯ��dataId�ַ���
		List<String> dataIdList = new LinkedList<String>();
		dataIdList.add(dataId);
		String dataIds = this.generateBatchQueryString(dataIdList);

		// ������ѯ
		assertEquals("/admin/config/batch_result",
				this.adminController.batchQuery(request, response, dataIds,
						group, modelMap));

		// �����л�, ��֤���
		String json = (String) modelMap.get("json");
		Object resultObj = null;
		try {
			resultObj = JSONUtils.deserializeObject(json,
					new TypeReference<List<ConfigInfoEx>>() {
					});
			if (!(resultObj instanceof List<?>)) {
				throw new RuntimeException("�����л�ʧ��, ���Ͳ���List");
			}
		} catch (Exception e) {
			fail("�����л�ʧ��, ���Ͳ���List");
		}
		List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
		assertEquals(1, resultList.size());
		ConfigInfoEx result = resultList.get(0);

		assertEquals(dataId, result.getDataId());
		assertEquals(group, result.getGroup());
		assertEquals(content, result.getContent());
		assertEquals(Constants.BATCH_QUERY_EXISTS, result.getStatus());
		assertEquals("query success", result.getMessage());
	}

	/**
	 * �����������������ѯʧ��, �����ݿ��в���������
	 */
	@Test
	public void testBatchQuery_ʧ��() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRemoteAddr("0.0.0.0");
		ModelMap modelMap = new ModelMap();

		String dataId = UUID.randomUUID().toString() + "-batchQueryDataId";
		String group = "test";

		// ����������ѯ��dataId�ַ���
		List<String> dataIdList = new LinkedList<String>();
		dataIdList.add(dataId);
		String dataIds = this.generateBatchQueryString(dataIdList);

		// ������ѯ
		assertEquals("/admin/config/batch_result",
				this.adminController.batchQuery(request, response, dataIds,
						group, modelMap));

		// �����л�, ��֤���
		String json = (String) modelMap.get("json");
		Object resultObj = null;
		try {
			resultObj = JSONUtils.deserializeObject(json,
					new TypeReference<List<ConfigInfoEx>>() {
					});
			if (!(resultObj instanceof List<?>)) {
				throw new RuntimeException("�����л�ʧ��, ���Ͳ���List");
			}
		} catch (Exception e) {
			fail("�����л�ʧ��, ���Ͳ���List");
		}
		List<ConfigInfoEx> resultList = (List<ConfigInfoEx>) resultObj;
		assertEquals(1, resultList.size());
		ConfigInfoEx result = resultList.get(0);

		assertEquals(dataId, result.getDataId());
		assertEquals(group, result.getGroup());
		assertEquals(null, result.getContent());
		assertEquals(Constants.BATCH_QUERY_NONEXISTS, result.getStatus());
		assertEquals("query data does not exist", result.getMessage());
	}

	private String generateBatchOpString(Map<String, String> dataId2ContentMap) {
		List<String> dataIdAndContentList = new LinkedList<String>();
		for (String dataId : dataId2ContentMap.keySet()) {
			String content = dataId2ContentMap.get(dataId);
			dataIdAndContentList.add(dataId + Constants.WORD_SEPARATOR
					+ content);
		}
		String allDataIdAndContent = Joiner.on(Constants.LINE_SEPARATOR).join(
				dataIdAndContentList);
		return allDataIdAndContent;
	}

	private String generateBatchQueryString(List<String> dataIds) {
		return Joiner.on(Constants.WORD_SEPARATOR).join(dataIds);
	}

	@Test
	public void testAddUser_listUser_changePassword_deleteUser()
			throws Exception {
		// �ظ���ӣ�ʧ��
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		try {
			ModelMap modelMap = new ModelMap();

			assertEquals("/admin/user/list", this.adminController.addUser(
					request, response, "admin", "admin", modelMap));
			assertEquals("���ʧ��!", modelMap.get("message"));

			Map<String, String> userMap = (Map<String, String>) modelMap
					.get("userMap");
			assertNotNull(userMap);
			assertEquals("admin", userMap.get("admin"));
			assertEquals(1, userMap.size());

			// ������û�
			modelMap = new ModelMap();
			assertEquals("/admin/user/list", this.adminController.addUser(
					request, response, "boyan", "boyan", modelMap));
			assertEquals("��ӳɹ�!", modelMap.get("message"));
			userMap = (Map<String, String>) modelMap.get("userMap");
			assertNotNull(userMap);
			assertEquals("boyan", userMap.get("boyan"));
			assertEquals(2, userMap.size());

			// �޸�����
			// 1���޸Ĳ����ڵ��û�����
			modelMap = new ModelMap();
			assertEquals("/admin/user/list",
					this.adminController.changePassword(request, response,
							"test", "newpass", modelMap));
			assertEquals("����ʧ��!", modelMap.get("message"));

			// 2���޸�boyan����
			modelMap = new ModelMap();
			assertEquals("/admin/user/list",
					this.adminController.changePassword(request, response,
							"boyan", "newpass", modelMap));
			assertEquals("���ĳɹ�,�´ε�¼���������룡", modelMap.get("message"));
			userMap = (Map<String, String>) modelMap.get("userMap");
			assertNotNull(userMap);
			assertEquals("newpass", userMap.get("boyan"));
			assertEquals(2, userMap.size());

			// ɾ���û�
			// 1��ɾ�������ڵ��û���ʧ��
			modelMap = new ModelMap();
			assertEquals("/admin/user/list", this.adminController.deleteUser(
					request, response, "test", modelMap));
			assertEquals("ɾ��ʧ��!", modelMap.get("message"));

			// 2��ɾ��boyan
			modelMap = new ModelMap();
			assertEquals("/admin/user/list", this.adminController.deleteUser(
					request, response, "boyan", modelMap));
			assertEquals("ɾ���ɹ�!", modelMap.get("message"));
			userMap = (Map<String, String>) modelMap.get("userMap");
			assertNotNull(userMap);
			assertNull(userMap.get("boyan"));
			assertEquals(1, userMap.size());
		} finally {
			this.adminController.deleteUser(request, response, "boyan",
					new ModelMap());
		}

	}

	@Test
	public void testReloadUser() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();

		assertEquals("/admin/user/list",
				this.adminController.reloadUser(request, response, modelMap));
		Map<String, String> allUserMap = this.adminController.getAdminService()
				.getAllUsers();
		assertEquals("admin", allUserMap.get("admin"));
		assertEquals("���سɹ�!", modelMap.get("message"));
	}

	@Test
	public void testSetRefuseRequstCount_GetRefuseRequestCount() {
		ModelMap modelMap = new ModelMap();
		// ���õļ���С�ڵ���0
		assertEquals("/admin/count",
				this.adminController.setRefuseRequestCount(0, modelMap));
		assertEquals("�Ƿ��ļ���", modelMap.get("message"));
		assertNull(modelMap.get("count"));

		modelMap = new ModelMap();
		assertEquals("/admin/count",
				this.adminController.setRefuseRequestCount(100, modelMap));
		assertEquals("���óɹ�!", modelMap.get("message"));
		assertEquals(100L, modelMap.get("count"));

		GlobalCounter.getCounter().set(0);
	}

	@Test
	public void testPostConfigFail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/config/new", this.adminController.postConfig(
				request, response, "", "hello", "test", modelMap));
		assertEquals("��Ч��DataId", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/new", this.adminController.postConfig(
				request, response, "notify", "hello test", "test", modelMap));
		assertEquals("��Ч�ķ���", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/new", this.adminController.postConfig(
				request, response, "notify", "hello", null, modelMap));
		assertEquals("��Ч������", modelMap.get("message"));

	}

	@Test
	public void testUpdateConfigFail() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/config/edit", this.adminController.updateConfig(
				request, response, "", "hello", "test", modelMap));
		assertEquals("��Ч��DataId", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/edit", this.adminController.updateConfig(
				request, response, "notify", "hello test", "test", modelMap));
		assertEquals("��Ч�ķ���", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/edit", this.adminController.updateConfig(
				request, response, "notify", "hello", null, modelMap));
		assertEquals("��Ч������", modelMap.get("message"));
	}

	@Test
	public void testUploadFail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockMultipartFile multipartFile = new MockMultipartFile("test.xml",
				"<root>hello</root>".getBytes());
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/config/upload", this.adminController.upload(
				request, response, "", "hello", multipartFile, modelMap));
		assertEquals("��Ч��DataId", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/upload", this.adminController.upload(
				request, response, "notify", "hello test", multipartFile,
				modelMap));
		assertEquals("��Ч�ķ���", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		multipartFile = new MockMultipartFile("test.xml", "".getBytes());
		assertEquals("/admin/config/upload", this.adminController.upload(
				request, response, "notify", "hello", multipartFile, modelMap));
		assertEquals("��Ч������", modelMap.get("message"));
	}

	@Test
	public void testReuploadFail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockMultipartFile multipartFile = new MockMultipartFile("test.xml",
				"<root>hello</root>".getBytes());
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/config/edit", this.adminController.reupload(
				request, response, "", "hello", multipartFile, modelMap));
		assertEquals("��Ч��DataId", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		assertEquals("/admin/config/edit", this.adminController.reupload(
				request, response, "notify", "hello test", multipartFile,
				modelMap));
		assertEquals("��Ч�ķ���", modelMap.get("message"));

		response = new MockHttpServletResponse();
		modelMap = new ModelMap();
		multipartFile = new MockMultipartFile("test.xml", "".getBytes());
		assertEquals("/admin/config/edit", this.adminController.reupload(
				request, response, "notify", "hello", multipartFile, modelMap));
		assertEquals("��Ч������", modelMap.get("message"));
	}

	@Test
	public void testAddUser_fail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/user/list", this.adminController.addUser(request,
				response, "", "123", modelMap));
		assertEquals("��Ч���û���", modelMap.get("message"));

		modelMap = new ModelMap();
		assertEquals("/admin/user/new", this.adminController.addUser(request,
				response, "leiwen", "", modelMap));
		assertEquals("��Ч������", modelMap.get("message"));
	}

	@Test
	public void testDeleteUser_fail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/user/list", this.adminController.deleteUser(
				request, response, "", modelMap));
		assertEquals("��Ч���û���", modelMap.get("message"));
	}

	@Test
	public void testChangePassword_fail() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/user/list", this.adminController.changePassword(
				request, response, "", "123", modelMap));
		assertEquals("��Ч���û���", modelMap.get("message"));

		modelMap = new ModelMap();
		assertEquals("/admin/user/list", this.adminController.changePassword(
				request, response, "leiwen", "", modelMap));
		assertEquals("��Ч��������", modelMap.get("message"));
	}

	private void assertContent(final String content, File file)
			throws FileNotFoundException, IOException {
		// У������
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String line = reader.readLine();
		assertNotNull(line);
		assertEquals(content, line);
		reader.close();
	}

	@Test
	public void testListConfigJson() throws Exception {
		ModelMap modelMap = new ModelMap();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		final String dataId = "test-dataId";
		final String group = "test-group";

		mockServletContext(dataId, group, "content");
		this.configService.addConfigInfo(dataId, group, "content");
		this.mocksControl.verify();
		this.mocksControl.reset();

		request.addHeader("Accept", "application/json");
		assertEquals("/admin/config/list_json",
				this.adminController.listConfig(request, response, dataId,
						group, 1, 15, modelMap));
		String json = (String) modelMap.get("pageJson");
		assertNotNull(json);
		Page<ConfigInfo> page = (Page<ConfigInfo>) JSONUtils.deserializeObject(
				json, new TypeReference<Page<ConfigInfo>>() {
				});
		assertEquals(1, page.getPageNumber());
		assertEquals(1, page.getPagesAvailable());
		assertEquals(1, page.getTotalCount());
		assertEquals(1, page.getPageItems().size());
		ConfigInfo configInfo = page.getPageItems().get(0);
		assertEquals(dataId, configInfo.getDataId());
		assertEquals(group, configInfo.getGroup());
		assertEquals("content", configInfo.getContent());
	}

	@Test
	public void testListConfigInfoLike() {

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		// û�����ò�ѯ���������
		ModelMap modelMap = new ModelMap();
		assertEquals("/admin/config/list", this.adminController.listConfigLike(
				new MockHttpServletRequest(), response, null, null, 1, 100,
				modelMap));
		assertEquals("ģ����ѯ����������һ����ѯ����", modelMap.get("message"));
		assertNull(modelMap.get("page"));

		// Ԥ����Ӳ�������
		final String dataId = "test-dataId";
		final String group = "test-group";
		for (int i = 0; i < 10; i++) {
			mockServletContext(dataId + i, group + i, "content" + i);
			this.configService.addConfigInfo(dataId + i, group + i, "content"
					+ i);
			this.mocksControl.verify();
			this.mocksControl.reset();
		}

		// ģ����ѯ
		modelMap = new ModelMap();
		assertEquals("/admin/config/list", this.adminController.listConfigLike(
				new MockHttpServletRequest(), response, dataId, group, 1, 100,
				modelMap));
		assertEquals("listConfigLike", modelMap.get("method"));
		Page<ConfigInfo> page = (Page<ConfigInfo>) modelMap.get("page");
		assertNotNull(page);
		assertEquals(1, page.getPageNumber());
		assertEquals(1, page.getPagesAvailable());
		assertEquals(10, page.getPageItems().size());
	}

	@Test
	public void testListConfigInfoLikeJson() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		// û�����ò�ѯ���������
		ModelMap modelMap = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		assertEquals("/admin/config/list", this.adminController.listConfigLike(
				request, response, null, null, 1, 100, modelMap));
		assertEquals("ģ����ѯ����������һ����ѯ����", modelMap.get("message"));
		assertNull(modelMap.get("page"));

		// Ԥ����Ӳ�������
		final String dataId = "test-dataId";
		final String group = "test-group";
		for (int i = 0; i < 10; i++) {
			mockServletContext(dataId + i, group + i, "content" + i);
			this.configService.addConfigInfo(dataId + i, group + i, "content"
					+ i);
			this.mocksControl.verify();
			this.mocksControl.reset();
		}

		// ģ����ѯ
		modelMap = new ModelMap();
		request.addHeader("Accept", "application/json");
		assertEquals("/admin/config/list_json",
				this.adminController.listConfigLike(request, response, dataId,
						group, 1, 100, modelMap));
		String json = (String) modelMap.get("pageJson");
		assertNotNull(json);
		Page<ConfigInfo> page = (Page<ConfigInfo>) JSONUtils.deserializeObject(
				json, new TypeReference<Page<ConfigInfo>>() {
				});
		assertEquals(1, page.getPageNumber());
		assertEquals(1, page.getPagesAvailable());
		assertEquals(10, page.getTotalCount());
		assertEquals(10, page.getPageItems().size());
	}
}
