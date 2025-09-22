package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TOTP setup response containing QR code and backup codes")
public class TotpSetupDto {
    @Schema(description = "Base64 encoded QR code image as data URI", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String qrCodeDataUri;

    @Schema(description = "TOTP secret for manual entry", example = "JBSWY3DPEHPK3PXP")
    private String secret;

    @Schema(description = "Instructions for setting up TOTP", example = "Scan this QR code with your authenticator app")
    private String instructions;
}