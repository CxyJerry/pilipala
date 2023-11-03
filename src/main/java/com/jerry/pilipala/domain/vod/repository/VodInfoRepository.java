package com.jerry.pilipala.domain.vod.repository;

import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VodInfoRepository extends Neo4jRepository<VodInfoEntity, Long> {
}
