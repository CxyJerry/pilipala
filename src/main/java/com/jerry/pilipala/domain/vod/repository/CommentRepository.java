package com.jerry.pilipala.domain.vod.repository;

import com.jerry.pilipala.domain.vod.entity.neo4j.CommentEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends Neo4jRepository<CommentEntity, String> {
}
