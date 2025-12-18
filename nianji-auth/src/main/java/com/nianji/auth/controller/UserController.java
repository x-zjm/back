package com.nianji.auth.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    // // private final UserService userService;
    //
    // @GetMapping("/info")
    // public Result<UserInfoVO> getUserInfo(@RequestHeader("X-User-Id") Long userId,
    //                                       @Valid @RequestBody RequestModel<Void> requestModel) {
    //     try {
    //         RequestModel.checkReqModel(requestModel);
    //         return userService.getUserInfo(userId);
    //
    //     } catch (Exception e) {
    //         log.error("获取用户信息失败", e);
    //         return Result.fail(ErrorCode.Server.SERVER_ERROR, e.getMessage());
    //     }
    // }
    //
    // @PutMapping("/info")
    // public Result<Void> updateUserInfo(@RequestHeader("X-User-Id") Long userId,
    //                                    @Valid @RequestBody RequestModel<UserUpdateRequest> requestModel) {
    //     try {
    //         RequestModel.checkReqModel(requestModel);
    //         UserUpdateRequest updateRequest = requestModel.getRequestData();
    //         return userService.updateUserInfo(userId, updateRequest);
    //
    //     } catch (Exception e) {
    //         log.error("更新用户信息失败", e);
    //         return Result.fail(ErrorCode.Server.SERVER_ERROR, e.getMessage());
    //     }
    // }
    //
    // @PutMapping("/password")
    // public Result<Void> updatePassword(@RequestHeader("X-User-Id") Long userId,
    //                                    @Valid @RequestBody RequestModel<PasswordUpdateRequest> requestModel) {
    //     try {
    //         RequestModel.checkReqModel(requestModel);
    //         PasswordUpdateRequest passwordRequest = requestModel.getRequestData();
    //         passwordRequest.validate();
    //         return userService.updatePassword(userId, passwordRequest);
    //
    //     } catch (Exception e) {
    //         log.error("修改密码失败", e);
    //         return Result.fail(ErrorCode.Server.SERVER_ERROR, e.getMessage());
    //     }
    // }
}