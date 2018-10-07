package nxt.addons;

import nxt.http.responses.BlockResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class RequestContext extends AbstractContractContext {

    private final HttpServletRequest req;

    public RequestContext(HttpServletRequest req, ContractRunnerConfig config, String contractName) {
        super(config, contractName);
        this.source = EventSource.REQUEST;
        this.req = req;
    }

    @Override
    public BlockResponse getBlock() {
        throw new UnsupportedOperationException();
    }

    public HttpServletRequest getRequest() {
        return req;
    }

    public Object getAttribute(String name) {
        return req.getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        return req.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return req.getCharacterEncoding();
    }

    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        req.setCharacterEncoding(enc);
    }

    public int getContentLength() {
        return req.getContentLength();
    }

    public long getContentLengthLong() {
        return req.getContentLengthLong();
    }

    public String getContentType() {
        return req.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return req.getInputStream();
    }

    public String getParameter(String name) {
        return req.getParameter(name);
    }

    public Map<String, String[]> getParameterMap() {
        return req.getParameterMap();
    }

    public Enumeration<String> getParameterNames() {
        return req.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return req.getParameterValues(name);
    }

    public String getProtocol() {
        return req.getProtocol();
    }

    public String getScheme() {
        return req.getScheme();
    }

    public String getServerName() {
        return req.getServerName();
    }

    public int getServerPort() {
        return req.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return req.getReader();
    }

    public String getRemoteAddr() {
        return req.getRemoteAddr();
    }

    public String getRemoteHost() {
        return req.getRemoteHost();
    }

    public void setAttribute(String name, Object o) {
        req.setAttribute(name, o);
    }

    public void removeAttribute(String name) {
        req.removeAttribute(name);
    }

    public Locale getLocale() {
        return req.getLocale();
    }

    public Enumeration<Locale> getLocales() {
        return req.getLocales();
    }

    public boolean isSecure() {
        return req.isSecure();
    }

    @Override
    protected String getReferencedTransaction() {
        return null;
    }
}
