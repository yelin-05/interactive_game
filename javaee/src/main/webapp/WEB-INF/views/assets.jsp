<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/inc/head.jspf" %>
<%@ include file="/WEB-INF/views/inc/nav.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<main class="wrap">
  <div class="page-head">
    <h1>🖼️ 素材库</h1>
    <span>共 ${total} 个素材</span>
  </div>

  <c:if test="${not empty okMsg}">
    <div class="msg ok">上传成功</div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="msg bad"><c:out value="${error}"/></div>
  </c:if>

  <c:choose>
    <c:when test="${loggedIn}">
      <section class="panel">
        <h2>上传素材</h2>
        <form class="upload-form" action="${ctx}/assets" method="post" enctype="multipart/form-data">
          <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
          <label class="field">
            <span>名称（选填）</span>
            <input class="input" type="text" name="name" placeholder="不填就用文件名">
          </label>
          <label class="field">
            <span>归类</span>
            <select class="input" name="category">
              <option value="">不归类</option>
              <c:forEach var="c" items="${categories}">
                <option value="${c.code}">${c.name}</option>
              </c:forEach>
            </select>
          </label>
          <label class="field">
            <span>图片（png / jpg / jpeg / gif / webp，≤10MB）</span>
            <input class="input" type="file" name="file" accept="image/*" required>
          </label>
          <button class="btn primary" type="submit">上传</button>
        </form>
      </section>
    </c:when>
    <c:otherwise>
      <div class="msg warn">浏览不需要登录，上传需要先 <a href="${ctx}/login?to=/assets">登录</a>。</div>
    </c:otherwise>
  </c:choose>

  <div class="tabs">
    <a class="tab ${empty category ? 'on' : ''}" href="${ctx}/assets">全部</a>
    <c:forEach var="c" items="${categories}">
      <a class="tab ${category == c.code ? 'on' : ''}" href="${ctx}/assets?category=${c.code}">
        ${c.icon} ${c.name}
      </a>
    </c:forEach>
  </div>

  <c:choose>
    <c:when test="${empty assets}">
      <div class="panel"><p class="muted">这个分类还没有素材。</p></div>
    </c:when>
    <c:otherwise>
      <div class="asset-grid">
        <c:forEach var="a" items="${assets}">
          <figure class="asset">
            <img src="${ctx}/uploads/${a.storedPath}" alt="<c:out value='${a.name}'/>" loading="lazy">
            <figcaption>
              <b><c:out value="${a.name}"/></b>
              <span>${a.sizeKb} KB</span>
            </figcaption>
          </figure>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

  <p class="muted">文件保存在应用外的持久目录，数据库记录文件名、类型和上传者；上传会校验图片内容、尺寸和扩展名，落盘名使用 UUID。</p>
</main>

<%@ include file="/WEB-INF/views/inc/foot.jspf" %>
