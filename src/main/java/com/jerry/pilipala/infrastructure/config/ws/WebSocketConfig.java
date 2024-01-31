package com.jerry.pilipala.infrastructure.config.ws;

import com.jerry.pilipala.infrastructure.annotations.WsMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final List<WebSocketHandler> handlers;

    public WebSocketConfig(List<WebSocketHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (WebSocketHandler handler : handlers) {
            WsMapping annotation = handler.getClass().getAnnotation(WsMapping.class);
            registry.addHandler(handler, annotation.value())
                    .addInterceptors(new WebSocketInterceptor(annotation.value()))
                    .setAllowedOrigins("*");
        }
    }


    public static class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {
        private final String uri;

        public WebSocketInterceptor(String uri) {
            this.uri = uri;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request,
                                       ServerHttpResponse response,
                                       WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) throws Exception {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String requestURI = servletRequest.getRequestURI();
            UriTemplate uriTemplate = new UriTemplate(uri);
            Map<String, String> match = uriTemplate.match(requestURI);
            attributes.putAll(match);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        }
    }
}
