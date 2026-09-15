package reservation_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id
            ){
        log.info("Called method getReservationById: id = {}", id);
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservationService.getReservationById(id));
        } catch (NoSuchElementException exeption){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(){
        log.info("Called method getAllReservations");
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody Reservation reservationToCreate
    ){
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                //.header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
        @PathVariable("id") Long id,
        @RequestBody Reservation reservationToUpdate
    ){
        log.info("Called method updateReservation, id = {}, reservationToUpdate = {}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.status(HttpStatus.OK)
                .body(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called method cancelReservation, id= {}", id);
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (NoSuchElementException exeption){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called method approveReservation: id = {}", id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservation);
    }
}
