<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>

<header class="hero">
  <h1>魔识</h1>
  <p class="sub">互动答题 · 任务 · 素材库 · 排行榜</p>
  <div class="hero-actions">
    <a class="btn primary lg" href="${pageContext.request.contextPath}/quiz">开始答题</a>
    <a class="btn lg" href="${pageContext.request.contextPath}/rank">看排行榜</a>
  </div>
  <p class="db-line">
    数据库：<span class="${dbOk ? 'ok' : 'bad'}">${dbOk ? '连接正常' : '未连接'}</span>
    · 注册用户 ${userCount} 人
  </p>
</header>

<main class="wrap">
  <section class="module-row">
    <a class="mod" href="${pageContext.request.contextPath}/quiz">
      <span class="mod-icon">🎮</span>
      <div class="mod-body">
        <div class="mod-head"><span class="mod-name">答题中心</span><span class="mod-tag">已上线</span></div>
        <p class="mod-desc">五分区题库 · 三档难度 · 服务端判分</p>
      </div>
    </a>
    <a class="mod" href="${pageContext.request.contextPath}/rank">
      <span class="mod-icon">🏆</span>
      <div class="mod-body">
        <div class="mod-head"><span class="mod-name">排行榜</span><span class="mod-tag">已上线</span></div>
        <p class="mod-desc">总榜 + 分区榜 · 分数降序</p>
      </div>
    </a>
    <a class="mod" href="${pageContext.request.contextPath}/tasks">
      <span class="mod-icon">📋</span>
      <div class="mod-body">
        <div class="mod-head"><span class="mod-name">任务中心</span><span class="mod-tag">已上线</span></div>
        <p class="mod-desc">进度按成绩实时计算 · 可领奖励</p>
      </div>
    </a>
    <a class="mod" href="${pageContext.request.contextPath}/assets">
      <span class="mod-icon">🖼️</span>
      <div class="mod-body">
        <div class="mod-head"><span class="mod-name">素材库</span><span class="mod-tag">已上线</span></div>
        <p class="mod-desc">图片上传 · 按分区浏览</p>
      </div>
    </a>
  </section>

  <section class="panel">
    <h2>这个项目是什么</h2>
    <p>
      「魔识」是一个互动答题平台：用户在五个知识分区（计算机 / 文学 / 地理 / 历史 / 科学）里
      按难度档位答题，交卷后由服务端判分，成绩入库并进入排行榜；
      完成指定局数可以领取任务奖励；管理员可以在后台查看运营数据。
    </p>
    <p class="muted">
      技术栈：JavaEE 8（<code>javax.servlet.*</code>）+ Tomcat 9 + JSP/JSTL + 原生 JDBC + MySQL 8。
      账号由部署者初始化，页面不公开任何固定密码。
    </p>
  </section>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
