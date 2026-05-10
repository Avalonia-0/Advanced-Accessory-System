## 0. 未来开发规范（长期维护约定）

为避免 AAS 再次退回“功能堆叠型项目”，后续开发默认遵循以下规范。

### 1. 功能域优先，技术分层次之
- 新功能应先确定所属的 `feature.xxx`，再在该功能域内细分为 `logic / state / sync / network / client / config / rule` 等子层。
- 不再将业务类直接挂在主包 `com.alonie.advancedaccessorysystem` 下。
- 主包仅保留入口职责，例如 `AdvancedAccessorySystemMod`。
- `bootstrap` 仅负责模块注册与初始化编排，不承载长期业务逻辑。

### 2. 状态必须先分类，再落代码
- 新增状态前，必须先判断其所属类别：
  - `global config`
  - `runtime session`
  - `client cache`
  - `derived state`
  - `per-player persistent state`
- 禁止将新的运行时状态直接塞入业务类或 sync manager 的 `static Map/Set` 中。
- sync manager 应尽量只负责“同步流程”，真正的数据容器应放在 `feature.xxx.state` 中。
- 状态命名必须准确表达作用域，不允许再出现“看起来像 per-player，实际是 global”的命名。

### 3. 网络按语义命名，不按临时习惯命名
- C2S 请求统一放在 `network.c2s.request`。
- S2C 状态同步统一放在 `network.s2c.sync`。
- 若未来出现广播型或提示型消息，统一放在 `network.s2c.event`。
- payload 命名必须体现语义，例如 `Request`、`Sync`、`Event`，不得继续使用双向含糊命名。
- 协议注册统一收口到 `network.registry`，不要将注册细节散落回 bootstrap 或业务类。

### 4. Collective 基础设施统一经由内部 bridge 调用
- AAS 内部凡是需要使用 Collective 的网络、任务调度或生命周期回调时，默认只通过 `com.alonie.advancedaccessorysystem.collective` 包下的 bridge 访问。
- 优先复用现有桥接入口，例如 `CollectiveNetworkingBridge`、`CollectiveTaskBridge`、`CollectiveLifecycleBridge`；不要在 feature 层直接散落 `Dispatcher`、`Network`、`TaskFunctions` 或 Collective callbacks 调用。
- 业务层只关心“发送什么消息、处理什么状态、何时清理 session”，不直接承载 Collective API 细节。
- 仅当 Collective 没有等价入口，或其语义明显不如 Fabric API 清晰时，才允许继续保留 Fabric 事件。当前明确保留的包括：
  - `UseEntityCallback`
  - `ServerTickEvents.END_SERVER_TICK`
  - `ClientTickEvents.END_CLIENT_TICK`
  - `ServerLifecycleEvents.SERVER_STARTING`
- 若未来需要新增基于 Collective 的通用能力，应优先先扩展 bridge，再让 feature 使用；不要反过来让多个 feature 各自直接接第三方 API。
- 涉及 Collective 的实现或重构前，默认先参考项目内文档 `Collective.docx`，并将其视为本项目当前采用的 Collective 参考资料。

### 5. Compat 仅暴露内部抽象，不向业务层泄漏第三方细节
- 业务层只能依赖 `compat.api` 或 `CompatServices`。
- 第三方实现统一放在 `compat.impl.xxx`。
- 禁止在业务逻辑中直接调用第三方 mod 的反射细节、slot 细节或渲染桥细节。
- 若第三方 API 发生变化，应优先修改 compat 实现层，不要将变更扩散到 feature 层。

### 6. Mixin 必须有明确归属和存在理由
- 新增 mixin 必须放入对应功能域包中，例如 `mixin.ride`、`mixin.headslot`、`mixin.client.armorvisibility`。
- 每个关键 mixin 至少需要说明三件事：
  - 注入目标是什么
  - 为什么事件 API 不足以完成需求
  - 若该 mixin 失效，会影响哪个功能
- 优先选择稳定的 Yarn 命名方法，避免继续依赖脆弱的旧 intermediary 方法名。

### 7. Accessory 判断优先走 capability
- 新玩法不得继续扩散 `isXxxAccessory()` 风格的直接判断。
- 应优先通过 `feature.accessory.capability` 判断“该头部物品支持哪些行为”。
- 物品识别规则可以继续落在状态层或配置层，但业务系统只应关心 capability，而不应关心底层识别细节。

### 8. 客户端代码必须拆分职责
- `client` 根层仅保留客户端入口。
- 输入、渲染、缓存、界面、客户端同步应分别归入：
  - `client.input`
  - `client.render`
  - `client.state`
  - `client.screen`
  - `client.sync`
- 客户端本地状态残留属于高风险点，断线或切世界时必须考虑清理策略。

### 9. 变更优先级：先整理结构，再处理语义
- 默认顺序为：
  1. 目录整理
  2. 命名澄清
  3. 状态抽离
  4. 注册分层
  5. 最后才处理玩法逻辑调整
- 不要将“改目录 + 改语义 + 改逻辑”混在同一次整理中完成。
- 高风险区域，如 `ride` 核心同步、`compat` 反射桥、容器共享会话，应单独开一轮处理并重点验证。

### 10. 每次开发的最低验收标准
- 修改后至少执行一次 `./gradlew.bat build`。
- 开发过程中的构建、重构、迁移与验证记录统一写入项目根目录 `devbuild.log`，不要再将同类开发日志分散到其他临时文件中。
- 若发生新的架构整理或模块迁移，必须在本 README 的变更记录中追加说明。
- 若本 README 中的流程规范本身发生变更，也必须同步在变更记录中留痕，不能只改规范正文而不记录。
- 若改动触及以下区域，默认需要额外自查：
  - `feature.ride`
  - `feature.headshulker`
  - `compat.impl`
  - `mixin.*`

### 11. 命名约定
- `*Manager`：流程协调器，不应成为状态仓库。
- `*State`：明确持有状态数据。
- `*Cache`：客户端缓存或派生缓存。
- `*Bootstrap`：注册入口。
- `*Helper`：仅用于轻量辅助；一旦开始持有状态或承担流程，应升级为更明确的类型。

## 2026-04-24 流程规范补充
- 约定开发日志统一写入项目根目录 `devbuild.log`。
- 约定流程规范变更必须同时在本 README 的变更记录中留痕。
- 约定 Collective 相关实现默认参考项目内文档 `Collective.docx`。
