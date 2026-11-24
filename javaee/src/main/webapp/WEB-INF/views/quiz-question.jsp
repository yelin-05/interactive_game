<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap narrow">
  <div class="hud">
    <div class="hud-item"><span class="lab">进度</span><b>${round.index + 1} / ${fn:length(round.items)}</b></div>
    <div class="hud-item"><span class="lab">本题剩余</span><b id="timer">${round.secondsPerQuestion}</b></div>
    <div class="hud-item"><span class="lab">已作答</span><b>${round.answeredCount}</b></div>
    <div class="hud-item"><span class="lab">判分</span><b>交卷后</b></div>
  </div>

  <div class="play-top">
    <span class="play-cat">${mode.icon} ${mode.label}</span>
    <span class="muted">第 ${round.index + 1} / ${fn:length(round.items)} 题</span>
  </div>

  <div class="qbox">
    <div class="qbar"><i id="qbar" style="width:100%"></i></div>
    <p class="qtext"><c:out value="${item.question}"/></p>

    <form action="${ctx}/quiz" method="post" id="answerForm">
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <input type="hidden" name="action" value="answer">
      <input type="hidden" name="pick" id="pickInput" value="-1">
      <div class="opts">
        <c:forEach var="opt" items="${item.options}" varStatus="s">
          <button class="opt" type="button" onclick="choose(${s.index})">
            <span class="opt-key">${fn:substring('ABCD', s.index, s.index + 1)}</span>
            <span class="opt-text"><c:out value="${opt}"/></span>
          </button>
        </c:forEach>
      </div>
    </form>

    <form action="${ctx}/quiz" method="post" class="quit-form">
      <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
      <input type="hidden" name="action" value="abandon">
      <button class="btn ghost" type="submit">退出本局</button>
    </form>
    <p class="muted">键盘 1-4 或 A-D 也可作答；超时会自动跳过。</p>
  </div>
</main>

<script>
  var left = ${round.secondsPerQuestion};
  var total = ${round.secondsPerQuestion};
  var timerEl = document.getElementById('timer');
  var barEl = document.getElementById('qbar');
  var submitted = false;

  function tick() {
    left--;
    if (left < 0) left = 0;
    timerEl.textContent = left;
    timerEl.className = left <= 5 ? 'bad' : '';
    barEl.style.width = (left / total * 100) + '%';
    if (left === 0 && !submitted) {
      submitAnswer(-1);
    }
  }

  function submitAnswer(pick) {
    if (submitted) return;
    submitted = true;
    document.getElementById('pickInput').value = pick;
    document.getElementById('answerForm').submit();
  }

  function choose(i) {
    submitAnswer(i);
  }

  setInterval(tick, 1000);

  document.addEventListener('keydown', function (e) {
    var k = e.key.toUpperCase();
    var idx = -1;
    if (k >= '1' && k <= '4') idx = Number(k) - 1;
    else if (k >= 'A' && k <= 'D') idx = k.charCodeAt(0) - 65;
    if (idx >= 0) submitAnswer(idx);
  });
</script>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
