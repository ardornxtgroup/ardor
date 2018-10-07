// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DetectMimeTypeCall extends APICall.Builder<DetectMimeTypeCall> {
    private DetectMimeTypeCall() {
        super("detectMimeType");
    }

    public static DetectMimeTypeCall create() {
        return new DetectMimeTypeCall();
    }

    public DetectMimeTypeCall filename(String filename) {
        return param("filename", filename);
    }

    public DetectMimeTypeCall data(String data) {
        return param("data", data);
    }

    public DetectMimeTypeCall isText(boolean isText) {
        return param("isText", isText);
    }

    public APICall.Builder file(byte[] b) {
        return parts("file", b);
    }
}
