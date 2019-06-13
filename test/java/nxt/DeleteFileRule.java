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
