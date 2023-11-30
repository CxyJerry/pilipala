package com.jerry.pilipala.domain.vod.service.impl;

import com.jerry.pilipala.domain.vod.entity.mongo.distribute.Quality;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.service.FileService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.config.FileConfig;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String[] SUPPORT_VIDEO_CONTAINER = {"mp4", "flv", "avi"};

    private final MongoTemplate mongoTemplate;
    private final HttpServletResponse response;
    private final FileConfig fileConfig;
    private final ApplicationEventPublisher applicationEventPublisher;

    public FileServiceImpl(MongoTemplate mongoTemplate,
                           HttpServletResponse response,
                           FileConfig fileConfig,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.mongoTemplate = mongoTemplate;
        this.response = response;
        this.fileConfig = fileConfig;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public String uploadCover(MultipartFile file) {
        String ext = parseExt(file);
        String uploadFilename = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "." + ext;
        String filepath = fileConfig.getWorkDir() + '/' + uploadFilename;

        try {
            InputStream inputStream = file.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);

            File dest = new File(filepath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            BufferedImage scaleResult = new BufferedImage(672, 378, BufferedImage.TYPE_INT_RGB);
            scaleResult.getGraphics().drawImage(image, 0, 0, 672, 378, null);

            ImageIO.write(scaleResult, ext, dest);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new BusinessException("封面上传失败", StandardResponse.ERROR);
        }
        return uploadFilename;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, value = "multiTransactionManager")
    public void uploadVideo(MultipartFile file, Long cid) {
        String ext = parseExt(file);
        if (!Arrays.asList(SUPPORT_VIDEO_CONTAINER).contains(ext)) {
            throw new BusinessException("视频格式不支持", StandardResponse.ERROR);
        }
        Query query = new Query(Criteria.where("_id").is(cid));
        Vod vod = mongoTemplate.findOne(query, Vod.class);
        if (Objects.isNull(vod)) {
            throw new BusinessException("稿件不存在", StandardResponse.ERROR);
        }
        String filename = vod.getFilename();
        String filepath = fileConfig.getWorkDir() + '/' + filename + "." + ext;
        saveFile(filepath, file);
        vod.setExt(ext);
        mongoTemplate.save(vod);

        // 触发上传结束
        VodHandleActionEvent event = new VodHandleActionEvent(VodHandleActionEnum.UPLOAD, ActionStatusEnum.finished, cid);
        applicationEventPublisher.publishEvent(event);

    }

    @Override
    public String parseExt(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext;
        if (Objects.nonNull(originalFilename)) {
            int dotIdx = originalFilename.lastIndexOf(".");
            if (dotIdx != -1) {
                ext = originalFilename.substring(dotIdx + 1);
            } else {
                ext = "";
            }
        } else {
            ext = "";
        }
        return ext;
    }

    @Override
    public void saveFile(String filepath, MultipartFile file) {
        File dest = new File(filepath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            log.error("上传失败,", e);
            throw new BusinessException("上传失败", StandardResponse.ERROR);
        }
    }

    @Override
    public String downloadVideo(String filename, String ext) {
        return null;
    }

    @Override
    public void deleteVideoOfWorkSpace(String filename, String ext) {

    }

    @Override
    public String generateThumbnailsDirPath(String filename) {
        return null;
    }

    @Override
    public String generateTranscodeResSaveToPath(String saveTo) {
        return null;
    }

    @Override
    public String filePathRemoveWorkspace(String path) {
        return null;
    }

    @Override
    public void uploadDirToOss(String dirPath) {

    }

    @Override
    public void deleteDirOfWorkSpace(String dirPath) {

    }

    @Override
    public ResponseEntity<InputStreamResource> video(String name, Quality quality) {
        return null;
    }

    @Override
    public void cover(String filename) {
        File file = new File(fileConfig.getWorkDir() + '/' + filename);
        if (!file.exists()) {
            throw new BusinessException("文件不存在", StandardResponse.ERROR);
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "attachment;filename=%s".formatted(filename));

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("下载失败，", e);
            throw new BusinessException("文件下载失败", StandardResponse.ERROR);
        }
    }

}
