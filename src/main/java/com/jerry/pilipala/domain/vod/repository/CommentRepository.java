package com.jerry.pilipala.domain.vod.repository;

import com.jerry.pilipala.domain.vod.entity.neo4j.CommentEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentRepository extends Neo4jRepository<CommentEntity, Long> {
    @Query("MATCH (c:`vod-comment`)-[:ReplyFor*1..]->(reply:`vod-comment`) " +
            "WHERE c.id IN $commentIds " +
            "WITH c, COLLECT(DISTINCT reply) AS allReplies " +
            "WITH c, REDUCE(s = 0, reply IN allReplies | s + CASE WHEN reply.parentComment.id = c.id THEN 1 ELSE 0 END) AS replyCount " +
            "RETURN c.id AS commentId, replyCount")
    List<Map<Long, Integer>> getReplyCounts(@Param("commentIds") List<Long> commentIds);



    @Query("MATCH (c:`vod-comment`) WHERE c.parentCommentId is NULL AND c.cid = $cid RETURN c SKIP $skip LIMIT $limit")
    List<CommentEntity> getTopLevelCommentsByCid(Long cid, int skip, int limit);


    @Query("MATCH (c:`vod-comment`) WHERE exists(c.parentCommentId) AND c.parentCommentId = $parentCommentId AND c.cid = $cid RETURN c SKIP $skip LIMIT $limit")
    List<CommentEntity> getSecondLevelCommentsByParentCommentIdAndCid(Long parentCommentId, Long cid, int skip, int limit);
}