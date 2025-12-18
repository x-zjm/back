package com.nianji.diary.controller;


import com.nianji.diary.dto.request.TagCreateRequest;
import com.nianji.diary.entity.Tag;
import com.nianji.diary.service.TagService;
import com.nianji.diary.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @PostMapping
    public Tag createTag(@RequestBody @Valid TagCreateRequest request,
                        @RequestAttribute Long userId) {
        return tagService.createTag(request, userId);
    }
    
    @PutMapping("/{id}")
    public void updateTag(@PathVariable Long id,
                         @RequestBody @Valid TagCreateRequest request,
                         @RequestAttribute Long userId) {
        tagService.updateTag(id, request, userId);
    }
    
    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id,
                         @RequestAttribute Long userId) {
        tagService.deleteTag(id, userId);
    }
    
    @GetMapping
    public List<TagVO> getUserTags(@RequestAttribute Long userId) {
        return tagService.getUserTags(userId);
    }
    
    @GetMapping("/search")
    public List<TagVO> searchTags(@RequestParam String keyword,
                                 @RequestAttribute Long userId) {
        return tagService.searchTags(keyword, userId);
    }
}