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

    private static long GetResourceLength(string name)
    {
        using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream(name))
        {
            return s == null ? -1 : s.Length;
        }
    }
}
