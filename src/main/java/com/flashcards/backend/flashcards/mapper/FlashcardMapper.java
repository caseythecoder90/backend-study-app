package com.flashcards.backend.flashcards.mapper;

import com.flashcards.backend.flashcards.dto.CreateFlashcardDto;
import com.flashcards.backend.flashcards.dto.FlashcardDto;
import com.flashcards.backend.flashcards.model.Flashcard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for converting between Flashcard entities and DTOs.
 * Handles polymorphic content blocks (Text, Code, Image, Mermaid).
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FlashcardMapper {

    // High-level flashcard mappings
    FlashcardDto toDto(Flashcard flashcard);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "difficulty", constant = "NOT_SET")
    @Mapping(target = "timesStudied", constant = "0")
    @Mapping(target = "timesCorrect", constant = "0")
    @Mapping(target = "timesIncorrect", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastStudiedAt", ignore = true)
    Flashcard toEntity(CreateFlashcardDto createFlashcardDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "timesStudied", ignore = true)
    @Mapping(target = "timesCorrect", ignore = true)
    @Mapping(target = "timesIncorrect", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastStudiedAt", ignore = true)
    void updateEntity(@MappingTarget Flashcard entity, FlashcardDto dto);

    List<FlashcardDto> toDtoList(List<Flashcard> flashcards);

    // Card content mappings
    FlashcardDto.CardContentDto toCardContentDto(Flashcard.CardContent cardContent);
    Flashcard.CardContent toCardContent(FlashcardDto.CardContentDto cardContentDto);

    // Content block mappings - Entity to DTO
    FlashcardDto.TextBlockDto toTextBlockDto(Flashcard.TextBlock textBlock);
    FlashcardDto.CodeBlockDto toCodeBlockDto(Flashcard.CodeBlock codeBlock);
    FlashcardDto.ImageBlockDto toImageBlockDto(Flashcard.ImageBlock imageBlock);
    FlashcardDto.MermaidBlockDto toMermaidBlockDto(Flashcard.MermaidBlock mermaidBlock);

    // Content block mappings - DTO to Entity
    Flashcard.TextBlock toTextBlock(FlashcardDto.TextBlockDto textBlockDto);
    Flashcard.CodeBlock toCodeBlock(FlashcardDto.CodeBlockDto codeBlockDto);
    Flashcard.ImageBlock toImageBlock(FlashcardDto.ImageBlockDto imageBlockDto);
    Flashcard.MermaidBlock toMermaidBlock(FlashcardDto.MermaidBlockDto mermaidBlockDto);

    // Polymorphic block list mappings
    List<FlashcardDto.ContentBlockDto> toContentBlockDtoList(List<Flashcard.ContentBlock> blocks);
    List<Flashcard.ContentBlock> toContentBlockList(List<FlashcardDto.ContentBlockDto> blockDtos);
}