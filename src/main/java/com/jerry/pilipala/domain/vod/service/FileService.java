package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.domain.vod.entity.mongo.distribute.Quality;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传
     *
     * @param file 文件
     * @return 文件地址
     */
    String uploadCover(MultipartFile file);

    /**
     * 上传视频
     *
     * @param file 视频文件
     * @param cid  稿件ID
     */
    void uploadVideo(MultipartFile file, Long cid);

    /**
     * 解析后缀名
     *
     * @param file 文件
     * @return 后缀名
     */
    String parseExt(MultipartFile file);

    /**
     * 保存文件
     *
     * @param filepath 文件路径
     * @param file     文件
     */
    void saveFile(String filepath, MultipartFile file);

    String downloadVideo(String filename, String ext);

    void deleteVideoOfWorkSpace(String filename, String ext);

    String generateThumbnailsDirPath(String filename);

    String generateTranscodeResSaveToPath(String saveTo);

    String filePathRemoveWorkspace(String path);

    void uploadDirToOss(String dirPath);

    void deleteDirOfWorkSpace(String dirPath);

    ResponseEntity<InputStreamResource> video(String name, Quality quality);
    /**
     * 下载文件
     *
     * @param filename 文件名
     * @return
     */
    ResponseEntity<InputStreamResource> cover(String filename);
}
