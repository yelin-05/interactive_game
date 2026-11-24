<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap narrow">
  <div class="page-head">
    <h1>🤖 大模型生成</h1>
    <span>通道：${empty result ? 'mock' : result.mode}</span>
  </div>

  <section class="panel">
    <h2>按分区 + 主题生成一道题</h2>
    <form class="upload-form" action="${ctx}/llm" method="post">
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
    当前是 <b>Mock 通道</b>：用模板拼出结构正确的题目，接口形状与真实模型一致。
    切真实通道只需在 <code>LlmServlet.generate()</code> 里把 TODO 处的 HTTP 调用补上。
  </p>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
