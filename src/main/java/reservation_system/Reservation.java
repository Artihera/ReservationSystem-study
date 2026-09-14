package reservation_system;

import java.time.LocalDate;
import java.lang.Long;

public record Reservation (
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,
    ReservationStatus status
){}
