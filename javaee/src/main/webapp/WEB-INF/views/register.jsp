<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>

<main class="auth-wrap">
  <div class="auth-card">
    <h1>注册</h1>
    <p class="auth-sub">密码用 BCrypt 加盐摘要存储，数据库里看不到明文</p>

    <c:if test="${not empty error}">
      <div class="msg bad"><c:out value="${error}"/></div>
    </c:if>

    <form class="form" action="${pageContext.request.contextPath}/register" method="post">
      <label class="field">
        <span>用户名</span>
        <input class="input" type="text" name="username" value="<c:out value='${username}'/>"
               required autocomplete="username" placeholder="3-20 位字母、数字或下划线">
      </label>
      <label class="field">
        <span>昵称（选填）</span>
        <input class="input" type="text" name="nickname" value="<c:out value='${nickname}'/>"
               placeholder="不填就用用户名">
      </label>
      <label class="field">
        <span>密码</span>
        <input class="input" type="password" name="password" required
               autocomplete="new-password" placeholder="至少 6 位">
      </label>
      <label class="field">
        <span>确认密码</span>
        <input class="input" type="password" name="password2" required
               autocomplete="new-password" placeholder="再输入一次">
      </label>
      <button class="btn primary block" type="submit">注册</button>
    </form>

    <p class="auth-foot">已有账号？<a href="${pageContext.request.contextPath}/login">去登录</a></p>
  </div>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
