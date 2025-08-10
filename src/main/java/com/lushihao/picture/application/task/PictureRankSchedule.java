package com.lushihao.picture.application.task;

import com.lushihao.picture.domain.picture.entity.PictureRank;
import com.lushihao.picture.domain.picture.service.PictureRankDomainService;
import com.lushihao.picture.interfaces.dto.picture.PictureRankCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-10   17:15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PictureRankSchedule {
    @Resource
    private PictureRankDomainService pictureRankDomainService;

    /**
     * 排行榜的显示条数
     */
    private static final int limit = 10;
    //    @Scheduled(cron = "*/5 * * * * ?")
    // 每天凌晨1点统计昨日榜单
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyRank() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = yesterday.atStartOfDay();
        LocalDateTime endTime = yesterday.plusDays(1).atStartOfDay().minusNanos(1);
        log.info("执行任务");
        pictureRankDomainService.generatePictureRank("day", startTime, endTime, limit);
    }

    // 每周一凌晨1点统计上周榜单
    @Scheduled(cron = "0 0 1 ? * MON")
    public void weeklyRank() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);

        LocalDateTime startTime = lastMonday.atStartOfDay();
        LocalDateTime endTime = lastSunday.plusDays(1).atStartOfDay().minusNanos(1);
        pictureRankDomainService.generatePictureRank("week", startTime, endTime, limit);
    }

    // 每月1号凌晨2点统计上月榜单
    @Scheduled(cron = "0 0 2 1 * ?")
    public void monthlyRank() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayLastMonth = firstDayLastMonth.withDayOfMonth(firstDayLastMonth.lengthOfMonth());

        LocalDateTime startTime = firstDayLastMonth.atStartOfDay();
        LocalDateTime endTime = lastDayLastMonth.plusDays(1).atStartOfDay().minusNanos(1);

        pictureRankDomainService.generatePictureRank("month", startTime, endTime, limit);
    }


}
