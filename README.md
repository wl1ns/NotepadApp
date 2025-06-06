# Android 记事本应用

这是一个基于 Android 的简洁美观的记事本应用，支持日/夜模式切换、分类管理、搜索历史、回收站等功能。

## 功能特色

- 🌞 日/夜模式自动切换，界面风格随系统变化
- 📝 支持笔记的新增、编辑、删除与恢复
- 🔍 实时搜索与搜索历史记录
- 🗂️ 分类筛选（全部、未分类、工作、生活、学习）
- 🗑️ 回收站功能，误删可恢复
- 💡 界面美观，支持渐变背景和圆角卡片

## 项目结构
app/ ├── src/ │ └── main/ │ ├── java/ │ │ └── com/ │ │ └── example/ │ │ └── lastapp/ # 主要 Java 代码（Activity、Adapter、数据库等） │ ├── res/ │ │ ├── layout/ # 布局文件（XML） │ │ ├── drawable/ # 图片、shape、selector 等资源 │ │ ├── values/ # 颜色、字符串、样式等资源 │ │ ├── values-night/ # 夜间模式下的资源（如颜色） │  │ └── AndroidManifest.xml # 应用清单文件 └── ...


- **java/com/example/lastapp/**：存放所有 Java 源代码，包括主界面、适配器、数据库操作等。
- **res/layout/**：存放所有界面布局 XML 文件。
- **res/drawable/**：存放图片、shape、selector 等可绘制资源。
- **res/values/**：存放颜色、字符串、样式等资源文件。
- **res/values-night/**：存放夜间模式下的资源（如深色主题的 colors.xml）。
- **AndroidManifest.xml**：应用的配置和组件声明文件。

