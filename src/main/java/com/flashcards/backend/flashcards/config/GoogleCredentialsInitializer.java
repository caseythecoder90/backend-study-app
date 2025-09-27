package com.flashcards.backend.flashcards.config;

import com.flashcards.backend.flashcards.exception.ConfigurationException;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_DECODE_ERROR;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_ENV_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_FILE_CREATE_ERROR;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_FILENAME;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_FILE_PREFIX;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_INIT_SUCCESS;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_INVALID_BASE64;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_PROPERTY_SOURCE;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GCP_CREDENTIALS_WRITE_ERROR;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.GOOGLE_CREDENTIALS_ENV_VAR;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.TEMP_DIR_PREFIX;
import static com.flashcards.backend.flashcards.constants.SecurityConstants.VERTEX_AI_CREDENTIALS_URI_PROPERTY;

/**
 * ApplicationContextInitializer that handles Google Cloud credentials from base64-encoded environment variable.
 * This runs before Spring binds configuration properties, allowing us to decode the credentials
 * and write them to a temporary file that can be referenced in the configuration.
 */
@Slf4j
public class GoogleCredentialsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String base64Credentials = environment.getProperty(GOOGLE_CREDENTIALS_ENV_VAR);

        if (StringUtils.isNotBlank(base64Credentials)) {
            try {
                // Decode the base64 credentials
                byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);

                // Create a temporary directory for the credentials file
                Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
                tempDir.toFile().deleteOnExit();

                // Generate a unique filename to avoid conflicts
                String fileName = GCP_CREDENTIALS_FILENAME.formatted(UUID.randomUUID());
                Path credentialsFile = tempDir.resolve(fileName);

                // Write the decoded credentials to the temporary file
                Files.write(credentialsFile, decodedCredentials);
                credentialsFile.toFile().deleteOnExit();

                // Add the file path as a property that can be used in the configuration
                Map<String, Object> properties = new HashMap<>();
                String fileUri = GCP_CREDENTIALS_FILE_PREFIX + credentialsFile.toAbsolutePath();
                properties.put(VERTEX_AI_CREDENTIALS_URI_PROPERTY, fileUri);

                // Add this property source with high priority
                MutablePropertySources propertySources = environment.getPropertySources();
                propertySources.addFirst(new MapPropertySource(GCP_CREDENTIALS_PROPERTY_SOURCE, properties));

                log.info(GCP_CREDENTIALS_INIT_SUCCESS);

            } catch (IllegalArgumentException e) {
                log.error("Failed to decode base64 Google credentials: {}", e.getMessage());
                throw new ConfigurationException(
                    GCP_CREDENTIALS_INVALID_BASE64,
                    ErrorCode.CONFIG_CREDENTIALS_DECODE_ERROR,
                    e
                );
            } catch (IOException e) {
                log.error("Failed to write Google credentials to temporary file: {}", e.getMessage());
                throw new ConfigurationException(
                    GCP_CREDENTIALS_FILE_CREATE_ERROR,
                    ErrorCode.CONFIG_CREDENTIALS_FILE_ERROR,
                    e
                );
            }
        } else {
            log.info(GCP_CREDENTIALS_ENV_NOT_FOUND);
        }
    }
}