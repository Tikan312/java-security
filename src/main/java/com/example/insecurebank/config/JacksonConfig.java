package com.example.insecurebank.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // INSECURE: enabling default typing with permissive validator allows polymorphic deserialization and gadget chains (RCE risk)
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        // INSECURE: allows broad subtype resolution, enabling untrusted types to be instantiated
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }
}
