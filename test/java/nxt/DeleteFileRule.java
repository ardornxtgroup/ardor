package nxt;

import org.junit.rules.ExternalResource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DeleteFileRule extends ExternalResource {
    private final List<File> files = new ArrayList<>();

    public void addFile(File file) {
        files.add(file);
    }

    public void moveToTestClasspath(File file) throws URISyntaxException {
        File parent = new File(ClassLoader.getSystemResource("unit.test.genesis.json").toURI()).getParentFile();
        File to = new File(parent, file.getName());
        assertTrue(file.renameTo(to));
        addFile(to);
    }

    @Override
    protected void after() {
        files.forEach(File::delete);
        files.clear();
    }
}
