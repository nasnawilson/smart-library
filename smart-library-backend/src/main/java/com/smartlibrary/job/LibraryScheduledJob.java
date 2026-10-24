package com.smartlibrary.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartlibrary.entity.Seat;
import com.smartlibrary.entity.SeatReservation;
import com.smartlibrary.entity.User;
import com.smartlibrary.service.SeatReservationService;
import com.smartlibrary.service.SeatService;
import com.smartlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书馆预约定时任务。
 *
 * 规则：
 * 1. 预约创建后 15 分钟未签到，预约状态改为超时未签，释放座位。
 * 2. 临时离开超过 15 分钟，预约状态改为超时未归，释放座位。
 * 3. 用户累计违规 3 次后，禁用预约权限 7 天。
 */
@Slf4j
@Component
public class LibraryScheduledJob {

    @Autowired
    private SeatReservationService seatReservationService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private UserService userService;

    /**
     * 每 1 分钟检查签到超时。
     *
     * 状态说明：
     * 0 = 待签到
     * 1 = 已使用
     * 2 = 超时未签/违规
     * 3 = 临时离开
     */
    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void checkSignTimeout() {
        log.info("开始检查预约签到超时...");
        try {
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);
            List<SeatReservation> timeoutList = seatReservationService.list(
                    new QueryWrapper<SeatReservation>()
                            .eq("status", 0)
                            .lt("create_time", deadline)
            );

            for (SeatReservation record : timeoutList) {
                handleViolationAndRelease(record);
            }

            log.info("预约签到超时检查完成，本次处理 {} 条", timeoutList.size());
        } catch (Exception e) {
            log.error("预约签到超时检查失败", e);
        }
    }

    /**
     * 每 1 分钟检查临时离开超时。
     *
     * 临时离开最长 15 分钟，超时后释放座位并计入违规。
     */
    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void checkLeaveTimeout() {
        log.info("开始检查暂离超时...");
        try {
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);
            List<SeatReservation> timeoutList = seatReservationService.list(
                    new QueryWrapper<SeatReservation>()
                            .eq("status", 3)
                            .lt("leave_time", deadline)
            );

            for (SeatReservation record : timeoutList) {
                handleViolationAndRelease(record);
            }

            log.info("暂离超时检查完成，本次处理 {} 条", timeoutList.size());
        } catch (Exception e) {
            log.error("暂离超时检查失败", e);
        }
    }

    /**
     * 将预约标记为违规、释放座位、累计用户违规次数。
     */
    private void handleViolationAndRelease(SeatReservation record) {
        record.setStatusCode(2);
        seatReservationService.updateById(record);

        Seat seat = seatService.getById(record.getSeatId());
        if (seat != null) {
            seat.setStatusCode(0);
            seatService.updateById(seat);
        }

        User user = userService.getById(record.getUserId());
        if (user != null) {
            int violationCount = user.getViolationCount() == null ? 0 : user.getViolationCount();
            user.setViolationCount(violationCount + 1);
            if (user.getViolationCount() >= 3) {
                user.setForbidTime(LocalDateTime.now().plusDays(7));
            }
            userService.updateById(user);
        }
    }
}
