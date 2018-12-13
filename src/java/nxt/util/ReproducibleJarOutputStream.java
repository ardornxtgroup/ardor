package nxt.util;

import nxt.Constants;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Produce a Jar that given the same class files and timestamps, generates the binary Jar file
 * Use this to generate test data which won't change
 */
public class ReproducibleJarOutputStream extends JarOutputStream {

    private long entryTime;

    public ReproducibleJarOutputStream(OutputStream out, Manifest man, long entryTime) throws IOException {
        super(out);
        this.entryTime = entryTime;
        if (man == null) {
            throw new NullPointerException("man");
        }
        ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
        e.setTime(Constants.EPOCH_BEGINNING); // Set the time of the manifest (if not set here, the Zip sets it to the current time)
        putNextEntry(e);
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }

    @Override
    public void putNextEntry(ZipEntry ze) throws IOException {
        ze.setTime(entryTime);
        super.putNextEntry(ze);
    }
}
