package com.example.find_campus.dto;

import lombok.Data;

@Data
public class DashboardStatsDto {

    private int userCount;
    private int lostCount;
    private int foundCount;
    private int reportCount;
    private int waitingReportCount;
}
