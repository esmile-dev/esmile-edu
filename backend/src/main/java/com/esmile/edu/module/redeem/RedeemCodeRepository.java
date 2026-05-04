package com.esmile.edu.module.redeem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RedeemCodeRepository extends JpaRepository<RedeemCodeEntity, Long> {
    Optional<RedeemCodeEntity> findByCode(String code);
    List<RedeemCodeEntity> findByCourseId(Long courseId);
    List<RedeemCodeEntity> findByCreatedBy(Long createdBy);
    Page<RedeemCodeEntity> findByCreatedBy(Long createdBy, Pageable pageable);
}
