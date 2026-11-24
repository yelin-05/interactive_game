<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <section class="panel" style="max-width:760px;margin:60px auto;">
    <h1>你好，<c:out value="${name}"/>！</h1>
    <p class="muted">当前服务器时间：<c:out value="${serverTime}"/></p>
    <p>本页面由 HelloServlet 提供，用于验证 JavaEE 8 Servlet 的映射、参数、编码与生命周期。</p>
    <p><a class="btn" href="${ctx}/home">返回魔识首页</a></p>
  </section>
</main>
<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
