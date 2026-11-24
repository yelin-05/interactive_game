# 魔识 JavaEE 8 部署说明

课程主项目是 `javaee/`：Servlet + JSP/JSTL + Service + 原生 JDBC，打包为 `moshi.war`，部署到外置 Tomcat 9。根目录 Spring Boot 项目仅作为扩展参考，不是课程评分主工程。

## 1. 环境要求

- JDK 11（课程允许 JDK 8/11，本项目使用 Java 11 API 编译）
- Apache Tomcat 9.0.x
- MySQL 8.0+（本机验证使用 MySQL 9.7）
- Maven Wrapper，无需单独安装 Maven

## 2. 初始化 MySQL

先创建数据库和表。脚本可重复执行，不会清空已有数据：

```bash
mysql --default-character-set=utf8mb4 -u root -p < javaee/src/main/resources/db/schema-mysql.sql
```

正式部署时创建最小权限账号，不要把 root 密码写入代码：

```sql
CREATE USER 'moshi_app'@'localhost' IDENTIFIED BY '请替换为强密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON moshi.* TO 'moshi_app'@'localhost';
FLUSH PRIVILEGES;
```

仅在本机教学演示时，可额外导入固定演示账号：

```bash
mysql --default-character-set=utf8mb4 -u root -p moshi < javaee/src/main/resources/db/data-demo-mysql.sql
```

`data-demo-mysql.sql` 含公开演示密码，禁止用于公网。正式部署不导入该文件，注册玩家后通过数据库手动初始化管理员，并立即修改密码。

应用启动时会幂等导入 `db/questions*.json` 和 `db/tasks.json`；表为空时写入，非空时跳过。设置 `MOSHI_DB_SEED=false` 可关闭。

## 3. 配置并构建

Windows PowerShell 示例：

```powershell
$env:MOSHI_DB_URL = "jdbc:mysql://localhost:3306/moshi?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
$env:MOSHI_DB_USER = "moshi_app"
$env:MOSHI_DB_PASSWORD = "请替换为强密码"
$env:MOSHI_LLM_MODE = "mock"

.\mvnw.cmd -f javaee/pom.xml clean test package
```

测试使用 H2 内存库，不需要 MySQL。构建产物为 `javaee/target/moshi.war`。

数据库用户名和密码没有代码默认值，缺失时应用会明确提示配置错误。生产环境建议为 JDBC 连接启用 TLS，并按部署环境设置 `useSSL=true` 与可信证书。

## 4. 部署到 Tomcat 9

1. 停止 Tomcat。
2. 备份 `webapps/moshi.war` 和 `webapps/moshi/`（如果存在）。
3. 复制 `javaee/target/moshi.war` 到 `Tomcat 9/webapps/`。WAR 文件名决定上下文路径，因此必须保留 `moshi.war`。
4. 在同一进程环境中设置第 3 节的 `MOSHI_DB_*` 变量；修改系统环境变量后需要重启 Tomcat。
5. 启动 Tomcat，确认 `java -version` 与 `version.bat` 输出的 JVM 均为 11.x，再访问 `http://localhost:8080/moshi/home`。
6. 访问 `http://localhost:8080/moshi/hello` 验证课程基础 Servlet，访问 `/moshi/health` 检查数据库；健康接口返回 200 且 `status`、`db` 均为 `UP`。

不要只复制源码目录到 `webapps`。WAR 应包含 `WEB-INF/classes`、`WEB-INF/lib` 和 `WEB-INF/web.xml`。

## 5. 大模型配置

默认 `mock` 通道不访问网络。真实通道使用 OpenAI 兼容的 `/chat/completions` 接口：

```text
MOSHI_LLM_MODE=real
MOSHI_LLM_BASE_URL=https://api.deepseek.com/v1
MOSHI_LLM_API_KEY=仅在运行环境设置
MOSHI_LLM_MODEL=deepseek-chat
MOSHI_LLM_TIMEOUT_SECONDS=30
```

API Key 只从环境变量或 JVM 系统属性读取，不写入仓库。生成接口要求登录并校验 CSRF，单会话最短请求间隔 2 秒；响应会校验四选项、答案下标、题干和解析，非 2xx、超时或结构错误均返回通用提示，不泄露密钥或上游明细。

## 6. Docker Compose（可选）

```bash
cp javaee/.env.example javaee/.env
# 修改 javaee/.env 中的数据库密码
docker compose -f javaee/docker-compose.yml up --build
```

访问 `http://localhost:18080/home`。`.env` 中还可设置 `MOSHI_HTTP_PORT`（默认 `18080`）和 `MOSHI_CONTEXT_PATH`（默认 `ROOT`）；上下文路径只允许字母、数字、下划线和连字符。Compose 默认导入本机演示账号，仅适合课程演示；公网部署应移除 `data-demo-mysql.sql` 挂载并使用强密码。

部署后执行健康验证，脚本只有在 HTTP 200 且响应中 `status=UP`、`db=UP` 时才返回成功：

```bash
bash javaee/deploy/verify.sh http://127.0.0.1:18080/
# Windows PowerShell
powershell -File javaee/deploy/verify.ps1 -BaseUrl http://127.0.0.1:18080/
```

当前本机未安装 Docker，以上命令和镜像文件属于已准备的交付材料，尚未取得 Compose 实跑证据。

## 7. 验证清单

- `mvnw -f javaee/pom.xml test` 全部通过。
- 首页显示数据库连接正常。
- `/hello` 返回 UTF-8 问候与服务器时间；`/health` 返回 200 和 `status=UP`、`db=UP`。
- 注册、登录、退出可用；登录后 Session ID 更新。
- 无 CSRF Token 的 POST 返回 403。
- 答题答案只在服务端判分，超时和越界选项被拒绝或记为未作答。
- 同一任务重复领取只产生一条奖励记录。
- 伪造扩展名的非图片上传被拒绝。
- 禁用账号或角色变更后，原 Session 不能继续访问受保护页面。
- 错误页不显示 SQL、路径、堆栈或异常明细。
