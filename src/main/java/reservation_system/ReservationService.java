package reservation_system;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.lang.Long;

@Service
public class ReservationService {
    private final Map<Long, Reservation> reservationMap = Map.of(
        1L, new Reservation(
                    1L,
                    100L,
                    40L,
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.APPROVED
            ),
        2L, new Reservation(
                    2L,
                    110L,
                    42L,
                    LocalDate.now(),
                    LocalDate.now().plusDays(3),
                    ReservationStatus.PENDING
            ),
            3L, new Reservation(
                    3L,
                    108L,
                    39L,
                    LocalDate.now().plusDays(6),
                    LocalDate.now().plusDays(10),
                    ReservationStatus.APPROVED
            )
    );

    public Reservation GetReservationByID(Long id){
        if (!reservationMap.containsKey(id)){
            try {
                throw new NoSuchFieldException("Reservation id " + id + " is not found");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return reservationMap.get(id);

    }

    public List<Reservation> FindAllReservations(){
        return reservationMap.values().stream().toList();
    }
}
