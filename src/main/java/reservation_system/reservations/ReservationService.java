package reservation_system.reservations;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reservation_system.reservations.availabitily.ReservationAvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.lang.Long;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationAvailabilityService availabilityService;
    private final ReservationMapper mapper;

    public ReservationService(
            ReservationRepository repository,
            ReservationAvailabilityService availabilityService,
            ReservationMapper mapper)
    {
        this.repository = repository;
        this.availabilityService = availabilityService;
        this.mapper = mapper;
    }

    public Reservation getReservationById(Long id){
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ){
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10; //hardcode temp
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0; //hardcode temp

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate){
        if (reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty");
        }
        if (reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalStateException("Start date must be at least 1 day earlier than end date");
        }
        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id = " + id + " not found"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status = " + reservationEntity.getStatus());
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalStateException("Start date must be at least 1 day earlier than end date");
        }
        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }
    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id " + id));
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Cannot cancel approved reservation. Contact manager");
        }
        if (reservation.getStatus().equals(ReservationStatus.CANCELED)){
            throw new IllegalStateException("Cannot cancel reservation. It is already canceled");
        }
        log.info("Reservation with id = {} got canceled",id);
        repository.setStatus(id, ReservationStatus.CANCELED);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id = " + id + " not found"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation with status: " + reservationEntity.getStatus());
        }
        boolean isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );
        if (!isAvailableToApprove){
            throw new IllegalStateException("Cannot approve reservation, date confliction");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

}
