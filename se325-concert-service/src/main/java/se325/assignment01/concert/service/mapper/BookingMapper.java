package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {
    public static BookingDTO toDTO(Booking booking){
        List<SeatDTO> seatDTO = new ArrayList<>();
        for(Seat s:booking.getSeats()){
            seatDTO.add(SeatMapper.toDto(s));
        }
        return new BookingDTO(
                booking.getConcert().getId(),
                booking.getDate(),
                seatDTO
        );
    }
}
