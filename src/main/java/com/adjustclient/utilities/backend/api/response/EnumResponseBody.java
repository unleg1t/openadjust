package com.adjustclient.utilities.backend.api.response;

public final class EnumResponseBody extends ResponseBody {
    public Enum<?> value;
    public EnumResponseBody(Enum<?> value) { this.value = value; }
}
