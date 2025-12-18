package com.nianji.diary.vo;


import lombok.Data;


@Data
public class ShareRequest {
    private Boolean allowCollect = true;
    private Boolean allowReshare = true;
    private Long parentShareId;
    private Integer expireDays;
}