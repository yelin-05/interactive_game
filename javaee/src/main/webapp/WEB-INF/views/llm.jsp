<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap narrow">
  <div class="page-head">
    <h1>🤖 大模型生成</h1>
    <span>通道：${llmMode}</span>
  </div>

  <c:if test="${not empty sessionScope.loginUserId}">
  <section class="panel">
    <h2>按分区 + 主题生成一道题</h2>
    <form class="upload-form" action="${ctx}/llm" method="post">
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <label class="field">
        <span>分区</span>
        <select class="input" name="category">
          <c:forEach var="c" items="${categories}">
            <option value="${c.code}" ${category == c.code ? 'selected' : ''}>${c.icon} ${c.name}</option>
          </c:forEach>
        </select>
      </label>
      <label class="field">
        <span>主题（选填）</span>
        <input class="input" type="text" name="topic" value="<c:out value='${topic}'/>"
               placeholder="例如：TCP 三次握手、宋词">
      </label>
      <button class="btn primary" type="submit">生成题目</button>
    </form>
  </section>
  </c:if>
  <c:if test="${empty sessionScope.loginUserId}">
    <div class="msg warn">生成题目需要先 <a href="${ctx}/login?to=/llm">登录</a>，避免匿名消耗模型额度。</div>
  </c:if>

  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>

  <c:if test="${not empty result}">
    <section class="panel">
      <h2>生成结果</h2>
      <p class="qtext"><c:out value="${result.question}"/></p>
      <div class="opts">
        <c:forEach var="o" items="${result.options}" varStatus="s">
          <div class="opt ${s.index == result.answerIndex ? 'correct' : ''}">
            <span class="opt-key">${fn:substring('ABCD', s.index, s.index + 1)}</span>
            <span class="opt-text"><c:out value="${o}"/></span>
          </div>
        </c:forEach>
      </div>
      <p class="explain"><c:out value="${result.explanation}"/></p>
    </section>
  </c:if>

  <p class="muted">
    默认使用 <b>Mock 通道</b>验证题目结构；配置 <code>MOSHI_LLM_MODE=real</code>、
    <code>MOSHI_LLM_BASE_URL</code>、<code>MOSHI_LLM_API_KEY</code> 和 <code>MOSHI_LLM_MODEL</code>
    后可调用 OpenAI 兼容接口。密钥只从环境变量或系统属性读取，不写入仓库。
  </p>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
