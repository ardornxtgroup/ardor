package nxt.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

public class MockedServletOutputStream extends ServletOutputStream {

    private final OutputStream out;

    public MockedServletOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}
