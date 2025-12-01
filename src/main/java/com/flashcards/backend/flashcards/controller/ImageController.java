package com.flashcards.backend.flashcards.controller;

import com.flashcards.backend.flashcards.annotation.ImageApiDocumentation;
import com.flashcards.backend.flashcards.dto.ImageUploadResponseDto;
import com.flashcards.backend.flashcards.service.ImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Controller for image upload, storage, and retrieval operations.
 * Handles image management for flashcard content blocks.
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs for uploading, downloading, and managing images for flashcards")
public class ImageController {

    private final ImageService imageService;

    /**
     * Upload an image file to GridFS storage.
     *
     * @param file           The image file to upload
     * @param authentication Current user authentication
     * @return Image upload response with ID and URL
     */
    @PostMapping("/upload")
    @ImageApiDocumentation.UploadImage
    public ResponseEntity<ImageUploadResponseDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("POST /api/images/upload - Uploading image for user: {}", userId);

        String imageUrl = imageService.uploadImage(file, userId);

        String imageId = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        ImageUploadResponseDto response = ImageUploadResponseDto.builder()
                .imageId(imageId)
                .url(imageUrl)
                .build();

        log.info("Successfully uploaded image with ID: {} for user: {}", imageId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Download an image by its ID.
     * Returns the image file with appropriate content-type header.
     *
     * @param id The image ID
     * @return Image file as Resource
     */
    @GetMapping("/{id}")
    @ImageApiDocumentation.DownloadImage
    public ResponseEntity<Resource> downloadImage(@PathVariable String id) {
        log.info("GET /api/images/{} - Downloading image", id);

        GridFsResource resource = imageService.getImage(id);

        // Determine content type
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        resource.getContentType();
        if (isNotBlank(resource.getContentType())) {
            contentType = resource.getContentType();
        }

        // Set content disposition for inline display
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("inline", resource.getFilename());

        log.info("Successfully retrieved image: {} with content-type: {}", id, contentType);
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * Delete an image by its ID.
     * Requires authentication - users can delete their own images.
     *
     * @param id             The image ID
     * @param authentication Current user authentication
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @ImageApiDocumentation.DeleteImage
    public ResponseEntity<Void> deleteImage(
            @PathVariable String id,
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("DELETE /api/images/{} - Deleting image for user: {}", id, userId);

        imageService.deleteImage(id);

        log.info("Successfully deleted image: {}", id);
        return ResponseEntity.noContent().build();
    }
}