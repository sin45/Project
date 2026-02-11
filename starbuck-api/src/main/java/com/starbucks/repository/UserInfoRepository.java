package com.starbucks.repository;

import com.starbucks.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByUsername(String username);
    Optional<UserInfo> findByUserPhone(String phone);
    Optional<UserInfo> findByWxOpenid(String openid);
}