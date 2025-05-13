package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.ActionStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {


//    List<TruckEntity> findTop10ByIsSentToCloud(boolean isSent);
    @Query(value = "SELECT * FROM trucks WHERE is_sent_to_cloud = :isSent ", nativeQuery = true)
    List<TruckEntity> findTop10ByIsSentToCloud(@Param("isSent") boolean isSent);


    List<TruckEntity> findByIsSentToMyCoalAndIsFinished(boolean isSent, boolean isFinished);

    List<TruckEntity> findByIsSentToMyCoalAndIsFinishedAndIsDeleted(boolean isSent, boolean isFinished, boolean isNotDeleted);

    Optional<TruckEntity> findTopByOrderByIdDesc();

    @Query("SELECT t FROM trucks t " +
            "JOIN t.truckActions ta " +
            "WHERE t.truckNumber = :truckNumber " +
            "AND ta.action = 'ENTRANCE' " +
            "AND NOT EXISTS (" +
            "    SELECT ta2 FROM truck_actions ta2 " +
            "    WHERE ta2 MEMBER OF t.truckActions " +
            "    AND ta2.action IN (" +
            "uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction.EXIT, " +
            "uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction.MANUAL_EXIT)" +
            ")")
    Optional<TruckEntity> findTruckWithEntranceNoExit(@Param("truckNumber") String truckNumber);

    Optional<TruckEntity> findByTruckPhotosContains(TruckPhotosEntity truckPhotos);

    Optional<TruckEntity> findByTruckNumberAndIsFinished(String truckNumber, Boolean isFinished);

    List<TruckEntity> findByTruckNumberOrderByCreatedAtDesc(String truckNumber);

    List<TruckEntity> findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAt(String truckNumber, Boolean finished, Boolean deleted);

    boolean existsByTruckNumberAndNextEntranceTimeIsBeforeAndIsFinishedTrue(String truckNumber, LocalDateTime localDateTime);

    boolean existsByTruckNumberAndTruckActions_ActionAndIsFinishedFalse(String truckNumber, TruckAction action);

    List<TruckEntity> findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAtDesc(String truckNumber, boolean isFinished, boolean isDeleted);

    @Query("SELECT t FROM trucks t " +
            "JOIN t.truckActions ta " +
            "WHERE t.truckNumber = :truckNumber " +
            "AND t.isFinished = :isFinished " +
            "AND t.isDeleted = :isDeleted " +
            "AND ta.actionStatus = 'COMPLETE' " +
            "AND ta.action IN :truckActions " +
            "ORDER BY t.createdAt DESC")
    List<TruckEntity> findByTruckNumberAndActionStatus(
            String truckNumber,
            List<TruckAction> truckActions,
            Boolean isFinished,
            Boolean isDeleted);



    Optional<TruckEntity> findByTruckNumberAndIsFinished(String truckNumber, boolean isFinished);

    boolean existsByTruckNumberAndNextEntranceTimeIsBeforeAndIsFinishedFalse(String truckNumber, LocalDateTime localDateTime);

    boolean existsByTruckNumberAndIsDeleted(String truckNumber, boolean isDeleted);

    boolean existsByIsFinishedFalseAndIsDeletedFalse();

    boolean existsByTruckNumberAndIsFinishedAndIsDeleted(String truckNumber, boolean isFinished, boolean isDeleted);

    List<TruckEntity> findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<TruckEntity> findByIsFinishedAndIsDeleted(Boolean isFinished, boolean deleted);

    List<TruckEntity> findAllByIsDeleted(boolean isDeleted, Sort sort);

    @Query("SELECT ta.action, COUNT(t.truckNumber), CAST(ta.createdAt AS date) " +
            "FROM trucks t JOIN t.truckActions ta " +
            "WHERE CAST(ta.createdAt AS date) BETWEEN :startDate AND :endDate " +
            "GROUP BY ta.action, CAST(ta.createdAt AS date)")
    List<Object[]> findTruckCountsByDate(LocalDate startDate, LocalDate endDate);


    @Query("""
    SELECT ta.action, COUNT(ta)
    FROM truck_actions ta
    WHERE ta.actionStatus = :status AND ta.createdAt BETWEEN :fromDate AND :toDate
    GROUP BY ta.action
""")
    List<Object[]> findTruckCountsByDateAndStatus(@Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate,
                                                  @Param("status") ActionStatus status);


}
