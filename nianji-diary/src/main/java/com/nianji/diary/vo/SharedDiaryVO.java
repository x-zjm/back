package com.nianji.diary.vo;


import com.nianji.diary.entity.Diary;
import com.nianji.diary.entity.DiaryShare;
import lombok.Data;


@Data
public class SharedDiaryVO {
    private Diary diary;
    private DiaryShare shareInfo;
    private Boolean canCollect;
    private Boolean canReshare;
    private Boolean alreadyFavorited;
}