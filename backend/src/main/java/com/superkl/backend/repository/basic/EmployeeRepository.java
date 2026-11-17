package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e " +
            "JOIN fetch e.wareHouse w " +
            "WHERE w.wareHouseId = :wareHouseId AND e.status = :status")
    List<Employee> listByWareHouseAndStatus(@Param("wareHouseId") Long wareHouseId,
                                     @Param("status") StatusEnum status);

    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.employeeCode = :employeeCode")
    boolean existsByCode(@Param("employeeCode") String employeeCode);

    @Query("SELECT e FROM Employee e " +
            "WHERE (:keyword IS NULL OR e.employeeCode LIKE :keyword OR e.employeeName LIKE :keyword) " +
            "ORDER BY e.employeeId DESC")
    Page<Employee> pageByCodeOrName(@Param("keyword") String keyword,
                                    Pageable pageable);
}
