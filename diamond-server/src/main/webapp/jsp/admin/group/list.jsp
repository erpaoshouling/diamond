 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond������Ϣ����</title>
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("��ȷ��Ҫɾ���÷�����Ϣ��??");  
   }
   
   function moveGroup(address,oldGroup,link){
       var newGroup=window.prompt("������Ŀ���������");
       if(newGroup==null||newGroup.length==0)
         return false;
       link.href=link.href+"&newGroup="+newGroup;
       return window.confirm("��ȷ��Ҫ��"+address+"��"+oldGroup+"�ƶ���"+newGroup+"��??");  
   }
  
</script>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>������Ϣ����</strong></h1></center>
   <p align='center'>
     <c:if test="${groupMap!=null}">
      <table border='1' width="800">
          <tr>
              <td>IP��ַ</td>
              <td>dataId</td>
              <td>����</td>
              <td>����</td>
          </tr>
          <c:forEach items="${groupMap}" var="entry">
           <c:forEach items="${entry.value}" var="groupInfo">
            <tr>
               <td>
                  <c:out value="${entry.key}"/>
               </td>
              <td name="tagDataID">
                  <c:out value="${groupInfo.key}" />
               </td>
              <td name="tagGroup">
                  <c:out value="${groupInfo.value.group}" />
               </td>
              <c:url var="moveGroupUrl" value="/admin.do" >
                  <c:param name="method" value="moveGroup" />
                  <c:param name="id" value="${groupInfo.value.id}" />
               </c:url>
               <c:url var="deleteGroupUrl" value="/admin.do" >
                  <c:param name="method" value="deleteGroup" />
                   <c:param name="id" value="${groupInfo.value.id}" />
               </c:url>
              <td>
                 <a href="${moveGroupUrl}" onclick="return moveGroup('${entry.key}','${groupInfo.value.group}',this);">�ƶ�������</a>&nbsp;&nbsp;&nbsp;
                 <a href="${deleteGroupUrl}" onclick="return confirmForDelete();">ɾ��</a>&nbsp;&nbsp;&nbsp;
              </td>
            </tr>
            </c:forEach>
          </c:forEach>
       </table>
    </c:if>
  </p>
  <p align='center'>
    <a href="<c:url value='/jsp/admin/group/new.jsp' />">��ӷ�����Ϣ</a> &nbsp;&nbsp;&nbsp;&nbsp;<a href="<c:url value='/admin.do?method=reloadGroup' />">���¼��ط�����Ϣ</a>
  </p>
</body>
</html>