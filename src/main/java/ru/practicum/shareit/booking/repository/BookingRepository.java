package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    @Query(value = "SELECT * FROM bookings b WHERE b.item_id = :itemId AND b.booker_id = :bookerId AND b.end_date < :endDate AND b.status = CAST(:status AS VARCHAR) LIMIT 1", nativeQuery = true)
    Optional<Booking> findByItemIdAndBookerIdAndEndBeforeAndStatus(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("endDate") LocalDateTime end,
            @Param("status") BookingStatus status);

    @Query(value = "SELECT * FROM bookings b WHERE b.item_id = :itemId AND b.booker_id = :bookerId AND b.end_date <= :currentTime AND b.status = 'APPROVED' LIMIT 1", nativeQuery = true)
    Optional<Booking> findPastApprovedForComment(@Param("itemId") Long itemId,
                                                 @Param("bookerId") Long bookerId,
                                                 @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByOwnerId(Long ownerId, Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b JOIN items i ON b.item_id = i.id WHERE i.owner_id = :ownerId AND b.start_date < :now AND b.end_date > :now AND b.status = 'APPROVED' ORDER BY b.start_date DESC", nativeQuery = true)
    List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b JOIN items i ON b.item_id = i.id WHERE i.owner_id = :ownerId AND b.end_date < :currentTime AND b.status = 'APPROVED' ORDER BY b.start_date DESC", nativeQuery = true)
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId, @Param("currentTime") LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
            "AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
            "AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query(value = "SELECT * FROM bookings b WHERE b.item_id = :itemId AND b.status = 'APPROVED' AND b.start_date < :now ORDER BY b.end_date DESC LIMIT 1", nativeQuery = true)
    Optional<Booking> findLastBooking(Long itemId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.start > :now ORDER BY b.start ASC")
    Optional<Booking> findNextBooking(Long itemId, LocalDateTime now);
}