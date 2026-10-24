package com.smartlibrary.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlibrary.dto.SeatQueryDTO;
import com.smartlibrary.entity.Seat;
import com.smartlibrary.entity.SeatReservation;
import com.smartlibrary.entity.User;
import com.smartlibrary.exception.BusinessException;
import com.smartlibrary.mapper.SeatReservationMapper;
import com.smartlibrary.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService extends ServiceImpl<SeatMapper, Seat> {
    @Autowired private SeatReservationMapper seatReservationMapper;
    @Autowired private UserService userService;

    public List<Seat> list(SeatQueryDTO dto) {
        return list(dto, false);
    }

    public List<Seat> list(SeatQueryDTO dto, boolean canViewOccupant) {
        QueryWrapper<Seat> wrapper = new QueryWrapper<>();
        if (dto.getFloor() != null) wrapper.eq("floor", dto.getFloor());
        return list(wrapper).stream().peek(seat -> fillView(seat, canViewOccupant)).collect(Collectors.toList());
    }

    public List<Seat> listByFloor(Integer floor) {
        return listByFloor(floor, false);
    }

    public List<Seat> listByFloor(Integer floor, boolean canViewOccupant) {
        return list(new QueryWrapper<Seat>().eq("floor", floor).orderByAsc("seat_num")).stream().peek(seat -> fillView(seat, canViewOccupant)).collect(Collectors.toList());
    }

    public int getSeatVersion() {
        StringBuilder builder = new StringBuilder();
        list(new QueryWrapper<Seat>().orderByAsc("id")).forEach(seat -> builder
                .append(seat.getId()).append(':')
                .append(seat.getStatusCode()).append('|'));
        seatReservationMapper.selectList(new QueryWrapper<SeatReservation>()
                .in("status", 0, 1, 3)
                .orderByAsc("seat_id")).forEach(record -> builder
                .append(record.getSeatId()).append(':')
                .append(record.getUserId()).append(':')
                .append(record.getStatusCode()).append(':')
                .append(record.getReservationDate()).append(':')
                .append(record.getTimeSlot()).append(':')
                .append(record.getCheckInTime()).append(':')
                .append(record.getLeaveTime()).append('|'));
        builder.append("minute:").append(System.currentTimeMillis() / 60000);
        return builder.toString().hashCode();
    }

    public void occupySeat(Long seatId, Long userId) {
        Seat seat = getById(seatId);
        if (seat == null) throw new BusinessException("座位不存在");
        if (seat.getStatusCode() != 0) throw new BusinessException("座位不可用");
        Seat update = new Seat();
        update.setId(seatId);
        update.setStatusCode(1);
        updateById(update);
    }

    public void releaseSeat(Long seatId) {
        Seat update = new Seat();
        update.setId(seatId);
        update.setStatusCode(0);
        updateById(update);
    }

    public void tempLeave(Long seatId, Long userId) {
        Seat update = new Seat();
        update.setId(seatId);
        update.setStatusCode(2);
        updateById(update);
    }

    public void returnFromTempLeave(Long seatId, Long userId) {
        Seat update = new Seat();
        update.setId(seatId);
        update.setStatusCode(1);
        updateById(update);
    }

    public void checkTempLeaveTimeout() {
    }

    private void fillView(Seat seat) {
        fillView(seat, false);
    }

    private void fillView(Seat seat, boolean canViewOccupant) {
        seat.setStatus(seat.getStatusCode() == null || seat.getStatusCode() == 0 ? "FREE" : seat.getStatusCode() == 1 ? "OCCUPIED" : "TEMP_LEAVE");
        seat.setArea(seat.getFloor() == 1 ? "一楼阅览区" : "二楼自习区");
        List<SeatReservation> pendingReservations = getActivePendingReservations(seat.getId());
        seat.setReservedTimeSlots(pendingReservations.stream()
                .map(record -> record.getReservationDate() + " " + record.getTimeSlot())
                .collect(Collectors.toList()));
        if ((seat.getStatusCode() == null || seat.getStatusCode() == 0) && !pendingReservations.isEmpty()) {
            seat.setStatus("RESERVED");
        }
        if (seat.getStatusCode() != null && (seat.getStatusCode() == 1 || seat.getStatusCode() == 2)) {
            SeatReservation record = seatReservationMapper.selectOne(new QueryWrapper<SeatReservation>()
                    .eq("seat_id", seat.getId())
                    .in("status", 1, 3)
                    .orderByDesc("id")
                    .last("LIMIT 1"), false);
            if (record != null) {
                seat.setCurrentUserId(record.getUserId());
                seat.setOccupyTime(record.getCheckInTime());
                seat.setTempLeaveTime(record.getLeaveTime());
                seat.setTempLeaveExpireTime(record.getLeaveTime() == null ? null : record.getLeaveTime().plusMinutes(15));
                if (canViewOccupant) {
                    User user = userService.getById(record.getUserId());
                    if (user != null) {
                        String displayName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getStudentNo();
                        seat.setCurrentUserName(displayName);
                    }
                }
            }
        }
    }

    private List<SeatReservation> getActivePendingReservations(Long seatId) {
        LocalDate today = LocalDate.now();
        return seatReservationMapper.selectList(new QueryWrapper<SeatReservation>()
                        .eq("seat_id", seatId)
                        .eq("status", 0)
                        .ge("reserve_date", today)
                        .orderByAsc("reserve_date")
                        .orderByAsc("time_period"))
                .stream()
                .filter(this::isPendingReservationActive)
                .collect(Collectors.toList());
    }

    private boolean isPendingReservationActive(SeatReservation record) {
        if (record.getReservationDate() == null) return false;
        if (record.getReservationDate().isAfter(LocalDate.now())) return true;
        if (record.getReservationDate().isBefore(LocalDate.now())) return false;
        int[] range = parsePeriod(record.getTimeSlot());
        if (range == null) return true;
        LocalDateTime endTime = LocalDateTime.of(record.getReservationDate(), LocalTime.of(Math.min(23, range[1]), 0));
        if (range[1] >= 24) {
            endTime = LocalDateTime.of(record.getReservationDate().plusDays(1), LocalTime.MIDNIGHT);
        }
        return endTime.isAfter(LocalDateTime.now());
    }

    private int[] parsePeriod(String period) {
        if (period == null) return null;
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
}
