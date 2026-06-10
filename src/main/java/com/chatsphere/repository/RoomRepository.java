package com.chatsphere.repository;

import com.chatsphere.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByType(String type);
    boolean    existsByName(String name);

    // Groups where username appears in member_ids column
    @Query(value = "SELECT * FROM rooms WHERE type = 'group' AND FIND_IN_SET(:username, member_ids) > 0",
           nativeQuery = true)
    List<Room> findGroupsByMemberUsername(@Param("username") String username);

    // DM between two users
    @Query(value = "SELECT * FROM rooms WHERE type = 'dm' AND FIND_IN_SET(:u1, member_ids) > 0 AND FIND_IN_SET(:u2, member_ids) > 0",
           nativeQuery = true)
    java.util.Optional<Room> findDMByUsers(@Param("u1") String u1, @Param("u2") String u2);
}