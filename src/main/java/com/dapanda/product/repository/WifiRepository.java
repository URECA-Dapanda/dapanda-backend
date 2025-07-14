package com.dapanda.product.repository;

import com.dapanda.product.entity.Wifi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WifiRepository extends JpaRepository<Wifi, Long> {

}
