<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap narrow">
  <section class="user-card">
    <div class="avatar">${initial}</div>
    <div>
      <h1><c:out value="${user.displayName}"/> <span class="tag-cat">${roleLabel}</span></h1>
      <p class="muted">@<c:out value="${user.username}"/></p>
    </div>
  </section>

  <section class="stat-grid">
    <div class="stat"><b>${stats[0]}</b><span>累计答题</span></div>
    <div class="stat"><b>${stats[1]}</b><span>最高分</span></div>
    <div class="stat"><b>${stats[2]}%</b><span>正确率</span></div>
    <div class="stat"><b>${stats[3] == 0 ? '--' : stats[3]}</b><span>总榜名次</span></div>
  </section>

  <section class="panel">
    <h2>最近战绩</h2>
    <c:choose>
      <c:when test="${empty recent}">
        <p class="muted">还没有答题记录。<a href="${ctx}/quiz">去答一局 →</a></p>
      </c:when>
      <c:otherwise>
        <table class="dt">
          <thead><tr><th>得分</th><th>答对</th><th>用时</th><th>分区</th></tr></thead>
          <tbody>
          <c:forEach var="s" items="${recent}">
            <tr>
              <td><b>${s.score}</b></td>
              <td>${s.correctCount} / ${s.totalCount}</td>
              <td>${s.durationSeconds}s</td>
              <td>${s.category}</td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
        <p class="muted"><a href="${ctx}/rank">去排行榜看完整名次 →</a></p>
      </c:otherwise>
    </c:choose>
  </section>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
