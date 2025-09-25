package com.flashcards.backend.flashcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jasypt.encryptor")
public class JasyptConfigProperties {

    private String password;
    private String algorithm;
    private Integer keyObtentionIterations;
    private Integer poolSize;
    private String providerName;
    private String saltGeneratorClassName;
    private String ivGeneratorClassName;
    private String stringOutputType;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getKeyObtentionIterations() {
        return keyObtentionIterations;
    }

    public void setKeyObtentionIterations(Integer keyObtentionIterations) {
        this.keyObtentionIterations = keyObtentionIterations;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getSaltGeneratorClassName() {
        return saltGeneratorClassName;
    }

    public void setSaltGeneratorClassName(String saltGeneratorClassName) {
        this.saltGeneratorClassName = saltGeneratorClassName;
    }

    public String getIvGeneratorClassName() {
        return ivGeneratorClassName;
    }

    public void setIvGeneratorClassName(String ivGeneratorClassName) {
        this.ivGeneratorClassName = ivGeneratorClassName;
    }

    public String getStringOutputType() {
        return stringOutputType;
    }

    public void setStringOutputType(String stringOutputType) {
        this.stringOutputType = stringOutputType;
    }
}