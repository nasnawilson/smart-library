package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.SeatReserveDTO;
import com.smartlibrary.entity.Seat;
import com.smartlibrary.entity.SeatReservation;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.SeatReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatReservationService extends ServiceImpl<SeatReservationMapper, SeatReservation> {
    @Autowired private SeatService seatService;
    @Autowired private UserService userService;

    @Transactional
    public void reserve(SeatReserveDTO dto, Long userId) {
        if (dto.getReservationDate().isBefore(LocalDate.now())) throw new BusinessException("不能预约过去的日期");
        User user = userService.getById(userId);
        if (user != null && user.getForbidTime() != null && user.getForbidTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("预约权限已被禁用，请于 " + user.getForbidTime() + " 后再预约");
        }
        ensureUserHasNoActiveSeat(userId);
        Seat seat = seatService.getById(dto.getSeatId());
        if (seat == null) throw new BusinessException("座位不存在");

        int[] range = resolveHourRange(dto);
        int startHour = range[0];
        int endHour = range[1];
        String timePeriod = formatRange(startHour, endHour);

        // 查询当日该座位所有未结束的预约，逐一判断时间区间重叠
        List<SeatReservation> sameDay = list(new QueryWrapper<SeatReservation>()
                .eq("seat_id", dto.getSeatId())
                .eq("reserve_date", dto.getReservationDate())
                .in("status", 0, 1, 3));
        for (SeatReservation rec : sameDay) {
            int[] existing = parsePeriod(rec.getTimeSlot());
            if (existing == null) continue;
            if (overlap(startHour, endHour, existing[0], existing[1])) {
                throw new BusinessException("该座位在 " + formatRange(existing[0], existing[1]) + " 已被预约，请重新选择时段");
            }
        }

        SeatReservation record = new SeatReservation();
        record.setUserId(userId);
        record.setSeatId(dto.getSeatId());
        record.setReservationDate(dto.getReservationDate());
        record.setTimeSlot(timePeriod);
        record.setStatusCode(0);
        record.setCreateTime(LocalDateTime.now());
        save(record);
    }

    private int[] resolveHourRange(SeatReserveDTO dto) {
        if (dto.getStartHour() != null && dto.getEndHour() != null) {
            int s = dto.getStartHour();
            int e = dto.getEndHour();
            if (s < 0 || s > 23) throw new BusinessException("开始时间必须在 00:00 - 23:00 之间");
            if (e < 1 || e > 24) throw new BusinessException("结束时间必须在 01:00 - 24:00 之间");
            if (e <= s) throw new BusinessException("结束时间必须晚于开始时间");
            return new int[]{s, e};
        }
        if (dto.getTimeSlot() != null) {
            switch (dto.getTimeSlot()) {
                case "MORNING": case "上午": return new int[]{8, 12};
                case "AFTERNOON": case "下午": return new int[]{14, 18};
                case "EVENING": case "晚上": return new int[]{19, 22};
                default:
                    int[] parsed = parsePeriod(dto.getTimeSlot());
                    if (parsed != null) return parsed;
            }
        }
        throw new BusinessException("请选择预约时段");
    }

    private String formatRange(int start, int end) {
        return String.format("%02d:00-%02d:00", start, end);
    }

    private int[] parsePeriod(String period) {
        if (period == null) return null;
        // 兼容旧格式
        switch (period) {
            case "MORNING": case "上午": return new int[]{8, 12};
            case "AFTERNOON": case "下午": return new int[]{14, 18};
            case "EVENING": case "晚上": return new int[]{19, 22};
        }
        try {
            String[] parts = period.split("-");
            if (parts.length != 2) return null;
            int s = Integer.parseInt(parts[0].split(":")[0]);
            int e = Integer.parseInt(parts[1].split(":")[0]);
            return new int[]{s, e};
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean overlap(int s1, int e1, int s2, int e2) {
        return s1 < e2 && s2 < e1;
    }

    /**
     * 现场入座：创建一条使用中记录，并占用座位。
     */
    @Transactional
    public void occupyNow(Long seatId, Long userId) {
        ensureUserHasNoActiveSeat(userId);
        Seat seat = seatService.getById(seatId);
        if (seat == null) throw new BusinessException("座位不存在");
        if (seat.getStatusCode() != null && seat.getStatusCode() != 0) throw new BusinessException("座位不可用");

        SeatReservation record = new SeatReservation();
        record.setUserId(userId);
        record.setSeatId(seatId);
        record.setReservationDate(LocalDate.now());
        record.setTimeSlot(getCurrentHourRange());
        record.setCheckInTime(LocalDateTime.now());
        record.setStatusCode(1);
        record.setCreateTime(LocalDateTime.now());
        save(record);

        seatService.occupySeat(seatId, userId);
    }

    public void checkIn(Long id, Long userId) {
        SeatReservation record = validateMine(id, userId);
        ensureUserHasNoActiveSeat(userId, id);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatusCode(1);
        updateById(record);
        seatService.occupySeat(record.getSeatId(), userId);
    }

    @Transactional
    public void cancel(Long id, Long userId) {
        SeatReservation record = validateMine(id, userId);
        if (record.getStatusCode() != null && (record.getStatusCode() == 1 || record.getStatusCode() == 3)) {
            seatService.releaseSeat(record.getSeatId());
        }
        removeById(id);
    }

    @Transactional
    public void complete(Long id, Long userId) {
        SeatReservation record = validateMine(id, userId);
        record.setStatusCode(4);
        record.setLeaveTime(null);
        updateById(record);
        seatService.releaseSeat(record.getSeatId());
    }

    /**
     * 释放当前用户正在使用或暂离的座位。
     */
    @Transactional
    public void releaseActiveSeat(Long seatId, Long userId, String role) {
        SeatReservation record = getOperableReservation(seatId, userId, role);
        if (record == null) throw new BusinessException("当前用户未使用该座位");
        record.setStatusCode(4);
        record.setLeaveTime(null);
        updateById(record);
        seatService.releaseSeat(seatId);
    }

    /**
     * 根据用户和座位标记当前预约为临时离开。
     */
    public void markTempLeave(Long seatId, Long userId, String role) {
        SeatReservation record = getOperableReservation(seatId, userId, role);
        if (record == null || record.getStatusCode() == null || record.getStatusCode() != 1) {
            throw new BusinessException("当前用户未入座该座位，不能暂离");
        }
        record.setStatusCode(3);
        record.setLeaveTime(LocalDateTime.now());
        updateById(record);
    }

    /**
     * 根据用户和座位标记临时离开归来。
     */
    public void markReturnFromTempLeave(Long seatId, Long userId, String role) {
        SeatReservation record = getOperableReservation(seatId, userId, role);
        if (record == null || record.getStatusCode() == null || record.getStatusCode() != 3) {
            throw new BusinessException("当前用户没有暂离该座位");
        }
        record.setStatusCode(1);
        record.setLeaveTime(null);
        updateById(record);
    }

    public List<SeatReservation> getUserReservations(Long userId) {
        return list(new QueryWrapper<SeatReservation>().eq("user_id", userId).orderByDesc("reserve_date")).stream().peek(this::fillView).collect(Collectors.toList());
    }

    public void checkOverdueReservations() {
    }

    private void ensureUserHasNoActiveSeat(Long userId) {
        ensureUserHasNoActiveSeat(userId, null);
    }

    private void ensureUserHasNoActiveSeat(Long userId, Long excludeReservationId) {
        QueryWrapper<SeatReservation> wrapper = new QueryWrapper<SeatReservation>()
                .eq("user_id", userId)
                .in("status", 0, 1, 3);
        if (excludeReservationId != null) {
            wrapper.ne("id", excludeReservationId);
        }
        long activeCount = count(wrapper);
        if (activeCount > 0) {
            throw new BusinessException("每位用户同一时间只能预约或入座一个座位，请先结束当前座位使用");
        }
    }

    private SeatReservation getActiveReservation(Long seatId, Long userId) {
        return getOne(new QueryWrapper<SeatReservation>()
                .eq("seat_id", seatId)
                .eq("user_id", userId)
                .in("status", 1, 3)
                .orderByDesc("reserve_date")
                .last("LIMIT 1"), false);
    }

    private SeatReservation getOperableReservation(Long seatId, Long userId, String role) {
        QueryWrapper<SeatReservation> wrapper = new QueryWrapper<SeatReservation>()
                .eq("seat_id", seatId)
                .in("status", 1, 3)
                .orderByDesc("reserve_date")
                .last("LIMIT 1");
        if (!"ADMIN".equals(role)) {
            wrapper.eq("user_id", userId);
        }
        return getOne(wrapper, false);
    }

    private SeatReservation validateMine(Long id, Long userId) {
        SeatReservation record = getById(id);
        if (record == null) throw new BusinessException("预约记录不存在");
        if (!record.getUserId().equals(userId)) throw new BusinessException("无权操作该预约");
        return record;
    }

    private String convertSlot(String slot) {
        if ("MORNING".equals(slot)) return "上午";
        if ("AFTERNOON".equals(slot)) return "下午";
        if ("EVENING".equals(slot)) return "晚上";
        return slot;
    }

    private String getCurrentTimeSlot() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "上午";
        if (hour < 18) return "下午";
        return "晚上";
    }

    private String getCurrentHourRange() {
        int hour = LocalDateTime.now().getHour();
        int end = Math.min(24, hour + 1);
        return String.format("%02d:00-%02d:00", hour, end);
    }

    private void fillView(SeatReservation record) {
        record.setStatus(record.getStatusCode() == null ? "PENDING" : record.getStatusCode() == 0 ? "PENDING" : record.getStatusCode() == 1 ? "IN_USE" : record.getStatusCode() == 2 ? "VIOLATION" : record.getStatusCode() == 3 ? "TEMP_LEAVE" : "COMPLETED");
        Seat seat = seatService.getById(record.getSeatId());
        if (seat != null) {
            record.setSeatNo(seat.getSeatNo());
            record.setFloor(seat.getFloor());
            record.setArea(seat.getFloor() == 1 ? "一楼阅览区" : "二楼自习区");
        }
    }
}
