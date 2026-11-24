<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>

<main class="auth-wrap">
  <div class="auth-card">
    <h1>登录</h1>
    <p class="auth-sub">登录后可以进个人中心、保存成绩</p>

    <c:if test="${param.registered == '1'}">
      <div class="msg ok">注册成功，请用新账号登录</div>
    </c:if>
    <c:if test="${param.need == '1'}">
      <div class="msg warn">请先登录再访问该页面</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="msg bad"><c:out value="${error}"/></div>
    </c:if>

    <form class="form" action="${pageContext.request.contextPath}/login" method="post">
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <input type="hidden" name="to" value="<c:out value='${to}'/>">
      <label class="field">
        <span>用户名</span>
        <input class="input" type="text" name="username" value="<c:out value='${username}'/>"
               required autocomplete="username" placeholder="3-20 位字母数字下划线">
      </label>
      <label class="field">
        <span>密码</span>
        <input class="input" type="password" name="password" required
               autocomplete="current-password" placeholder="至少 6 位">
      </label>
      <button class="btn primary block" type="submit">登录</button>
    </form>

    <p class="auth-foot">还没有账号？<a href="${pageContext.request.contextPath}/register">立即注册</a></p>
  </div>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
