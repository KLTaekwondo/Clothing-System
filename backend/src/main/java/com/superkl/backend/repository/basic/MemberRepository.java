package com.superkl.backend.repository.basic;

import com.superkl.backend.common.extralreturn.MemberCounts;
import com.superkl.backend.entity.basic.Member;
import com.superkl.backend.enums.MemberLevelEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.memberPhone = :phone")
    Optional<Member> findByPhone(@Param("phone") String phone);

    @Query(value = "select * from t_member m where member_phone = :phone for update",nativeQuery = true)
    Optional<Member> lockByPhoneForUpdate(@Param("phone") String phone);

    @Query("select count(m)>0 from Member m where m.memberPhone = :phone")
    boolean existsByPhone(@Param("phone") String phone);

    @Query("SELECT m FROM Member m " +
            "WHERE (:keyword IS NULL OR (m.memberPhone LIKE :keyword OR m.memberName LIKE :keyword)) " +
            "AND (:memberLevel IS NULL OR m.memberLevel = :memberLevel) " +
            "ORDER BY m.memberId DESC")
    Page<Member> findPage(@Param("keyword") String keyword,
                          @Param("memberLevel") MemberLevelEnum memberLevel,
                          Pageable pageable);

    @Query(value = "SELECT member_level AS memberLevel,COUNT(*) AS mount FROM t_member GROUP BY member_level" ,nativeQuery = true)
    List<MemberCounts> countByMemberLevel();
}
