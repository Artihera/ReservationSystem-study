package reservation_system.reservations.availabitily;

public record CheckAvailabilityResponse (
        String message,
        AvailabilityStatus status
){
}
