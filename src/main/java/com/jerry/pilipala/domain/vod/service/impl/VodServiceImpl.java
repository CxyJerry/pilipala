package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionRecord;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodPlayOffsetRecord;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatics;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.BVod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodProfiles;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.config.FileConfig;
import com.jerry.pilipala.domain.vod.service.media.UGCSchema;
import com.jerry.pilipala.domain.vod.service.media.encoder.Encoder;
import com.jerry.pilipala.application.dto.PreUploadDTO;
import com.jerry.pilipala.application.dto.VideoPostDTO;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import com.jerry.pilipala.infrastructure.enums.VodStatusEnum;
import com.jerry.pilipala.infrastructure.enums.video.Resolution;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.entity.mongo.distribute.Quality;
import com.jerry.pilipala.domain.vod.entity.mongo.distribute.VodDistributeInfo;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.Thumbnails;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.VodThumbnails;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.media.profiles.Profile;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.SecurityTool;
import com.jerry.pilipala.application.vo.PreUploadVO;
import com.jerry.pilipala.application.vo.QualityVO;
import com.jerry.pilipala.application.vo.bvod.BVodVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.application.vo.vod.PreviewVodVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
    private FFprobe fFprobe;
    private FFmpeg fFmpeg;

    private final ObjectMapper mapper;

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Snowflake snowflake = IdUtil.getSnowflake();

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final ApplicationEventPublisher applicationEventPublisher;

    private final FileConfig fileConfig;

    private final UGCSchema ugcSchema;
    private final TaskExecutor taskExecutor;

    private final HttpServletResponse response;
    private final VodInfoRepository vodInfoRepository;


    public static boolean windows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }


    public VodServiceImpl(ObjectMapper mapper,
                          MongoTemplate mongoTemplate,
                          RedisTemplate<String, Object> redisTemplate,
                          ApplicationEventPublisher applicationEventPublisher,
                          FileConfig fileConfig,
                          UGCSchema ugcSchema,
                          @Qualifier("asyncServiceExecutor") TaskExecutor taskExecutor,
                          HttpServletResponse response,
                          VodInfoRepository vodInfoRepository) {
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fileConfig = fileConfig;
        this.ugcSchema = ugcSchema;
        this.taskExecutor = taskExecutor;
        this.response = response;
        this.vodInfoRepository = vodInfoRepository;
    }

    {
        File ffprobeFile;
        File ffmpegFile;
        try {
            String ffprobeResourcesPath = "classpath:bin/ffprobe";
            String ffmpegResourcesPath = "classpath:bin/ffmpeg";
            if (windows()) {
                ffprobeResourcesPath += ".exe";
                ffmpegResourcesPath += ".exe";
            }
            ffprobeFile = ResourceUtils.getFile(ffprobeResourcesPath);
            ffmpegFile = ResourceUtils.getFile(ffmpegResourcesPath);
            fFprobe = new FFprobe(ffprobeFile.getAbsolutePath());
            fFmpeg = new FFmpeg(ffmpegFile.getAbsolutePath());
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
            String json = mapper.writeValueAsString(preUploadDTO);
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
    @Transactional(rollbackFor = Exception.class, value = "multiTransactionManager")
    public void add(VideoPostDTO videoPostDTO) {
        Query query = new Query(Criteria.where("_id").is(videoPostDTO.getBvId()));
        BVod bVod = mongoTemplate.findOne(query, BVod.class);
        if (Objects.isNull(bVod)) {
            throw new BusinessException("稿件不存在，请重新投稿", StandardResponse.ERROR);
        }
        bVod.getCidList().add(videoPostDTO.getCid());
        bVod.setReady(true);
        mongoTemplate.save(bVod);


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
                .setMtime(System.currentTimeMillis());
        mongoTemplate.save(vodInfo);

        VodHandleActionEvent actionEvent = new VodHandleActionEvent(
                VodHandleActionEnum.SUBMIT,
                ActionStatusEnum.finished,
                videoPostDTO.getCid()
        );
        // 触发提交结束
        applicationEventPublisher.publishEvent(actionEvent);
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

        log.info("cid [{}] -> select profiles: {}", cid, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(profiles));
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
        String originFilePath = "%s/%s.%s".formatted(fileConfig.getWorkDir(), vod.getFilename(), vod.getExt());
        // 规划转码规格
        List<Profile> profiles = schema(cid, vod);
        CompletableFuture<?>[] tasks = new CompletableFuture[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            final Profile profile = profiles.get(i);
            tasks[i] = CompletableFuture.runAsync(() -> transcodeTask(profile, vod, originFilePath), taskExecutor);
        }
        CompletableFuture.allOf(tasks).get();
    }


    @Override
    public void transcodeThumbnails(Long cid) {
        CompletableFuture.runAsync(() -> {
            // 每两秒抽一帧
            // ffmpeg -i f2023102822062151aed32d88f9984f5cb2f264e26c85b9.mp4 -vf "fps=0.5,scale=56:32" "thumbnails_%05d.png
            Query query = new Query(Criteria.where("_id").is(cid));
            Vod vod = mongoTemplate.findOne(query, Vod.class);
            if (Objects.isNull(vod)) {
                throw new BusinessException("稿件不存在", StandardResponse.ERROR);
            }
            String originVideoPath = "%s/%s.%s".formatted(fileConfig.getWorkDir(), vod.getFilename(), vod.getExt());

            String thumbOutputDirPath = "%s/%s/thumbnails".formatted(fileConfig.getWorkDir(), vod.getFilename());
            File thumbOutputDir = new File(thumbOutputDirPath);
            if (!thumbOutputDir.exists()) {
                thumbOutputDir.mkdirs();
            }

            String thumbnailsNamePattern = "%s/%s".formatted(thumbOutputDirPath, "%05d.png");
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
                thumbnailsPng.setTime(i).setUrl("%s/thumbnails/%05d.png".formatted(vod.getFilename(), i));
                thumbnails.add(thumbnailsPng);
            }
            VodThumbnails vodThumbnails = new VodThumbnails()
                    .setCid(cid)
                    .setThumbnails(thumbnails);
            mongoTemplate.save(vodThumbnails);

            log.info("cid: {},thumbnails generate completed.", cid);
        });
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
        String outputDir = "%s/%s".formatted(fileConfig.getWorkDir(), saveTo);
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 16.mpd
        String outputFilename = "%s.%s".formatted(
                profile.getEncoder().quality().getQn(),
                profile.getFormat().getExt()
        );
        String outputPath = "%s/%s/%s".formatted(fileConfig.getWorkDir(), profile.getSaveTo(), outputFilename);

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
        VodDistributeInfo vodDistributeInfo = mongoTemplate.findOne(queryDistributeInfo, VodDistributeInfo.class);
        if (Objects.isNull(vodDistributeInfo)) {
            vodDistributeInfo = new VodDistributeInfo();
            vodDistributeInfo.setCid(cid)
                    .setReady(false)
                    .setQualityMap(new HashMap<>())
                    .setFilename(filename)
            ;
        }
        Quality quality = new Quality();
        quality.setQn(qn.getQn())
                .setSaveTo(saveTo)
                .setExt(ext)
                .setType("auto");
        vodDistributeInfo.getQualityMap().put(qn.getQn(), quality);
        mongoTemplate.save(vodDistributeInfo);
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
     */
    @Override
    public void video(Long cid, String format, String name) {
        String outputFilename;
        VodInfo vodInfo = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(cid)
                        .and("status").is(VodStatusEnum.PASSED)),
                VodInfo.class);
        if (Objects.isNull(vodInfo)) {
            throw new BusinessException("稿件未开放", StandardResponse.ERROR);
        }

        VodDistributeInfo vodDistributeInfo = mongoTemplate.findOne(new Query(Criteria.where("_id").is(cid)), VodDistributeInfo.class);
        assert vodDistributeInfo != null;
        if (!Objects.isNull(vodDistributeInfo.getReady()) && !vodDistributeInfo.getReady()) {
            throw new BusinessException("稿件未准备妥当", StandardResponse.ERROR);
        }
        Qn qn = Qn.valueOfFormat(format);
        Quality quality = vodDistributeInfo.getQualityMap().get(qn.getQn());
        String saveTo = quality.getSaveTo();

        if (name.contains("stream")) {
            outputFilename = "%s/%s".formatted(saveTo, name);
        } else {
            outputFilename = "%s/%s.%s".formatted(saveTo, qn.getQn(), quality.getExt());
        }

        String videoFilePath = "%s/%s".formatted(fileConfig.getWorkDir(), outputFilename);
        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new BusinessException("视频文件丢失", StandardResponse.ERROR);
        }
        response.reset();
        response.setContentType("application/dash+xml");
        response.setCharacterEncoding("utf-8");
        response.setContentLengthLong(videoFile.length());
        response.setHeader("Content-Disposition", "attachment;filename=%s".formatted(outputFilename));

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(videoFile));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("视频文件读取失败，", e);
            throw new BusinessException("视频文件读取失败", StandardResponse.ERROR);
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
        redisTemplate.opsForSet().add(vodInfo.getPartition(), vodInfo.getBvId());
        redisTemplate.opsForSet().add("all_bvod", vodInfo.getBvId());
        mongoTemplate.save(vodInfo);

        User authorModel = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(vodInfo.getUid()))),
                User.class);
        if (Objects.isNull(authorModel)) {
            throw new BusinessException("创作者信息异常", StandardResponse.ERROR);
        }
        UserEntity author = new UserEntity().setUid(authorModel.getUid().toString())
                .setTel(authorModel.getTel())
                .setNickname(authorModel.getNickname())
                .setCtime(authorModel.getCtime());

        // 存入图数据库
        VodInfoEntity vodInfoEntity = new VodInfoEntity().setCid(vodInfo.getCid())
                .setBvId(vodInfo.getBvId())
                .setAuthor(author)
                .setGcType(vodInfo.getGcType())
                .setPartition(vodInfo.getPartition())
                .setSubPartition(vodInfo.getSubPartition())
                .setLabels(vodInfo.getLabels())
                .setCtime(vodInfo.getCtime());
        vodInfoRepository.save(vodInfoEntity);
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
        List<VodVO> vodVOList = buildVodVOList(vodInfoList);

        Query totalQuery = new Query(criteria);
        long count = mongoTemplate.count(totalQuery, BVod.class);

        return page.setTotal(count).setPage(vodVOList);
    }

    public List<VodVO> buildVodVOList(List<VodInfo> vodInfos) {
        // 整理出所有的 cid
        Collection<Object> cidSet = vodInfos.stream()
                .map(VodInfo::getCid)
                .collect(Collectors.toSet());


        // statics 数据查询
        List<VodStatics> bVodStaticsList = mongoTemplate.find(
                new Query(Criteria.where("_id").in(cidSet)),
                VodStatics.class);

        Map<Long, VodStatics> vodStaticsMap;
        vodStaticsMap = bVodStaticsList.stream().filter(Objects::nonNull).map(o -> mapper.convertValue(o, VodStatics.class))
                .collect(Collectors.toMap(VodStatics::getCid, vodStatics -> vodStatics));

        return vodInfos.stream().map(vodInfo -> {
            // 查询统计信息
            VodStatics vodStatics = vodStaticsMap.getOrDefault(vodInfo.getCid(), VodStatics.EMPTY_STATICS);
            if (Objects.isNull(vodStatics)) {
                vodStatics = VodStatics.EMPTY_STATICS;
            }

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
                    .setViewCount(vodStatics.getViewCount())
                    .setLikeCount(vodStatics.getLikeCount())
                    .setBarrageCount(vodStatics.getBarrageCount())
                    .setCommentCount(vodStatics.getCommentCount())
                    .setCoinCount(vodStatics.getCoinCount())
                    .setCollectCount(vodStatics.getCollectCount())
                    .setShareCount(vodStatics.getShareCount());
        }).toList();
    }

    @Override
    public BVodVO videos(String bvid) {
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
            throw new BusinessException("稿件未开放", StandardResponse.ERROR);
        }

        // 取出所有的可播放的 vod 的 cid,查询统计信息
        Collection<Object> cidList = vodInfoList.stream().map(VodInfo::getCid).collect(Collectors.toSet());
        List<VodStatics> vodStaticsList = mongoTemplate.find(
                new Query(Criteria.where("id").in(cidList)),
                VodStatics.class);

        Map<Long, VodStatics> staticsMap = vodStaticsList.stream()
                .collect(Collectors.toMap(VodStatics::getCid, vodStatics -> vodStatics));


        // 查询用户观看数据
        Object loginId = StpUtil.getLoginId(null);
        Map<Long, VodPlayOffsetRecord> vodPlayRecordMap;
        if (Objects.nonNull(loginId)) {
            // 查询视频观看数据
            Query userPlayInfoQuery = new Query(
                    Criteria.where("uid").is(loginId)
                            .and("cid").in(bVod.getCidList())
            );
            List<VodPlayOffsetRecord> vodPlayOffsetRecords = mongoTemplate.find(userPlayInfoQuery, VodPlayOffsetRecord.class);
            vodPlayRecordMap = vodPlayOffsetRecords.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(VodPlayOffsetRecord::getCid, vodPlayOffsetRecord -> vodPlayOffsetRecord));
        } else {
            vodPlayRecordMap = Collections.emptyMap();
        }

        // 查询投稿人信息
        User user = mongoTemplate.findOne(new Query(Criteria.where("_id").is(bVod.getUid())), User.class);
        if (Objects.isNull(user)) {
            throw new BusinessException("稿件信息异常", StandardResponse.ERROR);
        }
        PreviewUserVO previewUserVO = new PreviewUserVO().setUid(user.getUid().toString())
                .setNickName(user.getNickname())
                .setAvatar(user.getAvatar())
                .setIntro(user.getIntro());
        bVodVO.setAuthor(previewUserVO);


        // 查询清晰度分发信息
        Query distributeQuery = new Query(Criteria.where("_id").in(bVod.getCidList()));
        List<VodDistributeInfo> distributeInfoList = mongoTemplate.find(distributeQuery, VodDistributeInfo.class);
        Map<Long, VodDistributeInfo> distributeInfoMap = distributeInfoList.stream()
                .collect(Collectors.toMap(VodDistributeInfo::getCid, vodDistributeInfo -> vodDistributeInfo));

        // 组装 vod 视图模型
        List<VodVO> vodVOList = vodInfoList.stream().map(vod -> {
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
            VodPlayOffsetRecord vodPlayOffsetRecord = vodPlayRecordMap.getOrDefault(
                    vodDistributeInfo.getCid(),
                    new VodPlayOffsetRecord()
            );

            // 获取统计数据
            VodStatics statics = staticsMap.getOrDefault(vod.getCid(), VodStatics.EMPTY_STATICS);

            // 查询在线观看人数
            Long onlineCount = redisTemplate.opsForZSet().size("online-".concat(String.valueOf(vod.getCid())));
            if (Objects.isNull(onlineCount)) {
                onlineCount = 0L;
            }
            // 组装视图模型
            VodVO vodVO = new VodVO();
            vodVO.setQuality(qualityVOList)
                    .setCid(vod.getCid())
                    .setCoverUrl(vod.getCoverUrl())
                    .setTitle(vod.getTitle())
                    .setGcType(vod.getGcType())
                    .setPartition(vod.getPartition())
                    .setSubPartition(vod.getSubPartition())
                    .setLabels(vod.getLabels())
                    .setDesc(vod.getDesc())
                    .setOffset(vodPlayOffsetRecord.getTime())
                    .setViewCount(statics.getViewCount())
                    .setLikeCount(statics.getLikeCount())
                    .setBarrageCount(statics.getBarrageCount())
                    .setCommentCount(statics.getCommentCount())
                    .setCoinCount(statics.getCoinCount())
                    .setCollectCount(statics.getCollectCount())
                    .setShareCount(statics.getShareCount())
                    .setMtime(vod.getMtime())
                    .setOnlineCount(onlineCount + 1);
            return vodVO;
        }).toList();

        bVodVO.setVodList(vodVOList);

        return bVodVO;
    }

    @Override
    public void updatePlayTime(String bvId, Long cid, Integer time) {
        String uid = StpUtil.getLoginId(null);
        if (Objects.isNull(uid)) {
            return;
        }


        // 记录播放到的位置 -> 下次恢复播放
        Query query = new Query(Criteria.where("uid").is(uid).and("cid").is(cid));
        VodPlayOffsetRecord playRecord = mongoTemplate.findOne(query, VodPlayOffsetRecord.class);
        if (Objects.isNull(playRecord)) {
            playRecord = new VodPlayOffsetRecord().setUid(uid).setCid(cid).setTime(0);
        }

        playRecord.setTime(time);
        mongoTemplate.save(playRecord);

        // 统计在线人数
        redisTemplate.opsForZSet().add("online-%s".formatted(cid), uid, Instant.now().plus(5, ChronoUnit.SECONDS).toEpochMilli());
        // 记录播放时间 -> 统计数据
        // todo 写入 clickhouse
//        VodPlayTimeRecord vodPlayTimeRecord = new VodPlayTimeRecord()
//                .setTel(userInfoBO.getTel())
//                .setCid(cid)
//                .setTime(time);
//        mongoTemplate.save(vodPlayTimeRecord);
    }

    @Override
    public void updatePlayCount(Long cid) {
        mongoTemplate.upsert(new Query(Criteria.where("_id").is(cid)), new Update().inc("viewCount", 1), VodStatics.class);
    }


    public List<PreviewBVodVO> buildPreviewBVodList(List<VodInfo> vodInfoList) {
        Map<String, VodInfo> vodInfoMap = vodInfoList.stream().collect(Collectors.toMap(VodInfo::getBvId, vodInfo -> vodInfo));

        List<String> bvIdList = vodInfoList.stream().map(VodInfo::getBvId).toList();

        List<BVod> bvodList = mongoTemplate.find(new Query(Criteria.where("_id").in(bvIdList)), BVod.class);
        // 整理出所有的 cid
        Set<Object> cidSet = vodInfoList.stream().map(VodInfo::getCid).collect(Collectors.toSet());

        // statics 数据查询
        List<VodStatics> bVodStaticsList = mongoTemplate.find(new Query(Criteria.where("_id").in(cidSet)), VodStatics.class);
        Map<Long, VodStatics> vodStaticsMap = bVodStaticsList.stream().filter(Objects::nonNull).map(o -> mapper.convertValue(o, VodStatics.class))
                .collect(Collectors.toMap(VodStatics::getCid, vodStatics -> vodStatics));


        // 查询 up 主信息
        List<ObjectId> telList = bvodList.stream().map(bvod -> new ObjectId(bvod.getUid())).toList();
        List<User> users = mongoTemplate.find(new Query(Criteria.where("_id").in(telList)), User.class);
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(user -> user.getUid().toString(), user -> user));


        return bvodList.stream().map(bvod -> {
            PreviewBVodVO previewBVodVO = new PreviewBVodVO();

            // 设置预览数据
            VodInfo vodInfo = vodInfoMap.get(bvod.getBvId());
            VodStatics statics = vodStaticsMap.getOrDefault(vodInfo.getCid(), VodStatics.EMPTY_STATICS);
            previewBVodVO.setBvId(bvod.getBvId())
                    .setCoverUrl(vodInfo.getCoverUrl())
                    .setTitle(vodInfo.getTitle())
                    .setDesc(vodInfo.getDesc())
                    .setPartition(vodInfo.getPartition());
            // 设置统计数据
            previewBVodVO.setViewCount(statics.getViewCount())
                    .setLikeCount(statics.getLikeCount())
                    .setBarrageCount(statics.getBarrageCount())
                    .setCommentCount(statics.getCommentCount())
                    .setCoinCount(statics.getCoinCount())
                    .setCollectCount(statics.getCollectCount())
                    .setShareCount(statics.getShareCount());

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
}
