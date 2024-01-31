package com.jerry.pilipala.domain.vod.service.impl;

import cn.hutool.core.io.FileUtil;
import com.jerry.pilipala.application.vo.vod.UploadVO;
import com.jerry.pilipala.domain.vod.entity.mongo.distribute.Quality;
import com.jerry.pilipala.domain.vod.entity.mongo.event.VodHandleActionEvent;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Vod;
import com.jerry.pilipala.domain.vod.service.FileService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.config.FileConfig;
import com.jerry.pilipala.infrastructure.config.Qiniu;
import com.jerry.pilipala.infrastructure.enums.ActionStatusEnum;
import com.jerry.pilipala.infrastructure.enums.VodHandleActionEnum;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.*;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private static final String[] SUPPORT_VIDEO_CONTAINER = {"mp4", "flv", "avi"};

    private final FileConfig fileConfig;
    private final MongoTemplate mongoTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Qiniu qiniu;
    private final JsonHelper jsonHelper;

    private final RestTemplate restTemplate;
    private final HttpServletResponse response;
    private final RedisTemplate<String, Object> redisTemplate;

    public FileServiceImpl(FileConfig fileConfig, MongoTemplate mongoTemplate,
                           ApplicationEventPublisher applicationEventPublisher,
                           Qiniu qiniu,
                           JsonHelper jsonHelper,
                           RestTemplate restTemplate,
                           HttpServletResponse response,
                           RedisTemplate<String, Object> redisTemplate) {
        this.fileConfig = fileConfig;
        this.mongoTemplate = mongoTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.qiniu = qiniu;
        this.jsonHelper = jsonHelper;
        this.restTemplate = restTemplate;
        this.response = response;
        this.redisTemplate = redisTemplate;
    }

    private Response uploadToQiniu(InputStream inputStream, String key, StringMap params, String mime) {
        try {
            Configuration configuration = new Configuration(Region.region0());
            UploadManager uploadManager = new UploadManager(configuration);
            Auth auth = Auth.create(qiniu.getAccessKey(), qiniu.getSecretKey());
            String uploadToken;
            if ("image/png".equals(mime)) {
                uploadToken = auth.uploadToken(qiniu.getImgBucket());
            } else {
                uploadToken = auth.uploadToken(qiniu.getVodBucket());
            }

            Response response = uploadManager.put(inputStream, key, uploadToken, params, mime);
            if (response.statusCode != HttpStatus.OK.value()) {
                log.error("上传请求异常,cause: {}", response.error);
            }
            return response;
        } catch (Exception e) {
            throw BusinessException.businessError("上传文件失败");
        }
    }

    @Override
    public String uploadCover(MultipartFile file) {
        try {
            BufferedImage originImage = ImageIO.read(file.getInputStream());
            Image scaledInstance = originImage.getScaledInstance(672, 378, Image.SCALE_SMOOTH);

            BufferedImage bufferedImage = new BufferedImage(672, 378, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(scaledInstance, 0, 0, null);
            g2d.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            Response response = uploadToQiniu(inputStream, null, null, "image/png");

            DefaultPutRet result = jsonHelper.parse(response.bodyString(), DefaultPutRet.class);
            log.info("封面上传成功,key: {}", result.key);
            return result.key;
        } catch (Exception e) {
            log.error("封面上传失败", e);
            throw BusinessException.businessError("视频封面上传失败");
        }
    }

    @Override
    public UploadVO uploadVideo(Long cid) {
        Auth auth = Auth.create(qiniu.getAccessKey(), qiniu.getSecretKey());
        Configuration configuration = new Configuration(Region.region0());
        BucketManager bucketManager = new BucketManager(auth, configuration);
        String key = String.valueOf(cid);

        try {
            FileInfo fileInfo = bucketManager.stat(qiniu.getVodBucket(), key);
            if (Objects.nonNull(fileInfo)) {
                throw BusinessException.businessError("文件已存在");
            }
        } catch (QiniuException ex) {
            if (ex.code() != 612) {
                log.error("获取七牛文件信息失败", ex);
            } else {
                log.warn("文件不存在");
            }
        }
        Query query = new Query(Criteria.where("_id").is(cid));
        Vod vod = mongoTemplate.findOne(query, Vod.class);
        if (Objects.isNull(vod)) {
            throw new BusinessException("稿件不存在", StandardResponse.ERROR);
        }
        String filename = vod.getFilename();
        UploadVO uploadVO = new UploadVO()
                .setFilename(filename);
        String token = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(token)) {
            uploadVO.setToken(token);
            return uploadVO;
        }

        token = auth.uploadToken(qiniu.getVodBucket());
        uploadVO.setToken(token);
        redisTemplate.opsForValue().set(key, token, 24 * 60 * 60, TimeUnit.SECONDS);
        return uploadVO;
    }

    @Override
    public void uploadCompleted(Long cid) {
        // 触发上传结束
        VodHandleActionEvent event = new VodHandleActionEvent(VodHandleActionEnum.UPLOAD, ActionStatusEnum.finished, cid);
        applicationEventPublisher.publishEvent(event);
        String key = String.valueOf(cid);
        redisTemplate.delete(key);
        log.info("cid: {} upload finished", cid);
    }

    @Override
    public String downloadVideo(String filename, String ext) {
        FileOutputStream outputStream = null;
        DownloadUrl url = new DownloadUrl(qiniu.getImgDomain(), false, filename);
        Auth auth = Auth.create(qiniu.getAccessKey(), qiniu.getSecretKey());
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        try {
            String urlString = url.buildURL(auth, deadline);
            log.info("下载视频源文件: {}", urlString);

            String filePath = "%s/%s.%s".formatted(fileConfig.getWorkDir(), filename, ext);
            File file = new File(filePath);
            if (file.exists()) {
                return filePath;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, byte[].class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("文件下载失败,code: %s,body: %s".formatted(
                        response.getStatusCode().value(),
                        response.getBody())
                );
                throw BusinessException.businessError("视频文件下载失败");
            }
            byte[] fileData = response.getBody();
            if (Objects.isNull(fileData)) {
                throw BusinessException.businessError("找不到视频文件");
            }
            outputStream = new FileOutputStream(filePath);
            outputStream.write(fileData);
            log.info("视频下载完成");

            return filePath;
        } catch (IOException e) {
            log.error("视频资源获取失败,", e);
            throw BusinessException.businessError("视频资源获取失败");
        } finally {
            if (Objects.nonNull(outputStream)) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void deleteVideoOfWorkSpace(String filename, String ext) {
        try {
            String filePath = "%s/%s.%s".formatted(fileConfig.getWorkDir(), filename, ext);
            File video = new File(filePath);
            video.delete();
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }

    @Override
    public String generateThumbnailsDirPath(String filename) {
        String thumbOutputDirPath = "%s/%s/thumbnails".formatted(fileConfig.getWorkDir(), filename);
        File thumbOutputDir = new File(thumbOutputDirPath);
        if (!thumbOutputDir.exists()) {
            thumbOutputDir.mkdirs();
        }

        return thumbOutputDirPath;
    }

    @Override
    public String generateTranscodeResSaveToPath(String saveTo) {
        String path = "%s/%s".formatted(fileConfig.getWorkDir(), saveTo);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path;
    }

    @Override
    public void uploadDirToOss(String dirPath) {
        File folder = new File(dirPath);
        if (!folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles();
        if (Objects.isNull(files)) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                uploadDirToOss(dirPath.concat("/").concat(file.getName()));
                continue;
            }
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                String mime = Files.probeContentType(Path.of(file.getAbsolutePath()));
                String folderPath = dirPath.substring(fileConfig.getWorkDir().length());
                if (folderPath.startsWith("/")) {
                    folderPath = folderPath.substring(1);
                }
                String key = "%s/%s".formatted(folderPath, file.getName());
                uploadToQiniu(fileInputStream, key, null, mime);
            } catch (Exception e) {
                log.error("文件上传失败");
            } finally {
                if (Objects.nonNull(fileInputStream)) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void deleteDirOfWorkSpace(String dirPath) {
        try {
            FileUtil.del(dirPath);
        } catch (Exception e) {
            log.error("文件删除失败");
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> video(String name, Quality quality) {
        Integer qn = quality.getQn();
        String key;
        String saveTo = quality.getSaveTo();
        if (name.contains("stream")) {
            key = "%s/%s".formatted(saveTo, name);
        } else {
            key = "%s/%s.%s".formatted(saveTo, qn, quality.getExt());
        }
        DownloadUrl url = new DownloadUrl(qiniu.getVodDomain(), false, key);
        Auth auth = Auth.create(qiniu.getAccessKey(), qiniu.getSecretKey());
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        try {
            String urlString = url.buildURL(auth, deadline);
            return restTemplate.getForEntity(urlString, InputStreamResource.class);
        } catch (IOException e) {
            log.error("视频资源获取失败,", e);
            throw BusinessException.businessError("图片资源获取失败");
        }
    }

    @Override
    public String filePathRemoveWorkspace(String path) {
        return path.substring(fileConfig.getWorkDir().length());
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

    }

    @Override
    public ResponseEntity<InputStreamResource> cover(String filename) {
        DownloadUrl url = new DownloadUrl(qiniu.getImgDomain(), false, filename);
        Auth auth = Auth.create(qiniu.getAccessKey(), qiniu.getSecretKey());
        long deadline = System.currentTimeMillis() / 1000 + 3600;
        try {
            String urlString = url.buildURL(auth, deadline);
            log.info("获取图片: {}", urlString);
            return restTemplate.getForEntity(urlString, InputStreamResource.class);
        } catch (IOException e) {
            log.error("图片资源获取失败,", e);
            throw BusinessException.businessError("图片资源获取失败");
        }
    }

}
