package reservation_system.reservations.availabitily;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service;

    public ReservationAvailabilityController(
            ReservationAvailabilityService service
    ){
        this.service = service;
    }
    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailatility(
            @Valid CheckAvailabilityRequest request
    ){
        log.info("Called method checkAvailatility: request = {}", request);
        boolean isReservationAvailable = service.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );
        var message = isReservationAvailable ?
                "Room is available to reservation"
                : "Room is not availbe to reservation";
        var status = isReservationAvailable ?
                AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message, status));
    }
}
