<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap narrow">
  <div class="result-card">
    <h1>本局结束</h1>
    <div class="big-score">${result.score}</div>
    <p class="muted">${categoryName} · ${mode.icon} ${mode.label}</p>

    <div class="stat-line"><span>答对题数</span><span>${result.correct} / ${result.total}</span></div>
    <div class="stat-line"><span>得分构成</span><span>基础 ${result.baseScore} + 速度 ${result.speedBonus}</span></div>
    <div class="stat-line"><span>用时</span><span>${result.durationSeconds} 秒</span></div>
    <div class="stat-line"><span>成绩</span>
      <span class="${result.saved ? 'ok' : 'warn'}">${result.saved ? '已入库' : '未登录，未保存'}</span>
    </div>

    <div class="detail-list">
      <c:forEach var="d" items="${result.details}" varStatus="s">
        <div class="detail ${d.correct ? 'ok' : 'bad'}">
          <div class="detail-head">${d.correct ? '✓' : '✗'} ${s.index + 1}. <c:out value="${d.question}"/></div>
          <div class="detail-body">
            你的选择：${d.yourLetter}　正确答案：${d.correctLetter}
            <c:if test="${not empty d.explanation}">　—　<c:out value="${d.explanation}"/></c:if>
          </div>
        </div>
      </c:forEach>
    </div>

    <div class="form-actions">
      <a class="btn primary" href="${ctx}/quiz">再来一局</a>
      <a class="btn" href="${ctx}/rank">看排行榜</a>
      <c:if test="${not result.saved}">
        <a class="btn" href="${ctx}/login">登录后成绩才会保存</a>
      </c:if>
    </div>
  </div>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
