package reservation_system;

import java.time.LocalDate;

public record Reservation (
    long id,
    long userId,
    long roomId,
    LocalDate startDate,
    LocalDate endDate,
    ReservationStatus status
){}
