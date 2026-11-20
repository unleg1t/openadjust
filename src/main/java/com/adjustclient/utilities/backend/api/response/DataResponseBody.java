package com.adjustclient.utilities.backend.api.response;

public final class DataResponseBody extends ResponseBody {
    public byte[] value;
    public DataResponseBody(byte[] value) { this.value = value; }
}
