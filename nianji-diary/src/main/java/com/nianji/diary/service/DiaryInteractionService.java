package com.nianji.diary.service;


import com.nianji.diary.entity.DiaryFavorite;
import com.nianji.diary.entity.DiaryShare;
import com.nianji.diary.vo.DiaryShareVO;
import com.nianji.diary.vo.SharedDiaryVO;


import java.util.List;


public interface DiaryInteractionService {
    
    // 点赞相关
    void likeDiary(Long diaryId, Long userId);
    
    void unlikeDiary(Long diaryId, Long userId);
    
    boolean isLiked(Long diaryId, Long userId);
    
    // 收藏相关
    DiaryFavorite favoriteDiary(Long sourceDiaryId, Long userId);
    
    void unfavoriteDiary(Long sourceDiaryId, Long userId);
    
    List<DiaryFavorite> getUserFavorites(Long userId);
    
    boolean isFavorited(Long sourceDiaryId, Long userId);
    
    // 分享相关
    DiaryShare shareDiary(Long diaryId, Long userId, Boolean allowCollect, Integer expireDays);
    
    DiaryShareVO getShareByCode(String shareCode, Long receiverUserId);
    
    SharedDiaryVO viewSharedDiary(String shareCode, Long receiverUserId);
    
    List<DiaryShare> getUserShares(Long userId);
    
    void cancelShare(Long shareId, Long userId);
}