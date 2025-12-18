package com.nianji.diary.controller;


import com.nianji.diary.entity.DiaryFavorite;
import com.nianji.diary.entity.DiaryShare;
import com.nianji.diary.service.DiaryInteractionService;
import com.nianji.diary.vo.DiaryShareVO;
import com.nianji.diary.vo.SharedDiaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@Slf4j
@RestController
@RequestMapping("/diaries/interaction")
public class DiaryInteractionController {


    // private final DiaryInteractionService interactionService;
    //
    // public DiaryInteractionController(DiaryInteractionService interactionService) {
    //     this.interactionService = interactionService;
    // }
    //
    // // 点赞相关接口
    // @PostMapping("/{diaryId}/like")
    // public Result<Void> likeDiary(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         interactionService.likeDiary(diaryId, userId);
    //         return Result.success();
    //     } catch (Exception e) {
    //         log.error("点赞失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @DeleteMapping("/{diaryId}/like")
    // public Result<Void> unlikeDiary(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         interactionService.unlikeDiary(diaryId, userId);
    //         return Result.success();
    //     } catch (Exception e) {
    //         log.error("取消点赞失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/{diaryId}/like/status")
    // public Result<Boolean> getLikeStatus(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         boolean isLiked = interactionService.isLiked(diaryId, userId);
    //         return Result.success(isLiked);
    //     } catch (Exception e) {
    //         log.error("获取点赞状态失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // // 收藏相关接口
    // @PostMapping("/{diaryId}/favorite")
    // public Result<DiaryFavorite> favoriteDiary(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         DiaryFavorite favorite = interactionService.favoriteDiary(diaryId, userId);
    //         return Result.success(favorite);
    //     } catch (Exception e) {
    //         log.error("收藏失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @DeleteMapping("/{diaryId}/favorite")
    // public Result<Void> unfavoriteDiary(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         interactionService.unfavoriteDiary(diaryId, userId);
    //         return Result.success();
    //     } catch (Exception e) {
    //         log.error("取消收藏失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/favorites")
    // public Result<List<DiaryFavorite>> getUserFavorites(@RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         List<DiaryFavorite> favorites = interactionService.getUserFavorites(userId);
    //         return Result.success(favorites);
    //     } catch (Exception e) {
    //         log.error("获取收藏列表失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/{diaryId}/favorite-status")
    // public Result<Boolean> getFavoriteStatus(
    //         @PathVariable Long diaryId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         boolean isFavorited = interactionService.isFavorited(diaryId, userId);
    //         return Result.success(isFavorited);
    //     } catch (Exception e) {
    //         log.error("获取收藏状态失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // // 分享相关接口
    // @PostMapping("/{diaryId}/share")
    // public Result<DiaryShare> shareDiary(
    //         @PathVariable Long diaryId,
    //         @RequestParam(required = false) Boolean allowCollect,
    //         @RequestParam(required = false) Integer expireDays,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         DiaryShare share = interactionService.shareDiary(diaryId, userId, allowCollect, expireDays);
    //         return Result.success(share);
    //     } catch (Exception e) {
    //         log.error("分享失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/share/{shareCode}")
    // public Result<DiaryShareVO> getShareByCode(
    //         @PathVariable String shareCode,
    //         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
    //     try {
    //         DiaryShareVO shareVO = interactionService.getShareByCode(shareCode, userId);
    //         return Result.success(shareVO);
    //     } catch (Exception e) {
    //         log.error("获取分享信息失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/share/{shareCode}/view")
    // public Result<SharedDiaryVO> viewSharedDiary(
    //         @PathVariable String shareCode,
    //         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
    //     try {
    //         SharedDiaryVO sharedDiary = interactionService.viewSharedDiary(shareCode, userId);
    //         return Result.success(sharedDiary);
    //     } catch (Exception e) {
    //         log.error("查看分享日记失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @GetMapping("/shares")
    // public Result<List<DiaryShare>> getUserShares(@RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         List<DiaryShare> shares = interactionService.getUserShares(userId);
    //         return Result.success(shares);
    //     } catch (Exception e) {
    //         log.error("获取分享列表失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
    //
    // @DeleteMapping("/shares/{shareId}")
    // public Result<Void> cancelShare(
    //         @PathVariable Long shareId,
    //         @RequestHeader("X-User-Id") Long userId) {
    //     try {
    //         interactionService.cancelShare(shareId, userId);
    //         return Result.success();
    //     } catch (Exception e) {
    //         log.error("取消分享失败: {}", e.getMessage());
    //         return Result.error(e.getMessage());
    //     }
    // }
}