package com.jerry.pilipala.infrastructure.config.resttemplate;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest,
                                        byte[] body,
                                        ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        // url
        // method
        // headers
        // body
        RequestInfo requestInfo = extractRequest(httpRequest, body);
        ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, body);

        // status code
        // status text
        // headers
        // body
        ResponseInfo responseInfo = extractResponse(response);

        log.debug("call url: {},method: {}, request: {}, response: {}",
                requestInfo.url,
                requestInfo.method,
                requestInfo,
                responseInfo);

        if (response.getStatusCode().isError()) {
            throw BusinessException.businessError("第三方数据请求异常");
        }
        return response;
    }

    private RequestInfo extractRequest(HttpRequest httpRequest, byte[] body) {
        RequestInfo requestInfo = new RequestInfo();
        return requestInfo.setUrl(httpRequest.getURI().toString())
                .setMethod(httpRequest.getMethodValue())
                .setHeaders(httpRequest.getHeaders().toString())
                .setBody(body.toString());
    }

    private ResponseInfo extractResponse(ClientHttpResponse response) throws IOException {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setStatusCode(response.getStatusCode().value())
                .setStatusText(response.getStatusText())
                .setHeaders(response.getHeaders().toString());

        InputStream inputStream = response.getBody();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }
        responseInfo.setBody(builder.toString());
        return responseInfo;
    }

    @Data
    @Accessors(chain = true)
    public static class RequestInfo {
        private String url;
        private String method;
        private String headers;
        private String body;
    }

    @Data
    @Accessors(chain = true)
    public static class ResponseInfo {
        private Integer statusCode;
        private String statusText;
        private String headers;
        private String body;
    }
}
