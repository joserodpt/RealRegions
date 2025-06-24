package joserodpt.realregions.api.utils;

/*
 *  ______           _______           
 *  | ___ \         | | ___ \         (_)
 *  | |_/ /___  __ _| | |_/ /___  __ _ _  ___  _ __  ___
 *  |    // _ \/ _` | |    // _ \/ _` | |/ _ \| '_ \/ __|
 *  | |\ \  __/ (_| | | |\ \  __/ (_| | | (_) | | | \__ \
 *  \_| \_\___|\__,_|_\_| \_\___|\__, |_|\___/|_| |_|___/
 *                                __/ |
 *                               |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class IO {

    public static long folderSize(File directory) {
        if (!directory.isDirectory()) return -1;
        if (directory.listFiles() == null) return -1;

        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .mapToLong(file -> file.isFile() ? file.length() : folderSize(file))
                .sum();
    }

    public static long toMB(long l) {return l / (1024 * 1024);}
}
