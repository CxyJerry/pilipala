package com.jerry.pilipala.domain.vod.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传
     *
     * @param file 文件
     * @return 文件地址
     */
    String upload(MultipartFile file);

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

    /**
     * 下载文件
     *
     * @param filename 文件名
     */
    void download(String filename);
}
