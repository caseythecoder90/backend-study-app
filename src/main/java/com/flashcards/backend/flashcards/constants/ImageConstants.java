package com.flashcards.backend.flashcards.constants;

import java.util.Set;

/**
 * Constants for image upload and storage configuration.
 */
public class ImageConstants {

    // File size limits
    public static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    public static final long MAX_IMAGE_SIZE_MB = 5;

    // Allowed image formats
    public static final Set<String> ALLOWED_IMAGE_FORMATS = Set.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/svg+xml"
    );

    // Allowed file extensions
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        "jpg",
        "jpeg",
        "png",
        "gif",
        "webp",
        "svg"
    );

    // API paths
    public static final String IMAGE_API_BASE_PATH = "/api/images";
    public static final String IMAGE_DOWNLOAD_PATH_TEMPLATE = "/api/images/%s";

    // GridFS metadata fields
    public static final String METADATA_USER_ID = "userId";
    public static final String METADATA_ORIGINAL_FILENAME = "originalFilename";
    public static final String METADATA_UPLOAD_DATE = "uploadDate";
    public static final String METADATA_FILE_SIZE = "fileSize";

    private ImageConstants() {
        // Private constructor to prevent instantiation
    }
}