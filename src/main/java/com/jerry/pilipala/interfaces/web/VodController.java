package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.google.common.collect.Maps;
import com.jerry.pilipala.application.dto.PreUploadDTO;
import com.jerry.pilipala.application.dto.VideoPostDTO;
import com.jerry.pilipala.application.vo.bvod.BVodVO;
import com.jerry.pilipala.application.vo.vod.InteractionInfoVO;
import com.jerry.pilipala.application.vo.vod.PreUploadVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.interactive.handler.InteractiveActionStrategy;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.entity.mongo.thumbnails.VodThumbnails;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.service.impl.VodServiceImpl;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
@Validated
@RestController
@RequestMapping("/vod")
public class VodController {
    private final VodService vodService;
    private final InteractiveActionStrategy interactiveActionStrategy;

    public VodController(VodServiceImpl vodService, InteractiveActionStrategy interactiveActionStrategy) {
        this.vodService = vodService;
        this.interactiveActionStrategy = interactiveActionStrategy;
    }

    /**
     * 预上传，用于换取 filename
     *
     * @return filename
     */
    @ApiOperation("预上传稿件")
    @SaCheckPermission("post-vod")
    @RateLimiter(key = "vod-limit:upload", seconds = 60, count = 3, message = "上传速度过快，请稍后再试试吧", limitType = LimitType.IP)
    @PostMapping("/pre-upload")
    public CommonResponse<?> preUpload(@Valid @RequestBody PreUploadDTO preUploadDTO) {
        PreUploadVO preUploadVO = vodService.preUpload(preUploadDTO);
        return CommonResponse.success(preUploadVO);
    }


    /**
     * 获取BVID下可播放稿件全部清晰度
     *
     * @param bvid bvid
     * @return bvod
     */
    @ApiOperation("获取BVID下可播放稿件全部清晰度")
    @GetMapping("/vod/{bvid}")
    public CommonResponse<?> videos(@PathVariable("bvid") @NotBlank(message = "BVID不得为空") String bvid, @RequestParam(value = "cid", required = false) Long cid) {
        BVodVO videos = vodService.videos(bvid, cid);
        if (Objects.isNull(cid)) {
            cid = videos.getVodList().get(0).getCid();
        }
        HashMap<@Nullable String, @Nullable Object> params = Maps.newHashMap();
        params.put("cid", cid);
        // 新增互动数据
        VodInteractiveAction playAction;
        try {
            playAction = interactiveActionStrategy.trigger(VodInteractiveActionEnum.PLAY, params).get();
        } catch (Exception e) {
            log.error("play action id failed to generate,cause ", e);
            throw BusinessException.businessError("play action id 生成失败");
        }
        videos.setActionId(playAction.getId());
        return CommonResponse.success(videos);
    }

    /**
     * 获取低清（360P）预览视频 - 审核用
     *
     * @param cid 稿件ID
     * @return bvod
     */
    @ApiOperation("获取低清（360P）预览视频 - 审核用")
    @GetMapping("/vod/sd/{cid}")
    public CommonResponse<?> sdVideo(@PathVariable("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        BVodVO videos = vodService.sdVideo(cid);
        return CommonResponse.success(videos);
    }

    /**
     * 获取缩略图
     *
     * @param cid 稿件ID
     * @return thumbnails
     */
    @ApiOperation("获取缩略图")
    @GetMapping("/thumbnails/{cid}")
    public CommonResponse<?> thumbnails(@PathVariable("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        VodThumbnails thumbnails = vodService.thumbnails(cid);
        return CommonResponse.success(thumbnails);
    }


    /**
     * 提交稿件
     *
     * @param videoPostDTO 稿件信息
     * @return success
     */
    @ApiOperation("提交稿件")
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
    @ApiOperation("重置转码任务")
    @GetMapping("/action/reset")
    public CommonResponse<?> reset(@RequestParam("task_id") @NotBlank(message = "任务ID不得为空") String taskId) {
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
    @ApiOperation("获取自己投递的稿件")
    @GetMapping("/content/page")
    public CommonResponse<?> page(@RequestParam(value = "uid", required = false) String uid, @RequestParam(value = "page_no", defaultValue = "1") @Min(value = 1, message = "最小1") @Max(value = 1000, message = "最大1000") Integer pageNo, @RequestParam(value = "page_size", defaultValue = "10") @Min(value = 1, message = "最小1") @Max(value = 1000, message = "最大1000") Integer pageSize, @RequestParam(value = "status", defaultValue = "") String status) {
        Page<VodVO> page = vodService.page(uid, pageNo, pageSize, status);
        return CommonResponse.success(page);
    }

    /**
     * 获取需要审核的稿件
     */
    @ApiOperation("获取需要审核的稿件")
    @SaCheckPermission("review-vod")
    @GetMapping("/review/page")
    public CommonResponse<?> reviewPage(@RequestParam(value = "page_no", defaultValue = "1") @Min(value = 1, message = "最小1") @Max(value = 1000, message = "最大1000") Integer pageNo, @RequestParam(value = "page_size", defaultValue = "10") @Min(value = 1, message = "最小1") @Max(value = 1000, message = "最大1000") Integer pageSize, @RequestParam(value = "status", defaultValue = "handing") String status) {
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
    @ApiOperation("审核稿件")
    @SaCheckPermission("review-vod")
    @PutMapping("/content/review")
    public CommonResponse<?> review(@RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid, @RequestParam("status") @NotBlank(message = "审核状态不得为空") String status) {
        vodService.review(cid, status);
        return CommonResponse.success();
    }

    /**
     * 更新用户播放到的时间
     *
     * @param bvId BVID
     * @param cid  CID
     * @param time 时间
     */
    @ApiOperation("更新用户播放到的时间")
    @PutMapping("/time/{bvId}/{cid}")
    public void updatePlayTime(@PathVariable("bvId") @NotBlank(message = "BVID不得为空") String bvId, @PathVariable("cid") @NotNull(message = "稿件ID不得为空") Long cid, @RequestParam("time") @NotNull(message = "时间不得为空") @Min(value = 0, message = "时间至少是0") Integer time, @RequestParam(value = "play_action_id", required = false) String playActionId) {
        vodService.updatePlayTime(bvId, cid, time);
        // 新增互动数据
        HashMap<@Nullable String, @Nullable Object> params = Maps.newHashMap();
        params.put("cid", cid);
        params.put("play_action_id", playActionId);
        interactiveActionStrategy.trigger(VodInteractiveActionEnum.UPDATE_TIME, params);

    }


    /**
     * 更新互动数据
     *
     * @param actionName 互动动作
     * @param cid        稿件ID
     */
    @ApiOperation("更新视频互动数据")
    @SaCheckLogin
    @PutMapping("/vod/interactive/put/{name}")
    public void updateInteractive(@PathVariable("name") @NotBlank(message = "互动动作丢失") String actionName, @RequestParam("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        VodInteractiveActionEnum action = VodInteractiveActionEnum.parse(actionName);
        HashMap<@Nullable String, @Nullable Object> params = Maps.newHashMap();
        params.put("cid", cid);
        try {
            interactiveActionStrategy.trigger(action, params).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取互动信息
     *
     * @param cid 稿件ID
     * @return vo
     */
    @ApiOperation("获取互动信息")
    @GetMapping("/vod/interaction/info/{cid}")
    public CommonResponse<InteractionInfoVO> info(@PathVariable("cid") @NotNull(message = "稿件ID不得为空") Long cid) {
        InteractionInfoVO interactionInfoVO = vodService.interactionInfo(cid);
        return CommonResponse.success(interactionInfoVO);
    }

    /**
     * 用户离开视频
     */
    @ApiOperation("离开视频")
    @GetMapping("/leave")
    public void leave() {
        interactiveActionStrategy.trigger(VodInteractiveActionEnum.LEAVE, Maps.newHashMap());
    }
}
