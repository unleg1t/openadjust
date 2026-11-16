package com.adjustclient.ui.account.login.auth;

import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.hud.notification.NotificationBuilder;

public class BrowserLoginRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String CLIENT_SECRET = "G.F8Q~x.uPo1CEz5I2CRpkx-KkVjNn1rNVM1ScYg";

    /**
     * Writes a 200 response with the "close this page" HTML (Content-Type "text/html; charset=UTF-8") and closes the
     * connection after the write.
     */
    private void sendCloseWindowPage(ChannelHandlerContext ctx) {
        ByteBuf content = Unpooled.buffer(0);
        content.writeBytes("<html>You can now close this page and return to Adjust.<script>close()</script></html>".getBytes(StandardCharsets.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + "; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            if (!request.uri().contains("?code=")) {
                return;
            }
            String code = request.uri().split("\\?code=")[1];
            String response = MicrosoftAuth.post("https://login.live.com/oauth20_token.srf",
                    "client_id=c4f0db78-5015-4a94-8b34-1a4da87b9ce4&code=" + code + "&client_secret=" + CLIENT_SECRET
                            + "&grant_type=authorization_code&redirect_uri=http://localhost:" + MicrosoftRefreshTokenAccount.port,
                    false);
            if (response == null) {
                return;
            }
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (json.has("refresh_token")) {
                MicrosoftRefreshTokenAccount.refreshTokenCallback.accept(json.get("refresh_token").getAsString());
                sendCloseWindowPage(ctx);
                new NotificationBuilder("Browser Account").addText("Successfully logged in!", 0xFFBEBCBD).show(); // addText(..).show()
                MicrosoftRefreshTokenAccount.stopServer();
            }
        } catch (Exception e) {
            new NotificationBuilder("Browser Account").format("Error during browser exchange: {}", e).show(); // format(..).show()
            Logger.log(LogLevel.ERROR, true, "Error happened while handling browser exchange: {}", new Object[] { e });
            throw new RuntimeException("Browser exchange", e);
        }
    }
}
