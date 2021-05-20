package org.matrixer.core.runtime;

import java.nio.file.Path;
import java.util.*;

public class AgentOptions {

    public static final String DEFAULT_DESTFILENAME = "matrixer-results.txt";
    public static final String DESTFILENAME = "destfile";

    public static final String TARGET_PKG = "pkg";
    public static final String TEST_PKG = "testPkg";
    public static final String DEPTH_LIMIT = "depth";
    public static final String DEBUG = "debug";

    private static final Collection<String> VALID_OPTIONS = Arrays.asList(
        DESTFILENAME, TARGET_PKG, TEST_PKG, DEPTH_LIMIT, DEBUG
    );

    private Map<String, String> options = new HashMap<>();

    public AgentOptions() {

    }

    public AgentOptions(String args) {
        if (args == null || args.length() <= 0) {
            return;
        }
        boolean setDepth = false;
        for (var entry : args.split(",")) {
            final int pos = entry.indexOf('=');
            if (pos == -1) {
                throw new IllegalArgumentException("Not a valid argument " + entry);
            }
            String key = entry.substring(0, pos);
            if (!VALID_OPTIONS.contains(key)) {
                throw new IllegalArgumentException("Not a valid argument " + entry);
            }
            String value = entry.substring(pos + 1);
            setOption(key, value);
            if (key.equals(DEPTH_LIMIT)) {
                setDepth = true;
            }
        }

        if (!setDepth) {
            String depthString = System.getenv("MATRIXER_DEPTH");
            try {
                int depth = Integer.valueOf(depthString);
                if (depth > 0) {
                    setOption(DEPTH_LIMIT, depth);
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    public String getDestFilename() {
        return getOption(DESTFILENAME, DEFAULT_DESTFILENAME);
    }

    public void setDestFilename(String destFilename) {
        options.put(DESTFILENAME, destFilename);
    }

    public void setTargetPackage(String pkg) {
        options.put(TARGET_PKG, pkg);
    }

    public String getTargetPackage() {
        return getOption(TARGET_PKG, "");
    }

    public void setTestPackage(String pkg) {
        options.put(TEST_PKG, pkg);
    }

    public String getTestPackage() {
        return getOption(TEST_PKG, "");
    }

    public boolean getDebug() {
        return getOption(DEBUG, false);
    }

    public void setDebug(boolean debug) {
        setOption(DEBUG, debug);
    }

    public int getDepthLimit() {
        return getOption(DEPTH_LIMIT, 0);
    }

    public void setDepthLimit(int depth) {
        setOption(DEPTH_LIMIT, depth);
    }

    /*
     * General functions
     */

    public void setOption(String key, String value) {
        options.put(key, value);
    }

    public String getOption(String key, String def) {
        String value = options.get(key);
        if (value == null) {
            return def;
        }
        return value;
    }

    public void setOption(String key, boolean value) {
        options.put(key, Boolean.toString(value));
    }

    public boolean getOption(String key, boolean def) {
        String value = options.get(key);
        if (value == null) {
            return def;
        }
        return Boolean.parseBoolean(value);
    }

    public void setOption(String key, int value) {
        options.put(key, Integer.toString(value));
    }

    public int getOption(String key, int def) {
        String value = options.get(key);
        if (value == null) {
            return def;
        }
        return Integer.parseInt(value);
    }

    public String getJVMArgument(Path agentJar) {
        return String.format("-javaagent:%s=%s", agentJar, this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var key : VALID_OPTIONS) {
            String value = options.get(key);
            if (value != null) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }
}
