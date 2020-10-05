package com.spring.boot.example.mybatis.web.rest.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ErrorResourceSerializer extends JsonSerializer<ErrorResource>  {

    @Override
    public void serialize(ErrorResource value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String, List<String>> json = new HashMap<>();

        gen.writeStartObject();
        gen.writeObjectFieldStart("errors");

        for (FieldErrorResource fieldErrorResource : value.getFieldErrors()) {
            if (!json.containsKey(fieldErrorResource.getField())) {
                json.put(fieldErrorResource.getField(), new ArrayList<>());
            }

            json.get(fieldErrorResource.getField()).add(fieldErrorResource.getMessage());
        }

        for (Map.Entry<String, List<String>> pair : json.entrySet()) {
            gen.writeArrayFieldStart(pair.getKey());

            pair.getValue().forEach(content -> {
                try {
                    gen.writeString(content);
                } catch (IOException e) {
                    log.error("Error in json serializer", e);
                }
            });

            gen.writeEndArray();
        }

        gen.writeEndObject();
        gen.writeEndObject();
    }

}
