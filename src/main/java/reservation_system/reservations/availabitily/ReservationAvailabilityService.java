package reservation_system.reservations.availabitily;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reservation_system.reservations.ReservationRepository;
import reservation_system.reservations.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {

    private static Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository;

    public ReservationAvailabilityService(ReservationRepository repository){
        this.repository = repository;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ){
        if (!endDate.isAfter(startDate)){
            throw new IllegalStateException("Start date must be at least 1 day earlier than end date");
        }
        List<Long> conflictingIds = repository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );
        if (conflictingIds.isEmpty()){
            return true;
        }
        log.info("Conflicting with IDs = {}", conflictingIds);
        return  false;
    }

}
