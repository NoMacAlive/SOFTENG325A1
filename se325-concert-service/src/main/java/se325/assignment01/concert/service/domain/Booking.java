package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(
            cascade = CascadeType.PERSIST,
            fetch = FetchType.EAGER
    )
    private User user;

    @OneToMany(
            cascade = CascadeType.PERSIST
    )
    @JoinTable(
            name = "BOOKING_SEAT",
            joinColumns = @JoinColumn(name = "BOOKING_ID")
    )
    private List<Seat> seats = new ArrayList<>();

    @ManyToOne(
            cascade = CascadeType.PERSIST,
            fetch = FetchType.EAGER
    )
    private Concert concert;

    private LocalDateTime date;


    public Booking(){}
    public Booking (Concert concert, LocalDateTime date, List<Seat> seats){
        this.concert = concert;
        this.date = date;
        this.seats = seats;
    }

    public Booking (Concert concert, LocalDateTime date, List<Seat> seats, User user){
        this.concert = concert;
        this.date = date;
        this.seats = seats;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return this.id;
    }
}
