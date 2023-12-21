package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jerry.pilipala.application.bo.UserInfoBO;
import com.jerry.pilipala.application.dto.PreUploadDTO;
import com.jerry.pilipala.application.dto.VideoPostDTO;
import com.jerry.pilipala.application.vo.bvod.BVodVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.application.vo.vod.*;
import com.jerry.pilipala.domain.common.template.MessageTrigger;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.vod.entity.mongo.distribute.Quality;
import com.jerry.pilipala.domain.vod.entity.mongo.distribute.VodDistributeInfo;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionRecord;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatistics;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.Thumbnails;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.VodThumbnails;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.BVod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodProfiles;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.domain.vod.service.FileService;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.media.UGCSchema;
import com.jerry.pilipala.domain.vod.service.media.encoder.Encoder;
import com.jerry.pilipala.domain.vod.service.media.profiles.Profile;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import com.jerry.pilipala.infrastructure.enums.VodStatusEnum;
import com.jerry.pilipala.infrastructure.enums.message.TemplateNameEnum;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.SecurityTool;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VodServiceImpl implements VodService {
    private final FFprobe fFprobe;
    private final FFmpeg fFmpeg;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Snowflake snowflake = IdUtil.getSnowflake();

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UGCSchema ugcSchema;
    private final TaskExecutor taskExecutor;
    private final VodInfoRepository vodInfoRepository;

    private final UserEntityRepository userEntityRepository;
    private final JsonHelper jsonHelper;
    private final FileService fileService;
    private final MessageTrigger messageTrigger;

    public VodServiceImpl(MongoTemplate mongoTemplate,
                          RedisTemplate<String, Object> redisTemplate,
                          ApplicationEventPublisher applicationEventPublisher,
                          UGCSchema ugcSchema,
                          @Qualifier("asyncServiceExecutor") TaskExecutor taskExecutor,
                          VodInfoRepository vodInfoRepository,
                          UserEntityRepository userEntityRepository,
                          MessageService messageService,
                          JsonHelper jsonHelper,
                          FileService fileService,
                          MessageTrigger messageTrigger1) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.ugcSchema = ugcSchema;
        this.taskExecutor = taskExecutor;
        this.vodInfoRepository = vodInfoRepository;
        this.userEntityRepository = userEntityRepository;
        this.jsonHelper = jsonHelper;
        this.fileService = fileService;
        this.messageTrigger = messageTrigger1;
    }

    {
        try {
            fFprobe = new FFprobe();
            fFmpeg = new FFmpeg();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ffprobe(String filepath) {
        try {
            FFmpegProbeResult probeResult = fFprobe.probe(filepath);
            FFmpegFormat format = probeResult.getFormat();
            System.out.println(probeResult.getStreams());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成全局唯一的 filename
     *
     * @param preUploadDTO 预上传稿件素材信息
     * @return filename
     */
    @Override
    public String genFilename(PreUploadDTO preUploadDTO) {
        try {
            String json = jsonHelper.as(preUploadDTO);
            String now = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            String md5 = SecurityTool.getMd5(json);
            return "f%s%s".formatted(now, md5);
        } catch (Exception e) {
            throw new BusinessException("文件名生成失败", StandardResponse.ERROR);
        }
    }

    /**
     * 预上传稿件
     *
     * @param preUploadDTO 稿件素材信息
     * @return 预上传结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class, value = "multiTransactionManager")
    public PreUploadVO preUpload(PreUploadDTO preUploadDTO) {
        String bvId = preUploadDTO.getBvId();
        String filename = genFilename(preUploadDTO);
        Long cid = snowflake.nextId();
        // 触发初始化开始
        applicationEventPublisher.publishEvent(new VodHandleActionEvent(VodHandleActionEnum.PRE_UPLOAD, ActionStatusEnum.init, cid));
        BVod bVod;
        if (StringUtils.isNotBlank(bvId)) {
            Query query = new Query(Criteria.where("_id").is(bvId));
            bVod = mongoTemplate.findOne(query, BVod.class);
            if (Objects.isNull(bVod)) {
                throw new BusinessException("bvid 不存在", StandardResponse.ERROR);
            }
        } else {
            bvId = "BV" + UUID.randomUUID().toString().replace("-", "").toUpperCase();

            bVod = new BVod()
                    .setBvId(bvId)
                    .setUid((String) StpUtil.getLoginId())
                    .setCidList(new ArrayList<>());
        }
        mongoTemplate.save(bVod);
        Vod vod = new Vod()
                .setCid(cid)
                .setBvId(bvId)
                .setFilename(filename)
                .setContainer(preUploadDTO.getContainer())
                .setVideo(preUploadDTO.getVideo())
                .setAudio(preUploadDTO.getAudio())
                .setExtra(preUploadDTO.getExtra());
        mongoTemplate.save(vod);

        // 触发初始化结束
        applicationEventPublisher.publishEvent(new VodHandleActionEvent(VodHandleActionEnum.PRE_UPLOAD, ActionStatusEnum.finished, cid));
        return new PreUploadVO().setBvId(bvId).setCid(cid).setFilename(filename);
    }

    /**
     * 投递稿件
     *
     * @param videoPostDTO 稿件信息
     */
    public void post(VideoPostDTO videoPostDTO) {
        Query query = new Query(Criteria.where("_id").is(videoPostDTO.getBvId()));
        BVod bVod = mongoTemplate.findOne(query, BVod.class);
        if (Objects.isNull(bVod)) {
            throw new BusinessException("稿件不存在，请重新投稿", StandardResponse.ERROR);
        }
        bVod.getCidList().add(videoPostDTO.getCid());
        bVod.setReady(true);
        mongoTemplate.save(bVod);

        Vod vod = mongoTemplate.findById(videoPostDTO.getCid(), Vod.class);
        if (Objects.isNull(vod)) {
            throw BusinessException.businessError("稿件不存在");
        }

        VodInfo vodInfo = new VodInfo();
        vodInfo.setBvId(videoPostDTO.getBvId())
                .setCid(videoPostDTO.getCid())
                .setUid((String) StpUtil.getLoginId())
                .setStatus(VodStatusEnum.HANDING)
                .setCoverUrl(videoPostDTO.getCoverUrl())
                .setTitle(videoPostDTO.getTitle())
                .setGcType(videoPostDTO.getGcType())
                .setPartition(videoPostDTO.getPartition())
                .setSubPartition(videoPostDTO.getSubPartition())
                .setLabels(videoPostDTO.getLabels())
                .setDesc(videoPostDTO.getDesc())
                .setDuration(vod.getContainer().getDuration())
                .setMtime(System.currentTimeMillis());
        mongoTemplate.save(vodInfo);

        VodHandleActionEvent actionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.SUBMIT,
                ActionStatusEnum.finished,
                videoPostDTO.getCid()
        );
        // 触发提交结束
        applicationEventPublisher.publishEvent(actionEvent);

        // 推送站内信
        User author = mongoTemplate.findById(new ObjectId(bVod.getUid()), User.class);
        if (Objects.isNull(author)) {
            log.error("消息推送失败，稿件作者信息异常.");
            return;
        }
        Map<String, String> variables = new HashMap<>();
        variables.put("username", author.getNickname());
        variables.put("title", vodInfo.getTitle());
        variables.put("bvid", vodInfo.getBvId());
        variables.put("cid", vodInfo.getCid().toString());
        messageTrigger.triggerSystemMessage(
                TemplateNameEnum.POST_VOD_NOTIFY,
                bVod.getUid(),
                variables
        );
    }

    /**
     * 规划需要转出的视频规格
     *
     * @param cid 稿件唯一ID
     * @param vod 稿件素材信息
     * @return 视频规格列表
     * @throws JsonProcessingException 打印视频规格信息时可能产生的序列化异常
     */
    @Override
    public List<Profile> schema(Long cid, Vod vod) throws JsonProcessingException {
        Query query = new Query(Criteria.where("_id").is(cid));
        VodProfiles vodProfiles = mongoTemplate.findOne(query, VodProfiles.class);
        List<Profile> profiles;
        if (Objects.isNull(vodProfiles)) {
            profiles = ugcSchema.selectAvProfiles(vod);
            vodProfiles = new VodProfiles().setCid(cid).setProfiles(profiles).setCompleted(false);
            mongoTemplate.save(vodProfiles);
        } else {
            profiles = vodProfiles.getProfiles();
        }

        log.info("cid [{}] -> select profiles: {}", cid, jsonHelper.as(profiles));
        return profiles;
    }

    /**
     * 视频转码
     *
     * @param cid 稿件唯一ID
     * @throws ExecutionException      ffmpeg 执行可能出现的异常
     * @throws InterruptedException    ffmpeg 执行可能出现的异常
     * @throws JsonProcessingException 打印视频规格信息时可能产生的序列化异常
     */
    @Override
    public void transcode(Long cid) throws ExecutionException, InterruptedException, JsonProcessingException {
        Query query = new Query(Criteria.where("_id").is(cid));
        Vod vod = mongoTemplate.findOne(query, Vod.class);
        if (Objects.isNull(vod)) {
            throw new BusinessException("稿件不存在", StandardResponse.ERROR);
        }
        String originFilePath = fileService.downloadVideo(vod.getFilename(), vod.getExt());
        // 规划转码规格
        List<Profile> profiles = schema(cid, vod);
        CompletableFuture<?>[] tasks = new CompletableFuture[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
            tasks[i] = CompletableFuture.runAsync(() -> transcodeTask(profile, vod, originFilePath), taskExecutor);
        }
        CompletableFuture.allOf(tasks).get();
        // 转码结束，删除本地临时文件
        fileService.deleteVideoOfWorkSpace(vod.getFilename(), vod.getExt());
    }

    /**
     * 转出缩略图
     *
     * @param cid 稿件唯一ID
     */
    @Override
    public void transcodeThumbnails(Long cid) {
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    // 每两秒抽一帧
                    // ffmpeg -i f2023102822062151aed32d88f9984f5cb2f264e26c85b9.mp4 -vf "fps=0.5,scale=56:32" "thumbnails_%05d.png
                    Query query = new Query(Criteria.where("_id").is(cid));
                    Vod vod = mongoTemplate.findOne(query, Vod.class);
                    if (Objects.isNull(vod)) {
                        throw new BusinessException("稿件不存在", StandardResponse.ERROR);
                    }
                    String originVideoPath = fileService.downloadVideo(vod.getFilename(), vod.getExt());

                    String thumbnailsDirPath = fileService.generateThumbnailsDirPath(vod.getFilename());
                    String thumbnailsNamePattern = "%s/%s".formatted(thumbnailsDirPath, "%05d.png");

                    FFmpegOutputBuilder builder = fFmpeg.builder()
                            .setInput(originVideoPath)
                            .overrideOutputFiles(true)
                            .setVideoFilter("fps=0.5,scale=56:32")
                            .addOutput(thumbnailsNamePattern);

                    FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
                    executor.createJob(builder.done()).run();

                    Double realDuration = vod.getVideo().getDuration();
                    int duration = realDuration.intValue();
                    List<Thumbnails> thumbnails = new ArrayList<>();
                    for (int i = 0; i <= duration; i += 2) {
                        Thumbnails thumbnailsPng = new Thumbnails();
                        thumbnailsPng.setTime(i).setUrl("%s/%05d.png".formatted(fileService.filePathRemoveWorkspace(thumbnailsDirPath), i));
                        thumbnails.add(thumbnailsPng);
                    }

                    // 上传七牛
                    fileService.uploadDirToOss(thumbnailsDirPath);
                    fileService.deleteDirOfWorkSpace(thumbnailsDirPath);

                    VodThumbnails vodThumbnails = new VodThumbnails()
                            .setCid(cid)
                            .setThumbnails(thumbnails);
                    mongoTemplate.save(vodThumbnails);

                    log.info("cid: {},thumbnails generate completed.", cid);
                } catch (Exception e) {
                    log.error("缩略图转码失败，cause ", e);
                    log.error("缩略图转码失败");
                }
            }).get();
        } catch (Exception e) {
            log.error("缩略图转码任务失败，", e);
        }
    }


    /**
     * 视频转码任务
     *
     * @param profile        需要转出的视频规格
     * @param vod            稿件素材信息
     * @param originFilePath 原始素材文件地址
     */
    private void transcodeTask(Profile profile, Vod vod, String originFilePath) {
        Long cid = vod.getCid();
        // f2023101502380651aed32d88f9984f5cb2f264e26c85b9/16
        String saveTo = profile.getSaveTo();
        String outputDir = fileService.generateTranscodeResSaveToPath(saveTo);

        // 16.mpd
        String outputFilename = "%s.%s".formatted(
                profile.getEncoder().quality().getQn(),
                profile.getFormat().getExt()
        );
        String outputPath = "%s/%s".formatted(outputDir, outputFilename);

        Encoder encoder = profile.getEncoder();
        encoder.fitInput(vod);
        Resolution resolution = encoder.getResolution();

        // ffmpeg -i input.mp4 -c copy -f dash output.mpd
        FFmpegOutputBuilder builder = fFmpeg.builder()
                .setInput(originFilePath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setFormat(profile.getFormat().getValue())
                .setAudioCodec(encoder.getAudioCodec())
                .setAudioBitRate(encoder.getAudioBitrate())
                .setVideoCodec(encoder.getVideoCodec())
                .setVideoFrameRate(encoder.getFrameRate())
                .setVideoBitRate(encoder.getVideoBitrate())
                .setVideoResolution(resolution.getWidth(), resolution.getHeight())
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL);
        if (!profile.isEnableAudio()) {
            builder.disableAudio();
        }
        if (!profile.isEnableVideo()) {
            builder.disableVideo();
        }
        if (profile.getDuration() > 0) {
            long duration = Math.min(vod.getVideo().getDuration().longValue(), profile.getDuration());
            builder.setDuration(duration, TimeUnit.SECONDS);
        }

        FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
        executor.createJob(builder.done()).run();
        log.info("cid: {}, resolution: {}x{} transcode completed.", cid, resolution.getWidth(), resolution.getHeight());
        distribute(cid, vod.getFilename(), profile.getSaveTo(), encoder.quality(), profile.getFormat().getExt());
    }

    /**
     * 转码后的稿件分发
     *
     * @param cid      稿件唯一ID
     * @param filename 稿件文件名
     * @param saveTo   转码后文件存储目录
     * @param qn       清晰度代号
     * @param ext      转码后文件拓展名
     */
    private void distribute(Long cid, String filename, String saveTo, Qn qn, String ext) {
        Query queryDistributeInfo = new Query(Criteria.where("_id").is(cid));

        Update update = new Update()
                .set("filename", filename)
                .set("ready", false)
                .set("qualityMap." + qn.getQn() + ".qn", qn.getQn())
                .set("qualityMap." + qn.getQn() + ".saveTo", saveTo)
                .set("qualityMap." + qn.getQn() + ".ext", ext)
                .set("qualityMap." + qn.getQn() + ".type", "auto");

        mongoTemplate.upsert(queryDistributeInfo, update, VodDistributeInfo.class);

        log.info("cid: {} distribute format {} successfully.", cid, qn.getDescription());
    }

    /**
     * 转码任务重置
     *
     * @param taskId 任务ID
     */
    @Override
    public void reset(String taskId) {
        Query query = new Query(Criteria.where("_id").is(taskId));
        VodHandleActionRecord actionRecord = mongoTemplate.findOne(query, VodHandleActionRecord.class);
        if (Objects.isNull(actionRecord)) {
            throw new BusinessException("任务不存在", StandardResponse.ERROR);
        }
        applicationEventPublisher.publishEvent(new VodHandleActionEvent(VodHandleActionEnum.TRANSCODE, ActionStatusEnum.init, actionRecord.getCid()));
    }

    /**
     * 获取视频文件
     *
     * @param cid    稿件唯一ID
     * @param format 稿件清晰度名称
     * @param name   文件名
     * @return
     */
    @Override
    public ResponseEntity<InputStreamResource> video(Long cid, String format, String name) {
        String outputFilename;
        String uid = StpUtil.getLoginId("");

        VodDistributeInfo vodDistributeInfo = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(cid)), VodDistributeInfo.class);

        checkVideoStatus(cid, uid, vodDistributeInfo);

        Qn qn = Qn.valueOfFormat(format);
        Quality quality = vodDistributeInfo.getQualityMap().get(qn.getQn());

        return fileService.video(name, quality);
    }

    private void checkVideoStatus(Long cid, String uid, VodDistributeInfo vodDistributeInfo) {
        if (Objects.isNull(vodDistributeInfo)) {
            throw BusinessException.businessError("稿件未准备妥当");
        }
        boolean needCheckPermission = false;
        if (StringUtils.isNotBlank(uid)) {
            UserInfoBO userInfoBO = (UserInfoBO) StpUtil.getSession().get("user-info");
            List<String> permissionIdList = userInfoBO.getPermissionIdList();
            Permission permission = mongoTemplate.findOne(new Query(Criteria.where("value").is("review-vod")),
                    Permission.class);
            if (Objects.isNull(permission) || !permissionIdList.contains(permission.getId().toString())) {
                needCheckPermission = true;
            }
        } else {
            needCheckPermission = true;
        }
        if (needCheckPermission) {
            Criteria criteria = Criteria.where("_id").is(cid)
                    .and("status").is(VodStatusEnum.PASSED);

            VodInfo vodInfo = mongoTemplate.findOne(
                    new Query(criteria),
                    VodInfo.class);
            if (Objects.isNull(vodInfo)) {
                throw new BusinessException("稿件未开放", StandardResponse.ERROR);
            }

            if (!vodDistributeInfo.getReady()) {
                throw new BusinessException("稿件未准备妥当", StandardResponse.ERROR);
            }
        }
    }

    @Override
    public void review(Long cid, String status) {
        VodStatusEnum statusEnum = VodStatusEnum.parse(status);
        if (!Arrays.asList(VodStatusEnum.PASSED, VodStatusEnum.FAIL).contains(statusEnum)) {
            throw new BusinessException("审批状态异常", StandardResponse.ERROR);
        }
        Query query = new Query(Criteria.where("_id").is(cid));
        VodInfo vodInfo = mongoTemplate.findOne(query, VodInfo.class);
        if (Objects.isNull(vodInfo)) {
            throw new BusinessException("稿件不存在", StandardResponse.ERROR);
        }
        vodInfo.setStatus(statusEnum);
        mongoTemplate.save(vodInfo);

        User author = mongoTemplate.findById(new ObjectId(vodInfo.getUid()), User.class);
        if (Objects.isNull(author)) {
            throw new BusinessException("创作者信息异常", StandardResponse.ERROR);
        }

        if (Objects.isNull(vodInfo.getDuration())) {
            Vod vod = mongoTemplate.findById(vodInfo.getCid(), Vod.class);
            if (Objects.nonNull(vod)) {
                Double duration = vod.getContainer().getDuration();
                vodInfo.setDuration(duration);
                mongoTemplate.save(vodInfo);
            }
        }

        // 审批未通过，不需要推荐
        if (!statusEnum.equals(VodStatusEnum.PASSED)) {
            vodInfoRepository.deleteById(vodInfo.getCid());
            if (statusEnum.equals(VodStatusEnum.FAIL)) {
                // 推送站内信
                Map<String, String> variables = new HashMap<>();
                variables.put("user_name", author.getNickname());
                variables.put("user_id", author.getUid().toString());
                variables.put("title", vodInfo.getTitle());
                variables.put("bvid", vodInfo.getBvId());
                variables.put("cid", vodInfo.getCid().toString());
                messageTrigger.triggerSystemMessage(
                        TemplateNameEnum.REVIEW_DENIED_NOTIFY,
                        vodInfo.getUid(),
                        variables
                );
            }
            return;
        }


        UserEntity userEntity = userEntityRepository.findById(author.getUid().toString()).orElse(null);
        if (Objects.isNull(userEntity)) {
            userEntity = new UserEntity();
            userEntity.setUid(author.getUid().toString())
                    .setTel(author.getTel())
                    .setEmail(author.getEmail())
                    .setCtime(author.getCtime())
                    .setFollowUps(new ArrayList<>());
        }
        synchronized (this) {
            // 存入图数据库,准备推荐
            VodInfoEntity exists = vodInfoRepository.findByCid(cid);
            if (Objects.isNull(exists)) {
                VodInfoEntity vodInfoEntity = new VodInfoEntity().setCid(vodInfo.getCid())
                        .setBvId(vodInfo.getBvId())
                        .setAuthorId(author.getUid().toString())
                        .setAuthor(userEntity)
                        .setCoverUrl(vodInfo.getCoverUrl())
                        .setTitle(vodInfo.getTitle())
                        .setDesc(vodInfo.getDesc())
                        .setDuration(vodInfo.getDuration())
                        .setGcType(vodInfo.getGcType())
                        .setPartition(vodInfo.getPartition())
                        .setSubPartition(vodInfo.getSubPartition())
                        .setLabels(vodInfo.getLabels())
                        .setCtime(vodInfo.getCtime());
                vodInfoRepository.save(vodInfoEntity);
            }
        }

        // 推送站内信
        Map<String, String> variables = new HashMap<>();
        variables.put("user_name", author.getNickname());
        variables.put("user_id", author.getUid().toString());
        variables.put("title", vodInfo.getTitle());
        variables.put("bvid", vodInfo.getBvId());
        variables.put("cid", vodInfo.getCid().toString());
        messageTrigger.triggerSystemMessage(
                TemplateNameEnum.REVIEW_PASSED_NOTIFY,
                vodInfo.getUid(),
                variables
        );
    }

    @Override
    public Page<VodVO> page(String uid, Integer pageNo, Integer pageSize, String status) {
        Page<VodVO> page = new Page<VodVO>().setPageNo(pageNo).setPageSize(pageSize);
        if (StringUtils.isBlank(uid)) {
            uid = (String) StpUtil.getLoginId();
        }

        pageNo = Math.max(pageNo - 1, 0);

        Criteria criteria = Criteria.where("uid").is(uid);
        if (StringUtils.isNotBlank(status)) {
            VodStatusEnum vodStatus = VodStatusEnum.parse(status);
            criteria.and("status").is(vodStatus);
        }

        // 查询出所有可播放的 bvod
        Query pageQuery = new Query(criteria)
                .with(Sort.by(Sort.Order.asc("ctime")))
                .skip((long) pageNo * pageSize)
                .limit(pageSize);
        List<VodInfo> vodInfoList = mongoTemplate.find(pageQuery, VodInfo.class);
        if (CollectionUtil.isEmpty(vodInfoList)) {
            return page.setTotal(0L).setPage(new ArrayList<>());
        }

        // 构建出 vod info 视图列表
        List<VodVO> vodVOList = batchBuildVodVOWithoutQuality(vodInfoList, true);

        Query totalQuery = new Query(criteria);
        long count = mongoTemplate.count(totalQuery, BVod.class);

        return page.setTotal(count).setPage(vodVOList);
    }

    public List<VodVO> batchBuildVodVOWithoutQuality(List<VodInfo> vodInfos, boolean needStatistics) {
        // 整理出所有的 cid
        Collection<Object> cidSet = vodInfos.stream()
                .map(VodInfo::getCid)
                .map(String::valueOf)
                .collect(Collectors.toSet());

        Map<Long, VodStatistics> vodStatisticsMap;
        // statics 数据查询
        if (needStatistics) {
            vodStatisticsMap = redisTemplate.opsForHash()
                    .multiGet(
                            VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY,
                            cidSet
                    ).stream()
                    .filter(Objects::nonNull)
                    .map(obj -> jsonHelper.convert(obj, VodStatistics.class)
                    )
                    .collect(Collectors.toMap(VodStatistics::getCid, v -> v));
        } else {
            vodStatisticsMap = new HashMap<>();
        }

        return vodInfos.stream().map(vodInfo -> {
            // 查询统计信息
            VodStatistics statistics = vodStatisticsMap.getOrDefault(vodInfo.getCid(), new VodStatistics());

            // 创建视图模型
            return new VodVO()
                    .setCid(vodInfo.getCid())
                    .setBvId(vodInfo.getBvId())
                    .setCoverUrl(vodInfo.getCoverUrl())
                    .setTitle(vodInfo.getTitle())
                    .setGcType(vodInfo.getGcType())
                    .setPartition(vodInfo.getPartition())
                    .setSubPartition(vodInfo.getSubPartition())
                    .setLabels(vodInfo.getLabels())
                    .setDesc(vodInfo.getDesc())
                    .setMtime(vodInfo.getMtime())
                    // 设置统计数据
                    .setViewCount(statistics.getViewCount())
                    .setLikeCount(statistics.getLikeCount())
                    .setBarrageCount(statistics.getBarrageCount())
                    .setCommentCount(statistics.getCommentCount())
                    .setCoinCount(statistics.getCoinCount())
                    .setCollectCount(statistics.getCollectCount())
                    .setShareCount(statistics.getShareCount());
        }).toList();
    }

    @Override
    public BVodVO videos(String bvid, Long cid) {
        BVodVO bVodVO = new BVodVO();

        // 查询出 bvod
        BVod bVod = mongoTemplate.findOne(new Query(Criteria.where("_id").is(bvid)), BVod.class);
        if (Objects.isNull(bVod) || Objects.isNull(bVod.getCidList()) || bVod.getCidList().isEmpty()) {
            throw new BusinessException("稿件不存在", StandardResponse.ERROR);
        }
        bVodVO.setBvId(bvid)
                .setMtime(bVod.getMtime());

        // 取出所有的可播放的 vod
        List<VodInfo> vodInfoList = mongoTemplate.find(
                new Query(Criteria.where("bvId").is(bVod.getBvId())
                        .and("status").is(VodStatusEnum.PASSED)),
                VodInfo.class);
        if (CollectionUtil.isEmpty(vodInfoList)) {
            throw BusinessException.businessError("稿件未开放");
        }

        // 取出所有的可播放的 vod 的 cid,查询统计信息
        Collection<Object> cidCollection = vodInfoList.stream().map(VodInfo::getCid).collect(Collectors.toSet());

        // 查询统计信息
        Map<Long, VodStatistics> staticsMap =
                batchQueryVodStatistics(cidCollection.stream().map(Object::toString).collect(Collectors.toSet()));

        // 查询用户观看数据
        Map<Long, Integer> vodPlayRecordMap = batchQueryPlayOffset(cidCollection);

        // 查询投稿人信息
        PreviewUserVO previewUserVO = queryAuthorPreviewVO(bVod.getUid());
        bVodVO.setAuthor(previewUserVO);

        // 查询清晰度分发信息
        Query distributeQuery = new Query(Criteria.where("_id").in(bVod.getCidList()));
        List<VodDistributeInfo> distributeInfoList = mongoTemplate
                .find(distributeQuery, VodDistributeInfo.class);
        Map<Long, VodDistributeInfo> distributeInfoMap = distributeInfoList.stream()
                .collect(Collectors.toMap(VodDistributeInfo::getCid,
                        vodDistributeInfo -> vodDistributeInfo));

        // 组装 vod 视图模型
        List<VodVO> vodVOList = batchBuildVodVO(vodInfoList, distributeInfoMap, vodPlayRecordMap, staticsMap);

        bVodVO.setVodList(vodVOList);

        return bVodVO;
    }

    @Override
    public Map<Long, VodStatistics> batchQueryVodStatistics(Collection<Object> cidCollection) {
        List<Object> list = redisTemplate.opsForHash()
                .multiGet(
                        VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY,
                        cidCollection
                );
        return list.stream()
                .filter(Objects::nonNull)
                .map(obj -> jsonHelper.convert(obj, VodStatistics.class))
                .collect(Collectors.toMap(VodStatistics::getCid, v -> v));
    }

    private Map<Long, Integer> batchQueryPlayOffset(Collection<Object> cidCollection) {
        String loginId = StpUtil.getLoginId("");
        Map<Long, Integer> vodPlayRecordMap;
        if (Objects.nonNull(loginId)) {
            // 查询视频观看数据
            List<Integer> offsets = redisTemplate.opsForHash()
                    .multiGet(VodCacheKeyEnum.HashKey.PLAY_OFFSET_CACHE_KEY.concat(loginId),
                            cidCollection.stream().map(String::valueOf).collect(Collectors.toSet()))
                    .stream()
                    .map(offset -> Objects.isNull(offset) ? 0 : Integer.parseInt(offset.toString()))
                    .toList();
            vodPlayRecordMap = new HashMap<>();
            List<Long> cidList = cidCollection.stream()
                    .map(Object::toString)
                    .map(Long::parseLong).toList();
            for (int i = 0; i < cidList.size(); i++) {
                vodPlayRecordMap.put(cidList.get(i), offsets.get(i));
            }
        } else {
            vodPlayRecordMap = Collections.emptyMap();
        }
        return vodPlayRecordMap;
    }

    private List<VodVO> batchBuildVodVO(List<VodInfo> vodInfoList,
                                        Map<Long, VodDistributeInfo> distributeInfoMap,
                                        Map<Long, Integer> vodPlayRecordMap,
                                        Map<Long, VodStatistics> staticsMap) {
        return vodInfoList
                .stream()
                .map(vod -> {
                    VodDistributeInfo vodDistributeInfo = distributeInfoMap.get(vod.getCid());
                    // 处理得到 quality 列表
                    List<QualityVO> qualityVOList = vodDistributeInfo.getQualityMap()
                            .values()
                            .stream()
                            // 排除预览
                            .filter(e -> !e.getQn().equals(Qn._PREVIEW.getQn()))
                            .sorted(Comparator.comparing(Quality::getQn))
                            .map(quality -> {
                                Qn qn = Qn.valueOfQn(quality.getQn());
                                String url = "/file/video/%s/%s/1".formatted(vodDistributeInfo.getCid(), qn.getDescription());
                                return new QualityVO().setName(qn.getDescription()).setUrl(url);
                            })
                            .collect(Collectors.toList());

                    // 获取每个视频的观看偏移量
                    Integer playOffset = vodPlayRecordMap.getOrDefault(vod.getCid(), 0);

                    // 获取统计数据
                    VodStatistics statics = staticsMap.getOrDefault(vod.getCid(), new VodStatistics());

                    statics.setViewCount(statics.getViewCount())
                            .setLikeCount(statics.getLikeCount())
                            .setBarrageCount(statics.getBarrageCount())
                            .setCommentCount(statics.getCommentCount())
                            .setCoinCount(statics.getCoinCount())
                            .setCollectCount(statics.getCollectCount())
                            .setShareCount(statics.getShareCount());

                    // 查询在线观看人数
                    Long onlineCount = redisTemplate.opsForZSet().size("online-".concat(String.valueOf(vod.getCid())));
                    if (Objects.isNull(onlineCount)) {
                        onlineCount = 0L;
                    }
                    // 组装视图模型
                    VodVO vodVO = new VodVO();
                    vodVO.setQuality(qualityVOList)
                            .setBvId(vod.getBvId())
                            .setCid(vod.getCid())
                            .setCoverUrl(vod.getCoverUrl())
                            .setTitle(vod.getTitle())
                            .setGcType(vod.getGcType())
                            .setPartition(vod.getPartition())
                            .setSubPartition(vod.getSubPartition())
                            .setLabels(vod.getLabels())
                            .setDesc(vod.getDesc())
                            .setOffset(playOffset)
                            .setViewCount(statics.getViewCount())
                            .setLikeCount(statics.getLikeCount())
                            .setBarrageCount(statics.getBarrageCount())
                            .setCommentCount(statics.getCommentCount())
                            .setCoinCount(statics.getCoinCount())
                            .setCollectCount(statics.getCollectCount())
                            .setShareCount(statics.getShareCount())
                            .setMtime(vod.getMtime())
                            .setOnlineCount(onlineCount);
                    return vodVO;
                }).toList();
    }

    private PreviewUserVO queryAuthorPreviewVO(String uid) {
        User user = mongoTemplate.findById(new ObjectId(uid), User.class);
        if (Objects.isNull(user)) {
            throw new BusinessException("稿件信息异常", StandardResponse.ERROR);
        }
        return new PreviewUserVO().setUid(user.getUid().toString())
                .setNickName(user.getNickname())
                .setAvatar(user.getAvatar())
                .setIntro(user.getIntro());
    }

    @Override
    public BVodVO sdVideo(Long cid) {
        VodInfo vodInfo = mongoTemplate.findById(cid, VodInfo.class);
        if (Objects.isNull(vodInfo)) {
            throw BusinessException.businessError("稿件异常");
        }
        // 查询清晰度分发信息
        Query distributeQuery = new Query(Criteria.where("_id").is(cid));
        VodDistributeInfo distributeInfo = mongoTemplate.findOne(distributeQuery, VodDistributeInfo.class);
        if (Objects.isNull(distributeInfo)) {
            throw BusinessException.businessError("稿件准备中");
        }
        Quality quality = distributeInfo.getQualityMap().get(Qn._360P.getQn());
        String qnDescription = Qn.valueOfQn(quality.getQn()).getDescription();
        QualityVO qualityVO = new QualityVO().setName(qnDescription)
                .setUrl("/file/video/%s/%s/1".formatted(cid, qnDescription));
        // 查询投稿人信息
        PreviewUserVO author = queryAuthorPreviewVO(vodInfo.getUid());


        VodVO vodVO = new VodVO()
                .setCid(vodInfo.getCid())
                .setBvId(vodInfo.getBvId())
                .setCoverUrl(vodInfo.getCoverUrl())
                .setTitle(vodInfo.getTitle())
                .setGcType(vodInfo.getGcType())
                .setPartition(vodInfo.getPartition())
                .setSubPartition(vodInfo.getSubPartition())
                .setLabels(vodInfo.getLabels())
                .setDesc(vodInfo.getDesc())
                .setQuality(Collections.singletonList(qualityVO));

        BVodVO bVodVO = new BVodVO();
        bVodVO.setAuthor(author)
                .setBvId(vodInfo.getBvId())
                .setMtime(vodInfo.getMtime())
                .setVodList(Collections.singletonList(vodVO));
        return bVodVO;
    }

    @Override
    public void updatePlayTime(String bvId, Long cid, Integer time) {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return;
        }

        // 记录播放到的位置 -> 下次恢复播放
        redisTemplate.opsForHash().put(
                VodCacheKeyEnum.HashKey.PLAY_OFFSET_CACHE_KEY.concat(uid),
                cid.toString(),
                time);

        // 统计在线人数
        redisTemplate.opsForZSet().add("online-%s".formatted(cid), uid, Instant.now().plus(5, ChronoUnit.SECONDS).toEpochMilli());

    }


    public List<PreviewBVodVO> buildPreviewBVodList(List<VodInfo> vodInfoList) {
        Map<String, VodInfo> vodInfoMap = vodInfoList.stream().collect(Collectors.toMap(VodInfo::getBvId, vodInfo -> vodInfo));

        List<String> bvIdList = vodInfoList.stream().map(VodInfo::getBvId).toList();

        List<BVod> bvodList = mongoTemplate.find(new Query(Criteria.where("_id").in(bvIdList)), BVod.class);

        // 整理出所有的 cid
        Set<Object> cidSet = vodInfoList.stream().map(VodInfo::getCid).map(String::valueOf).collect(Collectors.toSet());

        // statics 数据查询
        Map<Long, VodStatistics> vodStatisticsMap = batchQueryVodStatistics(cidSet);

        // 查询 up 主信息
        List<String> uidList = bvodList.stream().map(BVod::getUid).toList();

        List<User> userList = mongoTemplate.find(new Query(Criteria.where("_id").in(uidList)), User.class);

        Map<String, User> userMap = userList.stream()
                .collect(Collectors.toMap(u -> u.getUid().toString(), user -> user));


        return bvodList.stream().map(bvod -> {
            PreviewBVodVO previewBVodVO = new PreviewBVodVO();

            // 设置预览数据
            VodInfo vodInfo = vodInfoMap.get(bvod.getBvId());
            VodStatistics statistics = vodStatisticsMap.getOrDefault(vodInfo.getCid().toString(), new VodStatistics());
            previewBVodVO.setBvId(bvod.getBvId())
                    .setCoverUrl(vodInfo.getCoverUrl())
                    .setTitle(vodInfo.getTitle())
                    .setDesc(vodInfo.getDesc())
                    .setPartition(vodInfo.getPartition());
            // 设置统计数据
            previewBVodVO.setViewCount(statistics.getViewCount())
                    .setLikeCount(statistics.getLikeCount())
                    .setBarrageCount(statistics.getBarrageCount())
                    .setCommentCount(statistics.getCommentCount())
                    .setCoinCount(statistics.getCoinCount())
                    .setCollectCount(statistics.getCollectCount())
                    .setShareCount(statistics.getShareCount());

            // 设置预览视频
            String url = "/file/video/%s/%s/1".formatted(vodInfo.getCid(), Qn._PREVIEW.getDescription());
            PreviewVodVO previewVodVO = new PreviewVodVO().setCid(vodInfo.getCid())
                    .setName(Qn._PREVIEW.getDescription())
                    .setUrl(url);
            previewBVodVO.setPreview(previewVodVO);

            // 设置用户预览信息
            PreviewUserVO previewUserVO = new PreviewUserVO();
            User user = userMap.getOrDefault(bvod.getUid(), User.UNKNOWN);
            previewUserVO.setUid(user.getUid().toString())
                    .setNickName(user.getNickname())
                    .setAvatar(user.getAvatar())
                    .setIntro(user.getIntro());
            previewBVodVO.setAuthor(previewUserVO);

            previewBVodVO.setDuration(vodInfo.getDuration());
            return previewBVodVO;
        }).toList();
    }

    @Override
    public VodThumbnails thumbnails(Long cid) {
        VodThumbnails thumbnails = mongoTemplate.findOne(new Query(Criteria.where("_id").is(cid)), VodThumbnails.class);
        if (Objects.isNull(thumbnails)) {
            thumbnails = new VodThumbnails().setCid(cid).setThumbnails(new ArrayList<>());
        }
        return thumbnails;
    }

    @Override
    public Page<VodVO> reviewPage(Integer pageNo, Integer pageSize, String status) {
        Page<VodVO> page = new Page<VodVO>().setPageNo(pageNo).setPageSize(pageSize);
        pageNo = Math.max(pageNo - 1, 0);
        VodStatusEnum vodStatus = VodStatusEnum.parse(status);
        Criteria criteria = Criteria.where("status").is(vodStatus);

        // 查询出所有 bvod
        Query pageQuery = new Query(criteria)
                .with(Sort.by(Sort.Order.asc("ctime")))
                .skip((long) pageNo * pageSize)
                .limit(pageSize);
        List<VodInfo> vodInfoList = mongoTemplate.find(pageQuery, VodInfo.class);
        if (CollectionUtil.isEmpty(vodInfoList)) {
            return page.setTotal(0L).setPage(new ArrayList<>());
        }

        // 构建出 vod info 视图列表
        List<VodVO> vodVOList = batchBuildVodVOWithoutQuality(vodInfoList, false);

        Query totalQuery = new Query(criteria);
        long count = mongoTemplate.count(totalQuery, VodInfo.class);

        return page.setTotal(count).setPage(vodVOList);
    }

    @Override
    public InteractionInfoVO interactionInfo(Long cid) {
        String uid = (String) StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return new InteractionInfoVO();
        }

        String likeKey = VodCacheKeyEnum.SetKey.LIKE_SET.concat(uid);
        String coinKey = VodCacheKeyEnum.SetKey.COIN_SET.concat(uid);
        String collectKey = VodCacheKeyEnum.SetKey.COLLECT_SET.concat(uid);

        return new InteractionInfoVO()
                .setLiked(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, cid.toString())))
                .setCoined(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(coinKey, cid.toString())))
                .setCollected(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(collectKey, cid.toString())));
    }


}
