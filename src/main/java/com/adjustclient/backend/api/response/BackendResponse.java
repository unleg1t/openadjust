package com.adjustclient.backend.api.response;

import com.adjustclient.backend.api.PendingRequest;
public class BackendResponse {
    public PendingRequest request;
    public ResponseStatus status;
    public ResponseBody body;
    public BackendResponse(PendingRequest request, ResponseStatus status, ResponseBody body) {
        this.request = request; this.status = status; this.body = body;
    }
    @Override public String toString() {
        if (body instanceof DataResponseBody) return "Response(" + status + ", data=" + ((DataResponseBody) body).value.length + ")";
        if (body instanceof IntResponseBody) return "Response(" + status + ", int=" + ((IntResponseBody) body).value + ")";
        if (body instanceof TextResponseBody) return "Response(" + status + ", text=" + ((TextResponseBody) body).value + ")";
        if (body instanceof EnumResponseBody) return "Response(" + status + ", enum=" + ((EnumResponseBody) body).value.name() + ")";
        return "Response(" + status + ")";
    }
}
