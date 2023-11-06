package com.jerry.pilipala.domain.user.repository;

import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserEntityRepository extends Neo4jRepository<UserEntity, String> {
    UserEntity findUserEntityByTel(String tel);

    /**
     * 获取粉丝数据
     *
     * @param uid 用户ID
     * @return list
     */
    @Query("MATCH (follower:`user`)-[:Followed]->(user:`user` {uid: $uid}) RETURN follower")
    List<UserEntity> findFollowersByUserId(String uid);

    /**
     * 获取粉丝数据
     *
     * @param uid 用户ID
     * @return list
     */
    @Query("MATCH (follower:`user`)-[:Followed]->(user:`user` {uid: $uid}) RETURN follower SKIP $skip LIMIT $limit")
    List<UserEntity> findFollowersByUserId(String uid, int skip, int limit);

    /**
     * 获取粉丝数量
     *
     * @param uid 用户ID
     * @return count
     */
    @Query("MATCH (follower:`user`)-[:Followed]->(user:`user` {uid: $uid}) RETURN COUNT(follower)")
    int countFollowersByUserId(String uid);


    /**
     * 获取我关注的 up
     *
     * @param userId 用户ID
     * @return up
     */
    @Query("MATCH (user:`user` {uid: $userId})-[:Followed]->(followed:`user`) RETURN followed SKIP $skip LIMIT $limit")
    List<UserEntity> getFollowedUsers(@Param("userId") String userId, @Param(("skip")) int skip, @Param("limit") int limit);

    /**
     * 获取我关注的 up 数量
     *
     * @param userId 用户ID
     * @return count
     */
    @Query("MATCH (user:`user` {uid: $userId})-[:Followed]->(followed:`user`) RETURN COUNT(followed)")
    int getFollowedUsersCount(@Param("userId") String userId);
}
