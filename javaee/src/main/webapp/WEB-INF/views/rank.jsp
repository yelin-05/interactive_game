<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <div class="page-head">
    <h1>🏆 排行榜</h1>
    <span>共 ${totalRecords} 条成绩 · 当前：${categoryName}（前 10）</span>
  </div>

  <div class="tabs">
    <a class="tab ${empty category ? 'on' : ''}" href="${ctx}/rank">总榜</a>
    <c:forEach var="c" items="${categories}">
      <a class="tab ${category == c.code ? 'on' : ''}" href="${ctx}/rank?category=${c.code}">
        ${c.icon} ${c.name}
      </a>
    </c:forEach>
  </div>

  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>

  <c:choose>
    <c:when test="${empty rows}">
      <div class="panel">
        <p class="muted">这个榜单还没有成绩。去「答题中心」玩一局，交卷后成绩会自动入库。</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="panel">
        <table class="dt">
          <thead>
          <tr>
            <th style="width:64px">名次</th>
            <th>玩家</th>
            <th style="width:80px">得分</th>
            <th style="width:90px">答对</th>
            <th style="width:80px">用时</th>
            <th style="width:110px">时间</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td><span class="rank-no ${r.rank <= 3 ? 'top' : ''}">${r.rank}</span></td>
              <td><c:out value="${r.nickname}"/></td>
              <td><b>${r.score}</b></td>
              <td>${r.correct} / ${r.total}</td>
              <td>${r.durationSeconds}s</td>
              <td>${r.createdAt}</td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>

  <p class="muted">排序规则：得分降序，同分按用时升序。</p>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
