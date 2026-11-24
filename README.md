# 魔识 · JavaEE 8 互动答题与素材管理

Java EE 期末大作业课程主工程位于 [`javaee/`](javaee/)。技术栈为 **JDK 11 + JavaEE 8（`javax.servlet.*`）+ Tomcat 9 + JSP/JSTL + Service + 原生 JDBC + MySQL 8+**，构建产物是 `javaee/target/moshi.war`。

根目录 Spring Boot 3 工程保留为扩展参考，**不属于课程主交付**。课程评分、部署截图和实验报告均以 `javaee/` 为准。

## 功能

- 五分区题库、三档难度、服务端判分、单题限时与速度奖励
- Session 登录、BCrypt 密码摘要、登录后 Session 更新、账号状态实时校验
- 认证/授权/UTF-8/CSRF/安全响应头过滤器
- 成绩入库、总榜与分区榜、个人中心
- 任务进度实时统计、事务与唯一键保证奖励只领取一次
- 图片内容校验、UUID 落盘、上传者记录、数据库登记后下载
- Mock / OpenAI 兼容真实大模型双通道
- JSP + JSTL + EL，无脚本代码；Servlet → Service → DAO 分层
- `/hello` 课程基础 Servlet 示例与 `/health` 应用/数据库健康检查

## 快速开始

完整步骤见 [`javaee/DEPLOY.md`](javaee/DEPLOY.md)。

```powershell
# 1. 使用 JDK 11，并确认构建与运行环境一致
java -version  # 应为 11.x

# 2. 初始化数据库（脚本不会清空已有表）
mysql --default-character-set=utf8mb4 -u root -p < javaee/src/main/resources/db/schema-mysql.sql

# 2. 配置数据库密码（不要写进代码）
$env:MOSHI_DB_URL = "jdbc:mysql://localhost:3306/moshi?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
$env:MOSHI_DB_USER = "moshi_app"
$env:MOSHI_DB_PASSWORD = "你的数据库密码"

# 3. 测试并构建 WAR
.\mvnw.cmd -f javaee/pom.xml test package
```

将 `javaee/target/moshi.war` 部署到 Tomcat 9 的 `webapps`，访问 `http://localhost:8080/moshi/home`。自动化测试使用 H2，不需要额外启动 MySQL。`maven.compiler.release=11`，请同时把 Maven 和 Tomcat 的 `JAVA_HOME` 固定为 JDK 11。

## 目录

```text
javaee/
├── pom.xml                         # WAR、Java 11、JUnit 5、H2 测试依赖
├── DEPLOY.md                       # MySQL/Tomcat/Docker 部署说明
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── deploy/                         # 容器上下文路径、部署后健康验证脚本
├── src/main/java/com/example/moshi/
│   ├── bootstrap/DatabaseSeeder.java
│   ├── dao/                        # PreparedStatement + try-with-resources
│   ├── entity/ / model/
│   ├── service/                    # 业务服务层
│   ├── util/
│   └── web/
│       ├── filter/                 # 编码、会话、CSRF、认证、授权、安全头
│       ├── servlet/
│       └── support/
├── src/main/resources/db/          # MySQL 脚本、演示账号、题库与任务种子
├── src/main/webapp/WEB-INF/        # web.xml、JSP/JSTL 视图
└── src/test/                       # H2 集成与单元测试
reports/                            # 八份实验报告，随代码一起上传
docs/screenshots/                   # 可复现的真实运行截图
```

## 配置

| 环境变量 | 说明 |
| --- | --- |
| `MOSHI_DB_URL` | JDBC URL，默认数据库名为 `moshi` |
| `MOSHI_DB_USER` | 数据库账号，无默认值 |
| `MOSHI_DB_PASSWORD` | 数据库密码，无默认值，禁止硬编码 |
| `MOSHI_HTTP_PORT` | Compose 宿主机 HTTP 端口，默认 `18080` |
| `MOSHI_CONTEXT_PATH` | Compose 上下文路径，默认 `ROOT`；只允许字母、数字、`_`、`-` |
| `MOSHI_DB_SEED` | 是否在空表时导入题目和任务，默认 `true` |
| `MOSHI_LLM_MODE` | `mock` 或 `real`，默认 `mock` |
| `MOSHI_LLM_BASE_URL` | OpenAI 兼容接口根地址 |
| `MOSHI_LLM_API_KEY` | 仅在运行环境设置 |
| `MOSHI_LLM_MODEL` | 模型名称 |
| `MOSHI_LLM_TIMEOUT_SECONDS` | 请求超时，默认 30 秒 |

`data-demo-mysql.sql` 含固定演示密码，只允许在本机课程演示使用。正式部署不要导入该文件，也不要把数据库密码、API Key 或 `.env` 提交到 GitHub。

## 验证

```bash
./mvnw -f javaee/pom.xml test
./mvnw -f javaee/pom.xml package
# 部署后验证：HTTP 200 且 status/db 均为 UP
bash javaee/deploy/verify.sh http://127.0.0.1:18080/moshi
```

测试覆盖认证、重定向防护、答题限时与选项校验、任务幂等领取、Mock 大模型数据结构。Tomcat/MySQL 实机截图与报告见 `reports/` 和 `docs/screenshots/`；需要另一台电脑、4G 外网或结对演示的证据必须由实际操作者补充，不能用生成图代替。

## 本机最终验证

2026-11-24 已使用 Temurin JDK 11.0.33 完成 Maven 测试与 WAR 构建，8 项测试全部通过；独立 Tomcat 9.0.121 + MySQL 启动后，`/moshi/home`、`/moshi/hello`、`/moshi/health` 均返回 HTTP 200，健康检查返回 `status=UP`、`db=UP`。构建输出与运行证据保存在 `docs/runtime-jdk11-build.txt` 和 `docs/screenshots/jdk11-*`。Docker/公网部署尚未实跑，部署说明中已明确标注。

## VS Code 上传

在仓库根目录打开终端，先确认分支和待提交文件，再分阶段提交并推送：

```bash
git status --short
git diff --check
git add README.md .gitignore javaee/ reports/ docs/
git commit -m "feat(javaee): 完成课程合规整改与实验报告"
git push origin master
git rev-parse HEAD
```

推送后重新打开 GitHub 仓库，确认 `javaee/`、`reports/` 中八份 DOCX、`docs/screenshots/` 和部署说明均已出现。把上一步得到的课程代码提交哈希填入第 8 份报告的“最终提交哈希”，保存后再单独提交并推送报告更新。不要提交 `.env`、`javaee/target/`、`.tools/` 或真实 API Key；`data-demo-mysql.sql` 的固定演示账号只允许用于本机教学演示。
