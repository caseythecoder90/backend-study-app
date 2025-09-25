package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TOTP setup response containing QR code and recovery codes")
public class TotpSetupDto {
    @Schema(description = "Base64 encoded QR code image as data URI", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String qrCodeDataUri;

    @Schema(description = "TOTP secret for manual entry", example = "JBSWY3DPEHPK3PXP")
    private String secret;

    @Schema(description = "Instructions for setting up TOTP", example = "Scan this QR code with your authenticator app")
    private String instructions;

    @Schema(description = "Recovery codes for emergency access (save these securely!)",
            example = "[\"ABCD-1234\", \"EFGH-5678\", \"IJKL-9012\", \"MNOP-3456\", \"QRST-7890\", \"UVWX-1234\", \"YZ12-5678\", \"3456-9012\", \"7890-3456\", \"ABCD-7890\"]")
    private List<String> recoveryCodes;

    @Schema(description = "Warning message about recovery codes",
            example = "Save these recovery codes in a secure place. Each code can only be used once. You won't be able to see these codes again!")
    private String recoveryCodeWarning;
}