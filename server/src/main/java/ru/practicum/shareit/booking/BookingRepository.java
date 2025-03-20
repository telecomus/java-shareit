package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findCurrentBookingsByBookerId(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1")
    List<Booking> findByOwnerId(Long ownerId, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findCurrentBookingsByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2")
    List<Booking> findPastBookingsByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start > ?2")
    List<Booking> findFutureBookingsByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start < ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.end desc")
    List<Booking> findLastBookingForItem(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start > ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.start asc")
    List<Booking> findNextBookingForItem(Long itemId, LocalDateTime now);

    @Query("select count(b) > 0 from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.end < ?3 " +
            "and b.status = 'APPROVED'")
    boolean hasUserBookedItem(Long itemId, Long userId, LocalDateTime now);
}