package josegamerpt.realregions;

import java.util.logging.Level;

public class Debugger {

    private boolean on = false;

    public static void debug(Class a, String s)
    {
        RealRegions.log(Level.WARNING, "[DEBUGGER-RealRegions] [" + getName(a) + "] > " + s);
    }

    static String getName(Class a) {
        Class<?> enclosingClass = a.getEnclosingClass();
        if (enclosingClass != null) {
            return enclosingClass.getName();
        } else {
            return a.getName();
        }
    }
}
