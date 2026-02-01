package org.umc.valuedi.global.external.codef.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CodefResponseDecoder implements Decoder {

    private final ObjectMapper objectMapper;

    public CodefResponseDecoder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() == null) {
            return null;
        }

        // Response body를 문자열로 읽기
        String responseBody = new String(
                response.body().asInputStream().readAllBytes(),
                StandardCharsets.ISO_8859_1
        );

        // URL 디코딩
        String decodedBody = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);

        // JSON으로 파싱
        return objectMapper.readValue(decodedBody, objectMapper.constructType(type));
    }
}
