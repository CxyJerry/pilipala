package com.jerry.pilipala.domain.vod.repository;

import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VodInfoRepository extends Neo4jRepository<VodInfoEntity, Long> {

    @Query("MATCH (vod:`vod-info`) " +
            "WHERE vod.partition = $partition " +
            "RETURN vod " +
            "ORDER BY vod.viewCount + vod.likeCount + vod.barrageCount + vod.commentCount + vod.coinCount + vod.collectCount + vod.shareCount DESC " +
            "SKIP $skip LIMIT $limit")
    List<VodInfoEntity> recommendVideosByContentBasedFiltering(String partition, int skip, int limit);


    @Query("MATCH (vod:`vod-info`) " +
            "WHERE vod.partition = $partition " +
            "RETURN COUNT(vod)")
    Long countVideosByPartition(String partition);

    @Query("MATCH (u:`user` {uid: $userId})-[:Followed]->(followed:`user`)-[:CreatedBy]->(vod:`vod-info`) " +
            "RETURN vod " +
            "ORDER BY vod.viewCount + vod.likeCount + vod.barrageCount + vod.commentCount + vod.coinCount + vod.collectCount + vod.shareCount DESC " +
            "LIMIT $limit")
    List<VodInfoEntity> recommendVideosByUserId(String userId, int limit);


    @Query("MATCH (vod:`vod-info`) " +
            "RETURN vod " +
            "ORDER BY vod.viewCount + vod.likeCount + vod.barrageCount + vod.commentCount + vod.coinCount + vod.collectCount + vod.shareCount DESC " +
            "LIMIT $limit")
    List<VodInfoEntity> recommendVideosByContentBasedFiltering(int limit);

    @Query("MATCH (vod:`vod-info`)-[:CreatedBy]->(user:`user`) " +
            "WHERE vod.partition = $partition " +
            "WITH vod, " +
            "  apoc.text.similarity.jaccardWeighted(vod.title, $searchQuery, 1.0, 0.5) AS titleScore, " +
            "  apoc.text.similarity.jaccardWeighted(vod.desc, $searchQuery, 1.0, 0.5) AS descScore, " +
            "  apoc.text.similarity.jaccardWeighted(vod.partition, $searchQuery, 1.0, 0.5) AS partitionScore, " +
            "  apoc.text.similarity.jaccardWeighted(apoc.coll.flatten(vod.labels), $searchQuery, 1.0, 0.5) AS labelScore " +
            "WITH vod, titleScore + descScore + partitionScore + labelScore AS totalScore " +
            "ORDER BY totalScore DESC " +
            "SKIP $skip LIMIT $limit " +
            "RETURN vod")
    List<VodInfoEntity> searchVodByContentBasedFiltering(String partition, String searchQuery, int skip, int limit);


    VodInfoEntity findByCid(Long cid);
}
