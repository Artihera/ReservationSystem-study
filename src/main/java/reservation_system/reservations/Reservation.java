package reservation_system.reservations;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.lang.Long;

public record Reservation (
        @Null
        Long id,
        @NotNull
        Long userId,
        @NotNull
        Long roomId,
        @FutureOrPresent
        @NotNull
        LocalDate startDate,
        @FutureOrPresent
        @NotNull
        LocalDate endDate,
        ReservationStatus status
){}
