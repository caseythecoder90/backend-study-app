package com.flashcards.backend.flashcards.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API documentation annotations for image management endpoints.
 */
public class ImageApiDocumentation {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
        summary = "Upload image",
        description = "Upload an image file to GridFS storage for use in flashcards. " +
                     "Supports JPEG, PNG, GIF, WebP, and SVG formats up to 5MB."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.flashcards.backend.flashcards.dto.ImageUploadResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format, size exceeded, or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during upload",
            content = @Content
        )
    })
    public @interface UploadImage {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
        summary = "Download image",
        description = "Retrieve an uploaded image by its ID. Returns the image file with appropriate content-type header."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image retrieved successfully",
            content = @Content(mediaType = "image/*")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid image ID format",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Image not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during retrieval",
            content = @Content
        )
    })
    public @interface DownloadImage {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
        summary = "Delete image",
        description = "Delete an uploaded image from GridFS storage by its ID. Requires admin privileges or ownership."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Image deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid image ID format",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Image not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during deletion",
            content = @Content
        )
    })
    public @interface DeleteImage {}
}