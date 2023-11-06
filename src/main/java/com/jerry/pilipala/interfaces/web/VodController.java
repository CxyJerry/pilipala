package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jerry.pilipala.application.dto.PreUploadDTO;
import com.jerry.pilipala.application.dto.VideoPostDTO;
import com.jerry.pilipala.application.vo.PreUploadVO;
import com.jerry.pilipala.application.vo.bvod.BVodVO;
import com.jerry.pilipala.application.vo.vod.InteractionInfoVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.VodThumbnails;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.impl.VodServiceImpl;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/vod")
public class VodController {
    private final VodService vodService;

    public VodController(VodServiceImpl vodService) {
        this.vodService = vodService;
    }

    /**
     * 预上传，用于换取 filename
     *
     * @return filename
     */
    @SaCheckPermission("post-vod")
//    @RateLimiter(key = "vod-limit:upload", seconds = 60, count = 3, message = "上传速度过快，请稍后再试试吧", limitType = LimitType.IP)
    @PostMapping("/pre-upload")
    public CommonResponse<?> preUpload(@Valid @RequestBody PreUploadDTO preUploadDTO) {
        PreUploadVO preUploadVO = vodService.preUpload(preUploadDTO);
        return CommonResponse.success(preUploadVO);
    }


    @GetMapping("/vod/{bvid}")
    public CommonResponse<?> videos(@PathVariable("bvid") String bvid) {
        BVodVO videos = vodService.videos(bvid);
        return CommonResponse.success(videos);
    }

    @GetMapping("/vod/sd/{cid}")
    public CommonResponse<?> sdVideo(@PathVariable("cid") Long cid) {
        BVodVO videos = vodService.sdVideo(cid);
        return CommonResponse.success(videos);
    }

    @GetMapping("/thumbnails/{cid}")
    public CommonResponse<?> thumbnails(@PathVariable("cid") Long cid) {
        VodThumbnails thumbnails = vodService.thumbnails(cid);
        return CommonResponse.success(thumbnails);
    }


    /**
     * 提交稿件
     *
     * @param videoPostDTO 稿件信息
     * @return success
     */
    @SaCheckPermission("post-vod")
    @PostMapping("/post")
    public CommonResponse<?> post(@Valid @RequestBody VideoPostDTO videoPostDTO) {
        vodService.post(videoPostDTO);
        return CommonResponse.success();
    }

    /**
     * 重置转码任务
     *
     * @param taskId 任务ID
     * @return success
     */
    @SaCheckPermission("admin")
    @GetMapping("/action/reset")
    public CommonResponse<?> reset(@RequestParam("task_id") String taskId) {
        vodService.reset(taskId);
        return CommonResponse.success();
    }

    /**
     * 获取自己投递的稿件
     *
     * @param pageNo   分页序号
     * @param pageSize 每页数量
     * @param status   稿件状态
     * @return 一页数据
     */
    @GetMapping("/content/page")
    public CommonResponse<?> page(@RequestParam(value = "uid", required = false) String uid,
                                  @RequestParam(value = "page_no", defaultValue = "1") Integer pageNo,
                                  @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                  @RequestParam(value = "status", defaultValue = "") String status) {
        Page<VodVO> page = vodService.page(uid, pageNo, pageSize, status);
        return CommonResponse.success(page);
    }

    /**
     * 获取需要审核的稿件
     */
    @GetMapping("/review/page")
    public CommonResponse<?> reviewPage(@RequestParam(value = "page_no", defaultValue = "1") Integer pageNo,
                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                        @RequestParam(value = "status", defaultValue = "handing") String status) {
        Page<VodVO> page = vodService.reviewPage(pageNo, pageSize, status);
        return CommonResponse.success(page);
    }

    /**
     * 审批稿件
     *
     * @param cid    cid
     * @param status 状态
     * @return success
     */
//    @Auth(permission = "admin")
    @PutMapping("/content/review")
    public CommonResponse<?> review(@RequestParam("cid") Long cid,
                                    @RequestParam("status") String status) {
        vodService.review(cid, status);
        return CommonResponse.success();
    }

    @PutMapping("/time/{bvId}/{cid}")
    @RateLimiter(key = "vod-limit:update-play-time", seconds = 1, count = 5, message = "更新过快，请稍后重试", limitType = LimitType.IP)
    public void updatePlayTime(@PathVariable("bvId") String bvId,
                               @PathVariable("cid") Long cid,
                               @RequestParam("time") Integer time) {
        vodService.updatePlayTime(bvId, cid, time);
    }

    @PutMapping("/vod/play/{cid}")
    public void updatePlayCount(@PathVariable("cid") Long cid) {
        vodService.updatePlayCount(cid);
    }


    @SaCheckLogin
    @PutMapping("/vod/interactive/put/{name}")
    public void updateInteractive(@PathVariable("name") String actionName,
                                  @RequestParam("cid") Long cid) {
        vodService.interactive(actionName, cid);
    }

    @GetMapping("/vod/interaction/info/{cid}")
    public CommonResponse<InteractionInfoVO> info(@PathVariable("cid") Long cid) {
        InteractionInfoVO interactionInfoVO = vodService.interactionInfo(cid);
        return CommonResponse.success(interactionInfoVO);
    }
}
