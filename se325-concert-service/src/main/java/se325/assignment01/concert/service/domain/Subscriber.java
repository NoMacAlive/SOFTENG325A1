package se325.assignment01.concert.service.domain;

import javax.ws.rs.container.AsyncResponse;
import java.time.LocalDateTime;

/**
 * This class represent a subscriber
 */
public class Subscriber {
    private long concertId;
    private AsyncResponse sub;
    private LocalDateTime localDateTime;
    private int percentage;

    public Subscriber(long concertId, AsyncResponse sub, LocalDateTime localDateTime,int percentage) {
        this.concertId = concertId;
        this.sub = sub;
        this.localDateTime = localDateTime;
        this.percentage = percentage;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public AsyncResponse getSub() {
        return sub;
    }

    public void setSub(AsyncResponse sub) {
        this.sub = sub;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
