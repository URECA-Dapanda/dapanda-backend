package com.dapanda.product.repository;

import com.dapanda.product.entity.MobileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileDataRepository extends JpaRepository<MobileData, Long> {

}
