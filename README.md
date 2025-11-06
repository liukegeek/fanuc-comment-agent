# Fanuc Comment Agent

一个用于集中管理 Fanuc 机器人IO注释的本地 Web 控制台。后端基于 Spring Boot 提供 REST 接口，前端使用 Vue 3 实现单页应用，便于实现批量查询、修改以及备份机器人控制柜中的注释信息。

## ✨ 功能概览

- **实时查询**：按照编号、编号区间或关键字从机器人中查询注释内容。
- **批量上传**：对本地修改过的注释进行批量上传，自动处理重复尝试与异常提示。
- **本地备份与恢复**：将选中的注释保存为 JSON 文件，或从已有的 JSON 文件中加载注释内容。
- **日志追踪与故障排查**：内置 Log4j2 日志体系，所有关键操作与异常信息都会写入本地日志文件，便于排障。

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- 操作系统需支持写入用户主目录（用于存放日志）

### 本地运行

```bash
mvn spring-boot:run
```

应用默认监听 `http://localhost:8910`，浏览器访问即可使用前端界面。

### 构建可执行 JAR

```bash
mvn clean package
java -jar target/fanuc-comment-agent-*.jar
```

### 直接下载绿色软件包使用
在 [Release 页面](https://github.com/liuke1995/FanucHelper/releases) 下载最新版本的绿色软件包（`.zip`）。

> 首次启动会在用户主目录下创建 `~/Desktop/.fanuc-comment-agent/logs` 文件夹，并将运行日志写入其中。

## 🔌 运行时设置要连接目标机器人的 IP

1. 启动应用后，点击界面右上角标题旁的插头图标。
2. 在弹出的对话框中输入目标机器人 IP 地址（示例：`192.168.0.1` 或 `127.0.0.1`）。
3. 保存成功后，后端会立即更新 KAREL 连接的基础地址，并清空旧缓存，随后的查询和写入都会使用新的目标。

当前后端会校验 IPv4 地址格式，输入非法地址会提示错误；若连接失败，可在日志文件中查看详细原因。

## 🧾 日志与异常处理

- 日志文件存放在：`~/Desktop/.fanuc-comment-agent/logs/app.log`
- 控制台与文件日志均使用 Log4j2 输出，涵盖：
  - 连接测试、读写请求及其返回状态
  - 本地文件读写
  - 批量上传的重试过程
  - 所有 REST 异常（统一由 `RestExceptionHandler` 记录）
- 如果需要调整日志目录，可在启动命令中覆盖系统属性，例如：

  ```bash
  java -Dlog.dir=/path/to/logs -jar target/fanuc-comment-agent-*.jar
  ```

## 📡 接口与前后端数据格式

- 所有 API 都位于 `/api` 前缀下，核心接口包括：
  - `GET /api/comments/queryById`
  - `GET /api/comments/queryByKeyWord`
  - `GET /api/comments/queryByIdRange`
  - `GET /api/comments/queryAll`
  - `POST /api/comments/batchUpdate`
  - `POST /api/comments/local/save`
  - `POST /api/comments/local/load`
  - `GET/POST /api/settings/connection`（新增，用于读取或修改机器人 IP）
- 前后端统一约定注释载荷格式：

  ```json
  {
    "id": "123",
    "content": "示例注释",
    "type": "R_COMMENT"
  }
  ```

  前端在上传或保存前会校验编号是否缺失，后端也会在收到无效编号时返回 400 错误，避免出现格式不匹配的问题。

## 🛠 自动化发行构建

当向仓库推送 `v` 前缀的标签（例如 `v1.0.0`）时，GitHub Actions 会自动：
1. 使用 Maven 构建 Spring Boot 可执行 JAR。
2. 基于 Temurin JDK 21 运行 `jlink` 打包完整的 Java 运行时。
3. 运行 `jpackage` 为 macOS 与 Windows 生成免安装的 `app-image` 绿色应用包。
4. 将每个平台的压缩包上传到对应的 GitHub Release，供直接下载。

生成的文件：
- `FanucCommentTool-macos-<版本>.zip`
- `FanucCommentTool-windows-<版本>.zip`

> 提示：请确保标签版本号与 README 中的说明一致，例如 `v1.0.0`，以保证 `jpackage` 的 `--app-version` 参数合法。
