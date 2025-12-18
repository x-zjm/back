package com.nianji.diary.vo;


import com.nianji.diary.entity.Diary;
import com.nianji.diary.entity.DiaryShare;
import lombok.Data;


@Data
public class DiaryShareVO {
    private DiaryShare share;
    private Diary diary;
}