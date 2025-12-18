package com.nianji.diary.vo;


import com.nianji.diary.entity.Diary;
import com.nianji.diary.entity.DiaryAttachment;
import com.nianji.diary.entity.DiaryTag;
import lombok.Data;

import java.util.List;


@Data
public class DiaryVO {
    private Diary diary;
    private List<DiaryAttachment> attachments;
    private List<DiaryTag> tags;
    private Boolean isOwner;
    private Long favoriteId;
}