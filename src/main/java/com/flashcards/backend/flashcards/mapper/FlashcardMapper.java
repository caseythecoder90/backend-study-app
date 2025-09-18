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

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FlashcardMapper {

    FlashcardDto toDto(Flashcard flashcard);

    FlashcardDto.CardContentDto toCardContentDto(Flashcard.CardContent cardContent);

    FlashcardDto.CodeBlockDto toCodeBlockDto(Flashcard.CodeBlock codeBlock);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "difficulty", constant = "NOT_SET")
    @Mapping(target = "timesStudied", constant = "0")
    @Mapping(target = "timesCorrect", constant = "0")
    @Mapping(target = "timesIncorrect", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastStudiedAt", ignore = true)
    Flashcard toEntity(CreateFlashcardDto createFlashcardDto);

    Flashcard.CardContent toCardContent(FlashcardDto.CardContentDto cardContentDto);

    Flashcard.CodeBlock toCodeBlock(FlashcardDto.CodeBlockDto codeBlockDto);

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
}