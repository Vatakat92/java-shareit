package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdOrderByStartDateDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
            Long bookerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndEndDateBeforeOrderByStartDateDesc(
            Long bookerId, LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStartDateAfterOrderByStartDateDesc(
            Long bookerId, LocalDateTime startDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStatusOrderByStartDateDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdOrderByStartDateDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
            Long ownerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(
            Long ownerId, LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(
            Long ownerId, LocalDateTime startDate, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDateDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    // Методы для ItemServiceImpl
    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.startDate < :now ORDER BY b.endDate DESC")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.startDate > :now ORDER BY b.startDate ASC")
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker " +
            "WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.endDate < :endDate AND b.status = :status")
    Optional<Booking> findCompletedBookingForItemAndBooker(
            @Param("itemId") Long itemId, @Param("bookerId") Long bookerId,
            @Param("endDate") LocalDateTime endDate, @Param("status") BookingStatus status);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.startDate < :now ORDER BY b.endDate DESC")
    Optional<Booking> findLastApprovedBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.startDate > :now ORDER BY b.startDate ASC")
    Optional<Booking> findNextApprovedBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"item", "booker"})
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.endDate <= :currentTime AND b.status = 'APPROVED'")
    Optional<Booking> findPastApprovedForComment(
            @Param("itemId") Long itemId, @Param("bookerId") Long bookerId,
            @Param("currentTime") LocalDateTime currentTime);
}