package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jerry.pilipala.domain.vod.service.FileService;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.impl.FileServiceImpl;
import com.jerry.pilipala.domain.vod.service.impl.VodServiceImpl;
import com.jerry.pilipala.infrastructure.annotations.FileType;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;
    private final VodService vodService;

    public FileController(FileServiceImpl fileService, VodServiceImpl vodService) {
        this.fileService = fileService;
        this.vodService = vodService;
    }

    /**
     * 正式上传文件
     *
     * @param video 视频文件
     * @param cid   预上传生产的稿件id
     * @return success
     */
    @SaCheckPermission("post-vod")
//    @RateLimiter(key = "vod-limit:upload", seconds = 60, count = 3, message = "上传速度过快，请稍后再试试吧", limitType = LimitType.IP)
    @PostMapping("/upload")
    public CommonResponse<?> upload(@RequestParam("video") @NotNull(message = "请选择视频文件") MultipartFile video,
                                    @RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        fileService.uploadVideo(video, cid);
        return CommonResponse.success();
    }

    /**
     * 上传封面
     *
     * @param cover 封面文件
     * @return 文件地址
     */
    @FileType(types = {"png", "jpg"})
//    @Auth(permission = "post-vod")
    @PostMapping("/upload-cover")
    public CommonResponse<?> uploadCover(@RequestParam("cover") @NotNull(message = "文件不得为空") MultipartFile cover) {
        String uploadFilename = fileService.upload(cover);
        return CommonResponse.success("file/cover/" + uploadFilename);
    }

    @GetMapping("/video/{cid}/{format}/{name}")
    @RateLimiter(key = "vod-limit:video-play", seconds = 1, count = 30, limitType = LimitType.IP)
    public void video(@PathVariable("cid") Long cid,
                      @PathVariable("format") String format,
                      @PathVariable("name") String name) {
        vodService.video(cid, format, name);
    }

    /**
     * 获取封面文件
     *
     * @param filename 文件地址
     */
    @GetMapping("/cover/{filename}")
    public void cover(@PathVariable("filename") String filename) {
        fileService.download(filename);
    }
}
