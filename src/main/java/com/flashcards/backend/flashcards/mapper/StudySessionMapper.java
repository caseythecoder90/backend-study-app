package com.flashcards.backend.flashcards.mapper;

import com.flashcards.backend.flashcards.dto.StudySessionDto;
import com.flashcards.backend.flashcards.model.StudySession;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface StudySessionMapper {

    StudySessionDto toDto(StudySession studySession);

    StudySessionDto.CardResultDto toCardResultDto(StudySession.CardResult cardResult);

    StudySession toEntity(StudySessionDto studySessionDto);

    StudySession.CardResult toCardResult(StudySessionDto.CardResultDto cardResultDto);

    List<StudySessionDto> toDtoList(List<StudySession> studySessions);
}