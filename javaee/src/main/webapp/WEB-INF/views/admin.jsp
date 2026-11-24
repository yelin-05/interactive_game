<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <div class="page-head">
    <h1>🛠️ 管理后台</h1>
    <span>只读概览</span>
  </div>

  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>

  <section class="kpi-row">
    <div class="kpi"><b>${userCount}</b><span>注册用户</span></div>
    <div class="kpi"><b>${questionCount}</b><span>题库总题数</span></div>
    <div class="kpi"><b>${scoreCount}</b><span>成绩记录</span></div>
    <div class="kpi"><b>${assetCount}</b><span>素材数量</span></div>
  </section>

  <section class="panel">
    <h2>各分区题量</h2>
    <table class="dt">
      <thead><tr><th>分区</th><th style="width:100px">题量</th></tr></thead>
      <tbody>
      <c:forEach var="row" items="${categoryStats}">
        <tr>
          <td>${row[0]}</td>
          <td>${row[1]}</td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </section>

  <section class="panel">
    <h2>最新入库题目</h2>
    <table class="dt">
      <thead><tr><th style="width:60px">ID</th><th>题干</th><th style="width:36%">解析</th></tr></thead>
      <tbody>
      <c:forEach var="q" items="${recentQuestions}">
        <tr>
          <td>${q.id}</td>
          <td><c:out value="${q.question}"/></td>
          <td class="clamp3"><c:out value="${q.explanation}"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </section>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
