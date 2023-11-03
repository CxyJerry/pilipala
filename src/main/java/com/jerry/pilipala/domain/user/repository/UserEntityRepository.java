package com.jerry.pilipala.domain.user.repository;

import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface UserEntityRepository extends Neo4jRepository<UserEntity, String> {
}
