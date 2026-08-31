# 扫雷游戏（三关扫雷）

Java Swing 实现的扫雷游戏课程设计，包含三项扩展功能。

## 运行方法

方式一（命令行）：
```bash
cd CurriculumDesign
javac -encoding UTF-8 -d out src/minesweeper/*.java
java -cp out minesweeper.MineSweeper
```

方式二（IntelliJ IDEA）：直接用 IDEA 打开 `CurriculumDesign` 目录，
运行 `src/minesweeper/MineSweeper.java` 中的 `main` 方法。

方式三（打包好的 exe，无需安装 JDK）：
- **单文件版**：双击 `dist\MineSweeper_Setup.exe`（23.6 MB）即可运行。
  首次运行会解压到临时目录（约 2~3 秒），之后直接秒开；关闭游戏后自动退出。
- 免安装目录版：双击 `dist\MineSweeper\MineSweeper.exe`，整个自包含程序在
  `dist\MineSweeper\` 文件夹（70.8 MB，含精简 JRE），可整体拷贝到其他电脑使用。
- 分发用压缩包：`dist\MineSweeper.zip`（23.6 MB）。
- 软件图标：`dist\icon.ico`（多尺寸 16~256，由 `MinesweeperIcon.java` 绘制、
  `packaging\MakeIcon.java` 生成），已嵌入 exe 文件图标；窗口/任务栏图标由
  `AppWindow` 运行时绘制同一图形。
- 体积优化：jpackage 只打包游戏需要的模块（`--add-modules java.base,java.desktop,
  java.logging,jdk.unsupported`），运行时从默认约 148 MB 降到约 70 MB。
- 内存优化：通过 `--java-options "-Xms32m -Xmx256m"` 限制 JVM 堆（小型游戏占用很低，默认堆太大）。
- 重新打包命令：
  ```bash
  # 递归编译所有子包
  javac -encoding UTF-8 -d out $(Get-ChildItem src -Recurse -Filter *.java | % FullName)
  jar --create --file dist-jar/minesweeper.jar --main-class minesweeper.ui.MineSweeper -C out .
  jpackage --type app-image --icon dist/icon.ico --input dist-jar --name MineSweeper --main-jar minesweeper.jar --main-class minesweeper.ui.MineSweeper --dest dist --add-modules java.base,java.desktop,java.logging,jdk.unsupported --java-options "-Xms32m -Xmx256m"
  # 单文件版：把 dist\MineSweeper 压成 zip 后，用 packaging\SfxProgram.cs 重新编译
  Compress-Archive -Path dist\MineSweeper -DestinationPath dist\MineSweeper.zip
  C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe /nologo /target:winexe /out:dist\MineSweeper_Setup.exe /win32icon:dist\icon.ico /r:System.Windows.Forms.dll /r:System.IO.Compression.FileSystem.dll /resource:dist\MineSweeper.zip,minesweeper.app.zip /codepage:65001 packaging\SfxProgram.cs
  ```

## 功能说明

### 1. 自定义级别菜单项
- 菜单栏：`游戏 → 难度 → 自定义级别...`
- 弹出**模态对话框**，可输入：
  - 行数（5~30）
  - 列数（5~50）
  - 雷数（自动限制为小于 行数×列数）
  - 级别名称（最多 12 个字符）
- 点击“确定”后以自定义配置开始新游戏，状态栏显示自定义级别名。

### 2. 游戏音效（无外部音频文件，运行时实时合成）
- 左键单击的方块**不是雷**：播放简短的愉悦音乐（C5-E5-G5 琶音）；
- 右键在方块上**标记地雷**：播放提示音乐（清脆双音）；
- 右键取消标记、过关、踩雷也各有提示音；
- 菜单 `游戏 → 音效` 可开关声音。

### 3. 过关游戏（三关扫雷）
- 菜单栏：`游戏 → 关卡（三关扫雷）`，共三关：
  - 第一关·初级（8×8，10 雷）
  - 第二关·中级（12×12，20 雷）
  - 第三关·高级（16×16，40 雷）
- 通过一关后弹出询问对话框：“是否继续下一关？”——选择“是”自动进入下一关，
  选择“否”留在本关；通过第三关后提示“通关成功”，可选择重新挑战。

## 操作说明
- 棋盘使用**固定正方形格子**：未翻开的格子为灰色立体凸起，已翻开的格子为浅色平面并带描边，两种状态颜色区分明显（整板自绘，不依赖系统按钮外观）。
- 左键：翻开方块（点到安全格播放愉悦音乐）
- 右键：标记 / 取消标记地雷（标记时播放提示音乐）
- 数字格上左键：若周围标记的雷数等于该数字，则快速翻开其余周边方块
- 第一次点击后才布雷，开局点击的格子一定安全
- 状态栏显示：当前级别、剩余雷数、用时

## 源码结构（按职责分层分包）
| 包 | 文件 | 说明 |
| --- | --- | --- |
| `minesweeper.core` | `MineField.java` | **核心逻辑模型**：布雷、翻开、标记、胜负判定（与界面无关，可独立测试） |
| | `GameConfig.java` | 游戏配置 record（行数/列数/雷数/名称） |
| | `LevelManager.java` | 三关与难度配置管理 |
| `minesweeper.ui` | `MineSweeper.java` | 程序入口（主类） |
| | `AppWindow.java` | 主窗口：菜单栏、状态栏、关卡流程 |
| | `MineFieldPanel.java` | **棋盘视图**：读取模型状态整板自绘、处理鼠标与音效 |
| | `CustomLevelDialog.java` | 自定义级别模态对话框 |
| `minesweeper.audio` | `SoundPlayer.java` | 音效合成播放器（`Note` record 描述音符） |
| `minesweeper.gfx` | `MinesweeperIcon.java` | 软件图标绘制（窗口图标 + icon.ico） |
| | `GameIcons.java` | 界面小图标（地雷/红旗/时钟/对勾） |

分层原则：`core`（纯逻辑）→ `ui`（界面）→ `audio`/`gfx`（辅助能力），`ui` 依赖 `core`，方向单向。
