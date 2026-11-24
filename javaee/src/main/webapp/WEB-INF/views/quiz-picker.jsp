<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <div class="page-head">
    <h1>🎮 答题中心</h1>
    <span>选一个分区，再选难度档位</span>
  </div>

  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>

  <form action="${ctx}/quiz" method="post" id="startForm">
    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
    <input type="hidden" name="action" value="start">

    <h2 class="sec-title">1. 选择分区</h2>
    <div class="pick-grid">
      <c:forEach var="c" items="${categories}">
        <label class="pick-card pick-select">
          <input type="radio" name="category" value="${c.code}" ${c.code == 'history' ? 'checked' : ''}>
          <span class="pick-icon">${c.icon}</span>
          <span class="pick-name">${c.name}</span>
          <span class="pick-desc">${c.desc}</span>
        </label>
      </c:forEach>
    </div>

    <h2 class="sec-title">2. 选择难度</h2>
    <div class="mode-row">
      <c:forEach var="m" items="${['EASY','NORMAL','HARD']}">
        <label class="mode-card">
          <input type="radio" name="mode" value="${m == 'EASY' ? 'easy' : (m == 'NORMAL' ? 'normal' : 'hard')}"
                 ${m == 'NORMAL' ? 'checked' : ''}>
          <span class="mode-name">
            <c:choose>
              <c:when test="${m == 'EASY'}">🌱 简单</c:when>
              <c:when test="${m == 'NORMAL'}">⚡ 普通</c:when>
              <c:otherwise>🔥 困难</c:otherwise>
            </c:choose>
          </span>
          <span class="mode-desc">
            <c:choose>
              <c:when test="${m == 'EASY'}">5 题 · 每题 20 秒 · 只出简单题</c:when>
              <c:when test="${m == 'NORMAL'}">7 题 · 每题 15 秒 · 简单 + 中等</c:when>
              <c:otherwise>10 题 · 每题 10 秒 · 中等 + 困难</c:otherwise>
            </c:choose>
          </span>
        </label>
      </c:forEach>
    </div>

    <div class="form-actions">
      <button class="btn primary lg" type="submit">开始答题</button>
      <span class="muted">答对按难度给分（10/20/30），提前完成另有速度奖励。</span>
    </div>
  </form>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
