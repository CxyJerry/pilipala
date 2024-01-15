package com.jerry.pilipala.domain.vod.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jerry.pilipala.application.dto.PreUploadDTO;
import com.jerry.pilipala.application.dto.VideoPostDTO;
import com.jerry.pilipala.application.vo.bvod.BVodVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.application.vo.vod.InteractionInfoVO;
import com.jerry.pilipala.application.vo.vod.PreUploadVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatistics;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.VodThumbnails;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.service.media.profiles.Profile;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public interface VodService {


    void ffprobe(String filepath);

    /**
     * 生成全局唯一的 filename
     *
     * @param preUploadDTO 预上传稿件素材信息
     * @return filename
     */
    String genFilename(PreUploadDTO preUploadDTO);

    /**
     * 预上传稿件
     *
     * @param preUploadDTO 稿件素材信息
     * @return 预上传结果
     */
    PreUploadVO preUpload(PreUploadDTO preUploadDTO);


    /**
     * 传递稿件
     *
     * @param videoPostDTO 稿件信息
     */
    void post(VideoPostDTO videoPostDTO);

    /**
     * 规划需要转出的视频规格
     *
     * @param cid 稿件唯一ID
     * @param vod 稿件素材信息
     * @return 视频规格列表
     * @throws JsonProcessingException 打印视频规格信息时可能产生的序列化异常
     */
    List<Profile> schema(Long cid, Vod vod) throws JsonProcessingException;

    /**
     * 视频转码
     *
     * @param cid 稿件唯一ID
     * @throws ExecutionException      ffmpeg 执行可能出现的异常
     * @throws InterruptedException    ffmpeg 执行可能出现的异常
     * @throws JsonProcessingException 打印视频规格信息时可能产生的序列化异常
     */
    void transcode(Long cid) throws ExecutionException, InterruptedException, JsonProcessingException;

    /**
     * 视频预览缩略图生产
     *
     * @param cid 稿件唯一ID
     */
    void transcodeThumbnails(Long cid);

    /**
     * 转码任务重置
     *
     * @param taskId 任务ID
     */
    void reset(String taskId);

    /**
     * 获取视频文件
     *
     * @param cid    稿件唯一ID
     * @param format 稿件清晰度名称
     * @param name   文件名
     * @return
     */
    ResponseEntity<InputStreamResource> video(Long cid, String format, String name);

    void review(Long cid, String status);

    Page<VodVO> page(String uid, Integer pageNo, Integer pageSize, String type);

    BVodVO videos(String bvid, Long cid);

    Map<Long, VodStatistics> batchQueryVodStatistics(Collection<Object> cidCollection);

    void updatePlayTime(String bvId, Long cid, Integer time);


    public List<PreviewBVodVO> buildPreviewBVodList(List<VodInfo> vodInfoList);


    VodThumbnails thumbnails(Long cid);

    Page<VodVO> reviewPage(Integer pageNo, Integer pageSize, String status);

    InteractionInfoVO interactionInfo(Long cid);

    BVodVO sdVideo(Long cid);

    List<VodVO> batchBuildVodVOWithoutQuality(List<VodInfo> vodInfos, boolean needStatistics);
}
