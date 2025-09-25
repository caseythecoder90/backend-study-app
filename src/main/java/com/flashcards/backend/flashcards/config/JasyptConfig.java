package com.flashcards.backend.flashcards.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor(JasyptConfigProperties properties) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(properties.getPassword());
        config.setAlgorithm(properties.getAlgorithm());
        config.setKeyObtentionIterations(String.valueOf(properties.getKeyObtentionIterations()));
        config.setPoolSize(String.valueOf(properties.getPoolSize()));
        config.setProviderName(properties.getProviderName());
        config.setSaltGeneratorClassName(properties.getSaltGeneratorClassName());
        config.setIvGeneratorClassName(properties.getIvGeneratorClassName());
        config.setStringOutputType(properties.getStringOutputType());

        encryptor.setConfig(config);
        return encryptor;
    }
}