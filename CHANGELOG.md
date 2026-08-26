# 更新日志

## [1.3] - 2026-08-25

### ✨ 处理引擎（参照 nyabox）
- 新增「规则 / 混合 / AI」三种处理引擎
  - 规则：离线确定性改写（替换规则 + 断句追加 + 颜文字）
  - 混合：规则先行 → AI 润色 → 失败自动降级规则
  - AI：大模型直接改写（OpenAI 兼容接口，失败回退原文）
- 新增「设置 → AI 设置」：配置 Base URL / API Key / 模型 / Temperature（默认 DeepSeek）
- AI / 混合在无障碍服务中异步处理，不阻塞输入；「测试当前配置」对三种引擎均生效

### ✨ 原生背景模糊
- 弹窗 / 底部弹窗打开时，用 Android 12 原生 `RenderEffect` 模糊宿主内容（参照 LibChecker 的 `WindowBlurCompatController`），关闭后恢复

### ✨ 其他
- 应用名改为「QQ喵喵助手X」

---

## [1.2] - 2026-08-25

### 🐛 修复
- 修复「测试当前配置」无法弹出（移除弹窗窗口级模糊 `setBackgroundBlurRadius`，改为直接读取界面当前配置）
- 开关（处理模式 / 断句追加 / 句末颜文字）与追加内容即时生效，无需再点保存；「保存设置」仅负责替换规则与自定义颜文字

### ✨ 界面
- 背景模糊改用 Android 12 原生 `RenderEffect` 模糊（参照 LibChecker），作用于服务状态卡片
- 服务状态「已开启」跟随主题色（不再写死绿色）
- 主题色与背景中性色统一：4 种主题各自独立的浅色 / 深色表面色板

### 🎨 主题
- 「昔涟粉」主色改为 `#ffd7e4`；切换为昔涟粉时 Toast「汝将收梢于花开时 一如终结诞下起始」
- 删除「白厄黄」

### ✨ 关于界面
- DEVS / CONTRIBS 改为可点击按钮，点击弹出底部弹窗（圆形头像 + 姓名）
- 点击作者跳转 GitHub（QiCaiJie114514 / Chinesebg）；「原作者请尽快联系」使用占位头像框
- 新增贡献者「己所不欲 勿施于人」，点击弹窗展示 src1.png

---

## [1.1] - 2026-08-25

### 🎨 主题系统
- **主题色统一并可切换**：内置 5 种统一配色 —— 新年红 / 鲸鱼蓝 / 原野绿 / 昔涟粉 / 白厄黄，设置页单选即时切换
- **深浅色模式**：参照 LibChecker，支持「跟随系统 / 浅色 / 深色」三种模式（基于 `AppCompatDelegate.setDefaultNightMode`）
- 每种主题色均提供浅色 / 深色两套 Material3 色板（`values` / `values-night` 资源限定自动切换）

### ✨ 界面细节
- 开关按钮升级为 Material3 `MaterialSwitch`（真 · Material3 样式，替换原 `SwitchMaterial`）
- 底部导航栏**沉浸式**：边到边（edge-to-edge），状态栏与底部手势条透明，内容自适应系统栏 inset

### ✨ 功能增强
- 文本替换规则新增「一键填入预设（我=本喵 / 你=主人）」，已存在则提示
- Android 12+ 弹窗 / 底部弹窗背景模糊（`Window#setBackgroundBlurRadius`，低版本自动跳过）

### 🔧 其他
- 新增 `ThemeManager`（主题色 + 深浅色模式持久化）与 `App`（Application 层应用夜间模式）
- 核心逻辑（`CatConfig` / `TextProcessor` / `QQAccessibilityService`）保持不变

---

## [1.0] - 2026-08-25

### 🎨 界面：Material3 化
- 参照 LibChecker 全面升级为 **Material3 风格**，引入 AndroidX AppCompat + Google Material Components（不再是无依赖工程）
- 新增底部「主页 / 设置」双 Tab
- **主页**：原控制面板功能全部保留，改用 Material3 组件（`MaterialButton` / `SwitchMaterial` / `TextInputLayout` / `MaterialCardView`），暖橙配色主题
- **设置**：新增「关于」「获取更新」「多语言」三个条目
  - 关于：二级页展示 App 图标、版本号、DEVS（QiCaiJie114514、占位符「原作者请尽快联系」）、CONTRIBS（Chinesebg）
  - 获取更新：底部弹窗（GitHub 图标 + 标签），点击跳转 https://github.com/Chinesebg/QQMeowAssistantX
  - 多语言：占位实现，点击提示 Toast「正在制作中qwq」

### 🔧 其他
- 应用主题由系统自带 `Theme.Material.Light` 改为自定义 Material3 主题 `Theme.QQMeow`
- 核心逻辑（`CatConfig` / `TextProcessor` / `QQAccessibilityService`）保持不变

---

## 逆向重建版（早期）
- 原版写死的替换规则（我→本喵、你→主人、断句加喵、随机颜文字）全部改为界面可自定义
- 修复反编译产生的编译问题（lambda 外层引用、未初始化变量/死循环等），使源码可正常编译运行
