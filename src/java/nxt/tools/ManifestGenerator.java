/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.tools;

import nxt.Nxt;
import nxt.env.service.ArdorService_ServiceManagement;
import nxt.util.security.BlockchainPermission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestGenerator {

    public static void main(String[] args) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        ManifestGenerator manifestGenerator = new ManifestGenerator();
        manifestGenerator.generate("./resource/ardor.manifest.mf", Nxt.class.getCanonicalName(), "./lib", "./javafx-sdk/lib");
        String serviceClassName = ArdorService_ServiceManagement.class.getCanonicalName();
        serviceClassName = serviceClassName.substring(0, serviceClassName.length() - "_ServiceManagement".length());
        manifestGenerator.generate("./resource/ardorservice.manifest.mf", serviceClassName, "./lib");
    }

    private void generate(String fileName, String className, String ... directories) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, className);
        StringBuilder classpath = new StringBuilder();

        for (String directory : directories) {
            DirListing dirListing = new DirListing();
            try {
                Files.walkFileTree(Paths.get(directory), EnumSet.noneOf(FileVisitOption.class), 2, dirListing);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            classpath.append(dirListing.getFileList().toString());
        }
        classpath.append("conf/");
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath.toString());
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            manifest.write(Files.newOutputStream(path, StandardOpenOption.CREATE));
            System.out.println("Manifest file " + fileName + " generated");

            // Print the file content to assist debugging
            byte[] encoded = Files.readAllBytes(path);
            System.out.println(new String(encoded, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class DirListing extends SimpleFileVisitor<Path> {

        private final StringBuilder fileList = new StringBuilder();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            Path dir = file.subpath(1, file.getNameCount() - 1);
            fileList.append(dir.toString().replace('\\', '/')).append('/').append(file.getFileName()).append(' ');
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.CONTINUE;
        }

        public StringBuilder getFileList() {
            return fileList;
        }
    }
}
