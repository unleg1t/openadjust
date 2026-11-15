package io.netty.handler.codec.http;

public interface FullHttpRequest extends HttpRequest, FullHttpMessage {
    @Override
    FullHttpRequest copy();

    @Override
    FullHttpRequest retain(int increment);

    @Override
    FullHttpRequest retain();

    @Override
    FullHttpRequest setProtocolVersion(HttpVersion version);

    @Override
    FullHttpRequest setMethod(HttpMethod method);

    @Override
    FullHttpRequest setUri(String uri);

    default String uri() {
        return getUri();
    }
}
