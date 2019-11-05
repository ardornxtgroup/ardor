/*
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.util;

import nxt.Nxt;
import nxt.addons.JO;
import nxt.env.RuntimeEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLookup {
    public final static boolean USE_SYSTEM_CLASS_LOADER = !RuntimeEnvironment.isAndroidRuntime();

    public static JO loadJsonResource(String resourceName) {
        try (Reader reader = loadResourceText(resourceName)) {
            if (reader == null) {
                return null;
            }
            return JO.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static JO loadJsonResource(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            return JO.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Reader loadResourceText(String resourceName) {
        InputStream is = loadResourceBytes(resourceName);
        if (is == null) {
            return null;
        }
        return new InputStreamReader(is);
    }

    public static InputStream loadResourceBytes(String resourceName) {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (resource != null) {
            Logger.logInfoMessage("Loading resource from classpath " + resourceName);
            return resource;
        } else {
            Path path = Paths.get(Nxt.getUserHomeDir(), resourceName);
            if (!Files.isReadable(path)) {
                path = Paths.get(resourceName);
                if (!Files.isReadable(path)) {
                    Logger.logErrorMessage("file not found " + path.toAbsolutePath());
                    return null;
                }
            }
            Logger.logInfoMessage("Loading file from path " + path.toAbsolutePath());
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                Logger.logErrorMessage("Cannot read json file from path " + path.toAbsolutePath(), e);
                return null;
            }
        }
    }

    public static byte[] getResourceBytes(String resourceName) {
        try (InputStream is = loadResourceBytes(resourceName)) {
            if (is == null) {
                return null;
            }
            return readInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static InputStream getSystemResourceAsStream(String resourceName) {
        if (USE_SYSTEM_CLASS_LOADER) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            return ResourceLookup.class.getClassLoader().getResourceAsStream(resourceName);
        }
    }

    public static byte[] readInputStream(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return buffer.toByteArray();
    }
}
