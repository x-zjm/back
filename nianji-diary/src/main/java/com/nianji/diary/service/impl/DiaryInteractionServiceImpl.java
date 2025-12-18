package com.nianji.diary.service.impl;


import com.nianji.diary.entity.*;
import com.nianji.diary.dao.mapper.*;
import com.nianji.diary.service.DiaryInteractionService;
import com.nianji.diary.service.DiaryService;
import com.nianji.diary.vo.DiaryShareVO;
import com.nianji.diary.vo.SharedDiaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class DiaryInteractionServiceImpl implements DiaryInteractionService {


    private final DiaryLikeMapper likeMapper;
    private final DiaryFavoriteMapper favoriteMapper;
    private final DiaryShareMapper shareMapper;
    private final DiaryShareAccessMapper shareAccessMapper;
    private final DiaryMapper diaryMapper;
    private final DiaryService diaryService;

    public DiaryInteractionServiceImpl(DiaryLikeMapper likeMapper,
                                       DiaryFavoriteMapper favoriteMapper,
                                       DiaryShareMapper shareMapper,
                                       DiaryShareAccessMapper shareAccessMapper,
                                       DiaryMapper diaryMapper,
                                       DiaryService diaryService) {
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.shareMapper = shareMapper;
        this.shareAccessMapper = shareAccessMapper;
        this.diaryMapper = diaryMapper;
        this.diaryService = diaryService;
    }

    @Override
    @Transactional
    public void likeDiary(Long diaryId, Long userId) {
        DiaryLike existingLike = likeMapper.selectByDiaryAndUser(diaryId, userId);
        if (existingLike != null) {
            throw new RuntimeException("已经点赞过该日记");
        }

        DiaryLike like = new DiaryLike();
        like.setDiaryId(diaryId);
        like.setUserId(userId);
        likeMapper.insert(like);

        diaryMapper.incrementLikeCount(diaryId);
        log.info("点赞日记成功, 日记ID: {}, 用户ID: {}", diaryId, userId);
    }

    @Override
    @Transactional
    public void unlikeDiary(Long diaryId, Long userId) {
        DiaryLike like = likeMapper.selectByDiaryAndUser(diaryId, userId);
        if (like == null) {
            throw new RuntimeException("未点赞过该日记");
        }

        likeMapper.deleteById(like.getId());
        diaryMapper.decrementLikeCount(diaryId);
        log.info("取消点赞成功, 日记ID: {}, 用户ID: {}", diaryId, userId);
    }

    @Override
    public boolean isLiked(Long diaryId, Long userId) {
        return likeMapper.existsByDiaryAndUser(diaryId, userId) > 0;
    }

    @Override
    @Transactional
    public DiaryFavorite favoriteDiary(Long sourceDiaryId, Long userId) {
        // 检查是否已经收藏
        DiaryFavorite existingFavorite = favoriteMapper.selectByUserAndDiary(userId, sourceDiaryId);
        if (existingFavorite != null) {
            throw new RuntimeException("已经收藏过该日记");
        }

        // 获取源日记信息
        Diary sourceDiary = diaryMapper.selectById(sourceDiaryId);
        if (sourceDiary == null) {
            throw new RuntimeException("日记不存在");
        }
        // 创建收藏记录
        DiaryFavorite favorite = new DiaryFavorite();
        favorite.setUserId(userId);
        favorite.setDiaryId(sourceDiaryId);
        favorite.setShareId(0L); // 暂时设置为0，实际应该通过分享ID收藏

        favoriteMapper.insert(favorite);

        // 更新源日记的收藏计数
        diaryMapper.incrementFavoriteCount(sourceDiaryId);

        log.info("收藏日记成功, 源日记ID: {}, 用户ID: {}", sourceDiaryId, userId);
        return favorite;
    }

    @Override
    @Transactional
    public void unfavoriteDiary(Long sourceDiaryId, Long userId) {
        DiaryFavorite favorite = favoriteMapper.selectByUserAndDiary(userId, sourceDiaryId);
        if (favorite == null) {
            throw new RuntimeException("未收藏该日记");
        }

        favoriteMapper.deleteById(favorite.getId());
        diaryMapper.decrementFavoriteCount(sourceDiaryId);
        log.info("取消收藏成功, 源日记ID: {}, 用户ID: {}", sourceDiaryId, userId);
    }

    @Override
    public List<DiaryFavorite> getUserFavorites(Long userId) {
        return favoriteMapper.selectByUserIdWithDiary(userId);
    }

    @Override
    public boolean isFavorited(Long sourceDiaryId, Long userId) {
        DiaryFavorite favorite = favoriteMapper.selectByUserAndDiary(userId, sourceDiaryId);
        return favorite != null;
    }

    @Override
    @Transactional
    public DiaryShare shareDiary(Long diaryId, Long userId, Boolean allowCollect, Integer expireDays) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new RuntimeException("日记不存在或无权分享");
        }

        String shareCode = generateShareCode();
        LocalDateTime expireTime = null;
        if (expireDays != null && expireDays > 0) {
            expireTime = LocalDateTime.now().plusDays(expireDays);
        }

        DiaryShare share = new DiaryShare();
        share.setDiaryId(diaryId);
        share.setOwnerUserId(userId);
        share.setSharerUserId(userId);
        share.setShareCode(shareCode);
        share.setAllowCollect(allowCollect != null ? allowCollect : true);
        share.setAllowReshare(true);
        share.setParentShareId(null);
        share.setExpireTime(expireTime);

        shareMapper.insert(share);
        diaryMapper.incrementShareCount(diaryId);

        log.info("分享日记成功, 日记ID: {}, 分享码: {}, 用户ID: {}", diaryId, shareCode, userId);
        return share;
    }

    @Override
    public DiaryShareVO getShareByCode(String shareCode, Long receiverUserId) {
        DiaryShare share = shareMapper.selectValidByShareCode(shareCode);
        if (share == null) {
            throw new RuntimeException("分享不存在或已过期");
        }

        // 记录接收信息
        if (receiverUserId != null && !share.getSharerUserId().equals(receiverUserId)) {
            recordShareAccess(share.getId(), receiverUserId);
        }

        Diary diary = diaryMapper.selectById(share.getDiaryId());
        DiaryShareVO shareVO = new DiaryShareVO();
        shareVO.setShare(share);
        shareVO.setDiary(diary);

        return shareVO;
    }

    @Override
    public SharedDiaryVO viewSharedDiary(String shareCode, Long receiverUserId) {
        DiaryShare share = shareMapper.selectValidByShareCode(shareCode);
        if (share == null) {
            throw new RuntimeException("分享不存在或已过期");
        }

        // 记录接收信息
        if (receiverUserId != null && !share.getSharerUserId().equals(receiverUserId)) {
            recordShareAccess(share.getId(), receiverUserId);
        }

        // 获取日记详情
        SharedDiaryVO sharedDiary = new SharedDiaryVO();
        sharedDiary.setDiary(diaryMapper.selectById(share.getDiaryId()));
        sharedDiary.setShareInfo(share);
        sharedDiary.setCanCollect(share.getAllowCollect());
        sharedDiary.setCanReshare(share.getAllowReshare());
        sharedDiary.setAlreadyFavorited(receiverUserId != null ?
                isFavorited(share.getDiaryId(), receiverUserId) : false);

        return sharedDiary;
    }

    @Override
    public List<DiaryShare> getUserShares(Long userId) {
        return shareMapper.selectBySharerUserId(userId);
    }

    @Override
    @Transactional
    public void cancelShare(Long shareId, Long userId) {
        DiaryShare share = shareMapper.selectById(shareId);
        if (share == null) {
            throw new RuntimeException("分享记录不存在");
        }

        // 只有分享者或日记所有者可以取消分享
        if (!share.getSharerUserId().equals(userId) && !share.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("无权取消该分享");
        }

        shareMapper.deleteById(shareId);
        log.info("取消分享成功, 分享ID: {}, 用户ID: {}", shareId, userId);
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void recordShareAccess(Long shareId, Long accessUserId) {
        DiaryShareAccess access = new DiaryShareAccess();
        access.setShareId(shareId);
        access.setAccessUserId(accessUserId);
        shareAccessMapper.insert(access);
    }
}