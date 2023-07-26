package josegamerpt.realregions.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class IO {

    public static long folderSize(File directory) {
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .mapToLong(file -> file.isFile() ? file.length() : folderSize(file))
                .sum();
    }

    public static long toMB(long l)
    {
        return l / (1024 * 1024);
    }
}
