package com.UNI_FYP.AI_Based_Road_Construction.repository;

import com.UNI_FYP.AI_Based_Road_Construction.entity.WorkRequestRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkRequestRatingRepository extends JpaRepository<WorkRequestRating, Long> {

    List<WorkRequestRating> findAllByOrderByCreatedAtDesc();

    boolean existsByWorkRequestIdAndRatedById(Long workRequestId, Long ratedById);

    long countByWorkRequestId(Long workRequestId);

    @Query("SELECT AVG(r.score) FROM WorkRequestRating r WHERE r.workRequest.id = :workRequestId")
    Double findAverageScore(@Param("workRequestId") Long workRequestId);
}