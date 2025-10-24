package com.flashcards.backend.flashcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recovery codes status information without exposing actual codes")
public class RecoveryCodeStatusDto {
    @Schema(description = "Number of recovery codes remaining", example = "9")
    private int remainingCodes;

    @Schema(description = "Number of recovery codes already used", example = "1")
    private int usedCodes;

    @Schema(description = "Timestamp when codes were generated", example = "2025-10-23T10:17:57.311")
    private LocalDateTime generatedAt;

    @Schema(description = "Warning message if codes are running low",
            example = "You have only 3 recovery codes remaining. Consider regenerating your codes.")
    private String warning;
}
