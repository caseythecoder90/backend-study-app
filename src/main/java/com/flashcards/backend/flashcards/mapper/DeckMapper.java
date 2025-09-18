package com.flashcards.backend.flashcards.mapper;

import com.flashcards.backend.flashcards.dto.CreateDeckDto;
import com.flashcards.backend.flashcards.dto.DeckDto;
import com.flashcards.backend.flashcards.model.Deck;
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
public interface DeckMapper {

    DeckDto toDto(Deck deck);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flashcardCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastStudiedAt", ignore = true)
    Deck toEntity(CreateDeckDto createDeckDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flashcardCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(@MappingTarget Deck entity, DeckDto dto);

    List<DeckDto> toDtoList(List<Deck> decks);
}