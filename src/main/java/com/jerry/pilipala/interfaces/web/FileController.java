package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jerry.pilipala.application.vo.vod.UploadVO;
import com.jerry.pilipala.domain.vod.service.FileService;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.impl.FileServiceImpl;
import com.jerry.pilipala.domain.vod.service.impl.VodServiceImpl;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.config.Qiniu;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;
    private final VodService vodService;
    private final Qiniu qiniu;
    private final HttpServletResponse response;

    public FileController(FileServiceImpl fileService,
                          VodServiceImpl vodService,
                          Qiniu qiniu, HttpServletResponse response) {
        this.fileService = fileService;
        this.vodService = vodService;
        this.qiniu = qiniu;
        this.response = response;
    }

    /**
     * 正式上传文件
     *
     * @param cid 预上传生产的稿件id
     * @return success
     */
    @Operation(summary = "上传视频文件")
    @SaCheckPermission("post-vod")
    @RateLimiter(key = "vod-limit:upload", count = 3, message = "上传速度过快，请稍后再试试吧", limitType = LimitType.IP)
    @GetMapping("/upload")
    public CommonResponse<?> upload(@RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        UploadVO uploadVO = fileService.uploadVideo(cid);
        return CommonResponse.success(uploadVO);
    }

    @Operation(summary = "上传视频完成")
    @RequestMapping(method = RequestMethod.HEAD, value = "/upload/completed")
    public void uploadCompleted(@RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        fileService.uploadCompleted(cid);
    }

//    @Operation(summary = "分片上传视频")
//    @SaCheckPermission("post-vod")
//    @PostMapping("/upload-chunk")
//    public CommonResponse<?> uploadChunk(@RequestParam("chunk") @NotNull(message = "视频内容不得为空") MultipartFile video,
//                                         @RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
//        fileService.uploadVideo(video, cid);
//        return CommonResponse.success();
//    }

    /**
     * 上传封面
     *
     * @param cover 封面文件
     * @return 文件地址
     */
    @Operation(summary = "上传视频封面")
    @SaCheckPermission("post-vod")
    @RateLimiter(key = "vod-limit:upload-cover", count = 3, message = "上传速度过快，请稍后再试试吧", limitType = LimitType.IP)
    @PostMapping("/upload-cover")
    public CommonResponse<?> uploadCover(@RequestParam("cover") @NotNull(message = "文件不得为空") MultipartFile cover) {
        String uploadFilename = fileService.uploadCover(cover);
        return CommonResponse.success("file/cover/" + uploadFilename);
    }

    /**
     * 获取视频文件
     *
     * @param cid    稿件ID
     * @param format 稿件清晰度规格
     * @param name   文件名
     */
    @Operation(summary = "获取视频文件")
    @GetMapping("/video/{cid}/{format}/{name}")
    public ResponseEntity<InputStreamResource> video(@PathVariable("cid") @NotNull(message = "稿件ID不得为空") Long cid,
                                                     @PathVariable("format") @NotBlank(message = "清晰度不得为空") String format,
                                                     @PathVariable("name") @NotBlank(message = "文件名不得为空") String name) {
        return vodService.video(cid, format, name);
    }

    /**
     * 获取封面文件
     *
     * @param filename 文件地址
     */
    @Operation(summary = "获取视频封面")
    @GetMapping("/cover/{filename}")
    public ResponseEntity<InputStreamResource> cover(@PathVariable("filename") @NotBlank(message = "稿件名称不得为空") String filename) {
        return fileService.cover(filename);
    }
}
