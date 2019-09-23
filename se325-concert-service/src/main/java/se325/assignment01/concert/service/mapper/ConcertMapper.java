package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to convert from domain model to DTO or from DTO to domain model
 */
public class ConcertMapper {
    public static Concert toDomainModel(ConcertDTO dtoConcert) {
        List<Performer> performers = new ArrayList<>();
        for(PerformerDTO p:dtoConcert.getPerformers()){
            performers.add(PerformerMapper.toDomainModel(p));
        }
        Set<LocalDateTime> dates = new HashSet<>();
        for(LocalDateTime d:dtoConcert.getDates()){
            dates.add(d);
        }

        return new Concert(
                dtoConcert.getId(),
                dtoConcert.getTitle(),
                dates,
                performers
                );

    }

    public static ConcertDTO toDto(Concert concert) {
        List<PerformerDTO> performers = new ArrayList<>();
        for(Performer p: concert.getPerformer()){
            performers.add(PerformerMapper.toDTO(p));
        }
        ConcertDTO concertDTO = new ConcertDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getImage_Name(),
                concert.getBlurb()
        );
        List<LocalDateTime> dates = new ArrayList<>();
        for(LocalDateTime d: concert.getDates()){
            dates.add(d);
        }
        concertDTO.setDates(dates);
        concertDTO.setPerformers(performers);
        return concertDTO;
    }
}
