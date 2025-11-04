# Fanuc Comment Agent

## Connection 模块
- 读取长文本: `http://192.168.0.1/karel/ComGet?sFc=33`
- 写入长文本: `http://192.168.0.1/karel/ComSet?sComment=aaa&sIndx=123&sFc=33`

## 自动化发行构建
当向仓库推送 `v` 前缀的标签（例如 `v1.0.0`）时，GitHub Actions 会自动：
1. 使用 Maven 构建 Spring Boot 可执行 JAR。
2. 基于 Temurin JDK 21 运行 `jlink` 打包完整的 Java 运行时。
3. 运行 `jpackage` 为 macOS 与 Windows 生成免安装的 `app-image` 绿色应用包。
4. 将每个平台的压缩包上传到对应的 GitHub Release，供直接下载。

生成的文件：
- `FanucComment-macos-<版本>.zip`
- `FanucComment-windows-<版本>.zip`

> 提示：请确保标签版本号与 README 中的说明一致，例如 `v1.0.0`，以保证 `jpackage` 的 `--app-version` 参数合法。
