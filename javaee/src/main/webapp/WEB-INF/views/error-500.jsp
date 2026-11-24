<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>500 · 魔识</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/site.css">
</head>
<body>
<main class="wrap narrow" style="text-align:center;padding-top:80px">
  <h1>500</h1>
  <p class="muted">服务器出错了。详细信息已经打印到 Tomcat 日志里。</p>
  <p class="muted">${pageContext.exception}</p>
  <p><a class="btn primary" href="${pageContext.request.contextPath}/home">回首页</a></p>
</main>
</body>
</html>
