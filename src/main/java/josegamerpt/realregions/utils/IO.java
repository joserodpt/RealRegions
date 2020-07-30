package josegamerpt.realregions.utils;

import java.io.File;

public class IO {

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static long toMB(long l)
    {
        return l / (1024 * 1024);
    }
}
