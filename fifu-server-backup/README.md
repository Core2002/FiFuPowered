# FiFuServerBackup

一个功能强大的 Minecraft 服务器备份插件，支持自动备份、加密传输、远程同步等功能。

## 功能特性

- ✅ **自动备份**: 可配置的定时备份系统
- 🔐 **加密传输**: AES-CBC 加密确保数据安全
- 🌐 **远程同步**: 支持将备份文件传输到远程服务器
- 📊 **TOTP 认证**: 基于时间的一次性密码保护
- 📝 **详细日志**: 完整的操作日志记录
- 🛡️ **安全验证**: 文件完整性校验和访问控制
- 📋 **命令系统**: 完整的用户交互界面

## 安装说明

1. 下载最新版本的插件 JAR 文件
2. 将 JAR 文件放入服务器的 `plugins` 目录
3. 重启服务器或使用 `/reload` 命令
4. 根据需要配置 `plugins/FiFuServerBackup/ServerBackupConfig.json`

## 配置说明

### 基础配置

```json
{
  "enableBackup": true,
  "backupServerDirPath": "./backups",
  "backupKeepDay": 7,
  "sendToRemoteServer": false,
  "sendRemoteServerUrl": "",
  "sendRemoteServerSecret": "",
  "maxUploadSizeMB": 1024,
  "backupCheckIntervalHours": 1
}
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enableBackup` | 是否启用备份功能 | `false` |
| `backupServerDirPath` | 备份文件存储目录 | `"./backups"` |
| `backupKeepDay` | 备份文件保留天数 | `7` |
| `sendToRemoteServer` | 是否启用远程传输 | `false` |
| `sendRemoteServerUrl` | 远程服务器 URL | `""` |
| `sendRemoteServerSecret` | TOTP 密钥（自动生成） | `""` |
| `maxUploadSizeMB` | 最大上传文件大小（MB） | `1024` |
| `backupCheckIntervalHours` | 备份检查间隔（小时） | `1` |

## 命令使用

### 备份命令 (`/backup`)

- `/backup start` - 立即开始备份
- `/backup status` - 查看备份状态
- `/backup config` - 查看备份配置
- `/backup reload` - 重新加载配置

### 插件命令 (`/fifu`)

- `/fifu version` - 查看版本信息
- `/fifu info` - 查看插件信息

## 权限系统

| 权限节点 | 说明 | 默认值 |
|----------|------|--------|
| `fifu.backup.use` | 使用备份命令的权限 | `op` |
| `fifu.admin` | 管理员权限 | `op` |

## 远程服务器设置

### 启动远程服务器

```bash
java -jar FiFuServerBackup.jar -s
```

### 配置远程传输

1. 在插件配置中设置 `sendToRemoteServer` 为 `true`
2. 配置 `sendRemoteServerUrl` 为远程服务器地址
3. 首次运行时会自动生成 TOTP 密钥
4. 在远程服务器上同步 TOTP 密钥

## 安全特性

- **AES-CBC 加密**: 使用 256 位密钥和 128 位 IV
- **TOTP 认证**: 基于 SHA-512 的时间一次性密码
- **文件完整性**: SHA-512 哈希校验
- **访问控制**: IP 地址记录和权限验证
- **大小限制**: 防止大文件攻击
- **路径验证**: 防止目录遍历攻击

## 故障排除

### 常见问题

1. **备份失败**
   - 检查备份目录权限
   - 确认磁盘空间充足
   - 查看服务器日志

2. **远程传输失败**
   - 验证网络连接
   - 检查 TOTP 密钥同步
   - 确认远程服务器状态

3. **权限错误**
   - 确认用户具有相应权限
   - 检查 `plugins/FiFuServerBackup` 目录权限

### 日志位置

- 插件日志：服务器控制台和 `logs/latest.log`
- 配置文件：`plugins/FiFuServerBackup/`
- 备份文件：配置中指定的备份目录

## 开发信息

- **作者**: NekokeCore
- **版本**: 1.0.0
- **许可**: Mulan PSL v2
- **兼容性**: Minecraft 1.19+

## 构建说明

```bash
# 克隆项目
git clone https://github.com/NekokeCore/FiFuPowered.git
cd FiFuPowered/fifu-server-backup

# 构建项目
./gradlew build

# 运行测试
./gradlew test
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 发起 Pull Request

## 许可证

本项目采用 [Mulan PSL v2](http://license.coscl.org.cn/MulanPSL2) 许可证。

## 更新日志

### v1.0.0
- 初始版本发布
- 基础备份功能
- 加密传输支持
- 远程同步功能
- 完整的命令系统
- 安全特性实现