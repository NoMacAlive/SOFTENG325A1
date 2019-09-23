package se325.assignment01.concert.service.domain;

import se325.assignment01.concert.service.util.TheatreLayout;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "SEATS")
public class Seat {
	@Id
	@GeneratedValue
	private long id;
	private String label;
	private boolean isBooked;
    private LocalDateTime date;
    private BigDecimal price;

    @Version
	private long version;
//    private TheatreLayout.PriceBand priceBand;

	public Seat() {}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public long getId() {
		return this.id;
	}

//	public TheatreLayout.PriceBand getPriceBand() {
//		return priceBand;
//	}
//
//	public void setPriceBand(TheatreLayout.PriceBand priceBand) {
//		this.priceBand = priceBand;
//	}
}
