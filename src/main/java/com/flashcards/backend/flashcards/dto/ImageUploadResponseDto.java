package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for image upload operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing uploaded image details")
public class ImageUploadResponseDto {

    @Schema(description = "Unique identifier of the uploaded image", example = "507f1f77bcf86cd799439011")
    private String imageId;

    @Schema(description = "URL to access the uploaded image", example = "/api/images/507f1f77bcf86cd799439011")
    private String url;
}