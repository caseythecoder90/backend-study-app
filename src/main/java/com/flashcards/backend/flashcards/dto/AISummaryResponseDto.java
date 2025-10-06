package com.flashcards.backend.flashcards.dto;

import com.flashcards.backend.flashcards.enums.AIModelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing AI-generated summary")
public class AISummaryResponseDto {

    @Schema(description = "Generated summary content")
    private String summary;

    @Schema(description = "Format of the summary")
    private AISummaryRequestDto.SummaryFormat format;

    @Schema(description = "Length category of the summary")
    private AISummaryRequestDto.SummaryLength length;

    @Schema(description = "Actual word count of the summary")
    private int wordCount;

    @Schema(description = "AI model used to generate the summary")
    private AIModelEnum modelUsed;

    @Schema(description = "Source type that was summarized")
    private AISummaryRequestDto.SummarySourceType sourceType;

    @Schema(description = "ID of the deck if applicable")
    private String deckId;

    @Schema(description = "Number of flashcards summarized if applicable")
    private Integer flashcardCount;

    @Schema(description = "Timestamp when the summary was generated")
    private Instant generatedAt;

    @Schema(description = "Time taken to generate summary in milliseconds")
    private long generationTimeMs;
}