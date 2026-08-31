// MineSweeper 单文件自解压启动器（由 Windows 自带 csc.exe 编译，无需任何第三方工具）
// 用法：将游戏目录 dist\MineSweeper 打包为 zip 并作为嵌入资源 minesweeper.app.zip
// 安全策略：用“内嵌 zip 长度”作为版本标记写入临时目录；标记不一致时强制重新解压，
//           避免运行到旧版本的缓存内容，同时保留“内容没变就秒开”的体验。
using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;

class Program
{
    [STAThread]
    static void Main()
    {
        try
        {
            string baseDir = Path.Combine(Path.GetTempPath(), "MineSweeperGame");
            string exePath = Path.Combine(baseDir, "MineSweeper", "MineSweeper.exe");
            string markerPath = Path.Combine(baseDir, "app.zip.len");

            long zipLen = GetResourceLength("minesweeper.app.zip");
            bool needExtract = !File.Exists(exePath)
                    || !File.Exists(markerPath)
                    || File.ReadAllText(markerPath).Trim() != zipLen.ToString();

            if (needExtract)
            {
                if (Directory.Exists(baseDir)) Directory.Delete(baseDir, true);
                Directory.CreateDirectory(baseDir);

                string zipPath = Path.Combine(baseDir, "app.zip");
                using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream("minesweeper.app.zip"))
                {
                    if (s == null) throw new Exception("Embedded resource not found.");
                    using (FileStream fs = new FileStream(zipPath, FileMode.Create, FileAccess.Write))
                    {
                        s.CopyTo(fs);
                    }
                }
                System.IO.Compression.ZipFile.ExtractToDirectory(zipPath, baseDir);
                File.Delete(zipPath);
                File.WriteAllText(markerPath, zipLen.ToString());
            }

            ProcessStartInfo psi = new ProcessStartInfo(exePath);
            psi.WorkingDirectory = Path.GetDirectoryName(exePath);
            Process p = Process.Start(psi);
            if (p != null) p.WaitForExit();
        }
        catch (Exception ex)
        {
            System.Windows.Forms.MessageBox.Show(
                "Failed to start: " + ex.Message,
                "MineSweeper",
                System.Windows.Forms.MessageBoxButtons.OK,
                System.Windows.Forms.MessageBoxIcon.Error);
        }
    }

    /// <summary>内嵌 zip 资源的字节长度（作为版本标记）</summary>
    private static long GetResourceLength(string name)
    {
        using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream(name))
        {
            return s == null ? -1 : s.Length;
        }
    }
}
