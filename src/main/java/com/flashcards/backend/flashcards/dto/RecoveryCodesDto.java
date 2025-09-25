package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recovery codes response for regeneration or status check")
public class RecoveryCodesDto {
    @Schema(description = "List of new recovery codes (only shown during generation)",
            example = "[\"ABCD-1234\", \"EFGH-5678\", \"IJKL-9012\", \"MNOP-3456\", \"QRST-7890\", \"UVWX-1234\", \"YZ12-5678\", \"3456-9012\", \"7890-3456\", \"ABCD-7890\"]")
    private List<String> codes;

    @Schema(description = "Number of recovery codes remaining", example = "10")
    private int remainingCodes;

    @Schema(description = "Number of recovery codes already used", example = "0")
    private int usedCodes;

    @Schema(description = "Timestamp when codes were generated", example = "2024-01-15T10:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "Warning message if codes are running low",
            example = "You have only 3 recovery codes remaining. Consider regenerating your codes.")
    private String warning;

    @Schema(description = "Instructions for saving recovery codes",
            example = "Save these recovery codes in a secure place. Each code can only be used once. You won't be able to see these codes again!")
    private String instructions;
}