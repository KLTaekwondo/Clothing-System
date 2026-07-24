package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    @Query("select a from Admin a where a.username = :account or a.adminCode = :account")
    Optional<Admin> findByAccount(String account);
}
