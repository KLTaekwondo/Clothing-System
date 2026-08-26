package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e " +
            "JOIN fetch e.wareHouse w " +
            "WHERE w.wareHouseId = :wareHouseId AND e.status = :status")
    List<Employee> findByWareHouseId(@Param("wareHouseId") Long wareHouseId,
                                     @Param("status") StatusEnum status);

    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.employeeCode = :employeeCode")
    boolean existsByCode(@Param("employeeCode") String employeeCode);
}
