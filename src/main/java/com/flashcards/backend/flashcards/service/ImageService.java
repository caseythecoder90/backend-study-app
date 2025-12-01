package com.flashcards.backend.flashcards.service;

import com.flashcards.backend.flashcards.annotation.TransientRetryableTemplate;
import com.flashcards.backend.flashcards.exception.ErrorCode;
import com.flashcards.backend.flashcards.exception.ServiceException;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_DELETE_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_DOWNLOAD_FAILED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_FILE_EMPTY;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_FILE_NULL;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_FORMAT_NOT_SUPPORTED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_INVALID_ID;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_NOT_FOUND;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_SIZE_EXCEEDED;
import static com.flashcards.backend.flashcards.constants.ErrorMessages.IMAGE_UPLOAD_FAILED;
import static com.flashcards.backend.flashcards.constants.ImageConstants.ALLOWED_IMAGE_FORMATS;
import static com.flashcards.backend.flashcards.constants.ImageConstants.IMAGE_DOWNLOAD_PATH_TEMPLATE;
import static com.flashcards.backend.flashcards.constants.ImageConstants.MAX_IMAGE_SIZE_BYTES;
import static com.flashcards.backend.flashcards.constants.ImageConstants.MAX_IMAGE_SIZE_MB;
import static com.flashcards.backend.flashcards.constants.ImageConstants.METADATA_FILE_SIZE;
import static com.flashcards.backend.flashcards.constants.ImageConstants.METADATA_ORIGINAL_FILENAME;
import static com.flashcards.backend.flashcards.constants.ImageConstants.METADATA_USER_ID;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Service for handling image upload, storage, and retrieval using MongoDB GridFS.
 * <p>
 * Implements retry logic for transient MongoDB failures using @TransientRetryableTemplate.
 * Public methods handle validation, private methods handle storage with automatic retry.
 * Recovery methods catch exceptions after all retries are exhausted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final GridFsTemplate gridFsTemplate;

    /**
     * Upload an image to GridFS storage.
     * Validates the file then delegates to retry-enabled storage method.
     *
     * @param file   The multipart file to upload
     * @param userId The ID of the user uploading the image
     * @return The download URL for the uploaded image
     * @throws ServiceException if upload fails or validation fails
     */
    public String uploadImage(MultipartFile file, String userId) {
        log.info("Uploading image for user: {}", userId);
        validateFile(file);
        return uploadToGridFS(file, userId);
    }

    /**
     * Retrieve an image from GridFS storage.
     * Validates the image ID then delegates to retry-enabled retrieval method.
     *
     * @param imageId The ID of the image to retrieve
     * @return GridFsResource containing the image data
     * @throws ServiceException if image not found or retrieval fails
     */
    public GridFsResource getImage(String imageId) {
        log.info("Retrieving image with ID: {}", imageId);
        validateImageId(imageId);
        return retrieveFromGridFS(imageId);
    }

    /**
     * Delete an image from GridFS storage.
     * Validates the image ID then delegates to retry-enabled deletion method.
     *
     * @param imageId The ID of the image to delete
     * @throws ServiceException if deletion fails
     */
    public void deleteImage(String imageId) {
        log.info("Deleting image with ID: {}", imageId);
        validateImageId(imageId);
        deleteFromGridFS(imageId);
    }

    /**
     * Check if an image exists in GridFS storage.
     *
     * @param imageId The ID of the image to check
     * @return true if image exists, false otherwise
     */
    public boolean imageExists(String imageId) {
        if (isBlank(imageId) || isFalse(ObjectId.isValid(imageId))) {
            return false;
        }

        GridFSFile gridFSFile = gridFsTemplate.findOne(
            new Query(Criteria.where("_id").is(new ObjectId(imageId)))
        );

        return nonNull(gridFSFile);
    }

    /**
     * Upload file to GridFS with automatic retry on transient failures.
     * Throws IOException and MongoException naturally - retry handles MongoException,
     * recovery method handles both after retries exhausted.
     *
     * @param file   The file to upload
     * @param userId The user ID
     * @return The download URL
     */
    @SneakyThrows
    @TransientRetryableTemplate
    private String uploadToGridFS(MultipartFile file, String userId) {
        ObjectId fileId = gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType(),
            createMetadata(file, userId)
        );

        String imageUrl = IMAGE_DOWNLOAD_PATH_TEMPLATE.formatted(fileId.toString());
        log.info("Successfully uploaded image with ID: {} for user: {}", fileId, userId);
        return imageUrl;
    }

    /**
     * Recovery method for uploadToGridFS - called when all retries are exhausted.
     * Catches both IOException (file reading) and MongoException (storage).
     */
    @Recover
    private String recoverUploadToGridFS(Exception e, MultipartFile file, String userId) {
        log.error("Failed to upload image for user {} after all retries: {}", userId, e.getMessage());
        throw new ServiceException(IMAGE_UPLOAD_FAILED.formatted(e.getMessage()), ErrorCode.IMAGE_UPLOAD_FAILED, e);
    }

    /**
     * Retrieve file from GridFS with automatic retry on transient failures.
     *
     * @param imageId The image ID
     * @return GridFsResource containing the image
     */
    @TransientRetryableTemplate
    private GridFsResource retrieveFromGridFS(String imageId) {
        GridFSFile gridFSFile = gridFsTemplate.findOne(
            new Query(Criteria.where("_id").is(new ObjectId(imageId)))
        );

        if (isNull(gridFSFile)) {
            log.warn("Image not found with ID: {}", imageId);
            throw new ServiceException(IMAGE_NOT_FOUND.formatted(imageId), ErrorCode.NOT_FOUND);
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
        log.info("Successfully retrieved image with ID: {}", imageId);
        return resource;
    }

    /**
     * Recovery method for retrieveFromGridFS - called when all retries are exhausted.
     */
    @Recover
    private GridFsResource recoverRetrieveFromGridFS(Exception e, String imageId) {
        log.error("Failed to retrieve image {} after all retries: {}", imageId, e.getMessage());
        throw new ServiceException(IMAGE_DOWNLOAD_FAILED.formatted(imageId, e.getMessage()), ErrorCode.IMAGE_RETRIEVAL_FAILED, e);
    }

    /**
     * Delete file from GridFS with automatic retry on transient failures.
     *
     * @param imageId The image ID
     */
    @TransientRetryableTemplate
    private void deleteFromGridFS(String imageId) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(imageId))));
        log.info("Successfully deleted image with ID: {}", imageId);
    }

    /**
     * Recovery method for deleteFromGridFS - called when all retries are exhausted.
     */
    @Recover
    private void recoverDeleteFromGridFS(Exception e, String imageId) {
        log.error("Failed to delete image {} after all retries: {}", imageId, e.getMessage());
        throw new ServiceException(IMAGE_DELETE_FAILED.formatted(imageId, e.getMessage()), ErrorCode.IMAGE_DELETE_FAILED, e);
    }

    /**
     * Validate uploaded file meets requirements.
     *
     * @param file The file to validate
     * @throws ServiceException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (isNull(file)) {
            throw new ServiceException(IMAGE_FILE_NULL, ErrorCode.VALIDATION_FAILED);
        }

        if (isTrue(file.isEmpty())) {
            throw new ServiceException(IMAGE_FILE_EMPTY, ErrorCode.VALIDATION_FAILED);
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ServiceException(
                IMAGE_SIZE_EXCEEDED.formatted(MAX_IMAGE_SIZE_MB),
                ErrorCode.VALIDATION_FAILED
            );
        }

        String contentType = file.getContentType();
        if (isBlank(contentType) || isFalse(ALLOWED_IMAGE_FORMATS.contains(contentType))) {
            throw new ServiceException(
                IMAGE_FORMAT_NOT_SUPPORTED.formatted(
                    nonNull(contentType) ? contentType : "unknown",
                    String.join(", ", ALLOWED_IMAGE_FORMATS)
                ),
                ErrorCode.VALIDATION_FAILED
            );
        }
    }

    /**
     * Validate image ID format.
     *
     * @param imageId The image ID to validate
     * @throws ServiceException if invalid
     */
    private void validateImageId(String imageId) {
        if (isBlank(imageId) || isFalse(ObjectId.isValid(imageId))) {
            throw new ServiceException(IMAGE_INVALID_ID.formatted(imageId), ErrorCode.VALIDATION_FAILED);
        }
    }

    /**
     * Create metadata document for GridFS storage.
     *
     * @param file   The uploaded file
     * @param userId The user ID
     * @return Metadata document
     */
    private org.bson.Document createMetadata(MultipartFile file, String userId) {
        return new org.bson.Document()
            .append(METADATA_USER_ID, userId)
            .append(METADATA_ORIGINAL_FILENAME, file.getOriginalFilename())
            .append(METADATA_FILE_SIZE, file.getSize());
    }
}