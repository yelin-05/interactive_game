<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <div class="page-head">
    <h1>📋 任务中心</h1>
    <span>累计获得 ${totalReward} 积分</span>
  </div>

  <c:if test="${not empty claimedPoints}">
    <div class="msg ok">领取成功，+<c:out value="${claimedPoints}"/> 积分</div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>
  <c:if test="${not loggedIn}">
    <div class="msg warn">未登录时不计进度。<a href="${ctx}/login?to=/tasks">登录</a>后答题才会累计。</div>
  </c:if>

  <c:choose>
    <c:when test="${empty rows}">
      <div class="panel"><p class="muted">还没有上架的任务。</p></div>
    </c:when>
    <c:otherwise>
      <div class="task-list">
        <c:forEach var="row" items="${rows}">
          <article class="task-card">
            <div class="task-main">
              <div class="task-title">
                <h2><c:out value="${row.task.title}"/></h2>
                <span class="tag-cat">${row.categoryName}</span>
              </div>
              <p class="task-desc"><c:out value="${row.task.description}"/></p>
              <div class="bar"><i style="width:${row.percent}%"></i></div>
              <span class="task-progress">${row.progress} / ${row.task.targetCount} 局</span>
            </div>
            <div class="task-side">
              <div class="task-reward">+${row.task.rewardPoints}<small>积分</small></div>
              <c:choose>
                <c:when test="${row.claimed}">
                  <span class="state-chip done">已领取</span>
                </c:when>
                <c:when test="${row.claimable}">
                  <form action="${ctx}/tasks" method="post">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="taskId" value="${row.task.id}">
                    <button class="btn primary" type="submit">领取奖励</button>
                  </form>
                </c:when>
                <c:otherwise>
                  <span class="state-chip">进行中</span>
                </c:otherwise>
              </c:choose>
            </div>
          </article>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

  <p class="muted">任务进度按 t_score 里符合条件的局数实时计算，答题成绩一入库这里立刻更新。</p>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
