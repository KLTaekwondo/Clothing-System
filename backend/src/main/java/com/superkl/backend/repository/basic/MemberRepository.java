package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("select m from Member m where m.memberPhone = :phone")
    Optional<Member> findMemberByPhone(@Param("phone") String phone);

    @Query("select count(m)>0 from Member m where m.memberPhone = :phone")
    boolean existsMemberByPhone(@Param("phone") String phone);
}
