package reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.lang.Long;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }
    public Reservation getReservationById(Long id){
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations(){

        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate){
        if (reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty");
        }
        if (reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }
        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id = " + id + " not found"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status = " + reservationEntity.getStatus());
        }
        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    public void deleteReservation(Long id) {
        if (!repository.existsById(id)){
            throw new EntityNotFoundException("Reservation with id = " + id + " not found");
        }
        repository.deleteById(id);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id = " + id + " not found"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation with status: " + reservationEntity.getStatus());
        }
        boolean isConflicting = isAlreadyReserved(reservationEntity);
        if (isConflicting){
            throw new IllegalStateException("Cannot approve reservation, date confliction");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }

    private boolean isAlreadyReserved(ReservationEntity reservationToCheck){
        var allReservations = repository.findAll();
        for (var reservation : allReservations){
            if(reservationToCheck.getId().equals(reservation.getId())) continue;;

            if (!reservationToCheck.getRoomId().equals(reservation.getRoomId())) continue;

            if (!reservation.getStatus().equals(ReservationStatus.APPROVED)) continue;

            if (reservation.getStartDate().isBefore((reservationToCheck.getEndDate()))
                && reservationToCheck.getStartDate().isBefore(reservation.getEndDate())){
                return true;
            }
        }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation){
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }
}
