package com.starbucks.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_log")
public class BalanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId; // 日志ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "User_id")
    private UserInfo user; // 关联用户ID

    @Column(name = "change_amount")
    private Integer changeAmount; // 变动金额（正数为增加，负数为减少）

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type")
    private ChangeType changeType; // 变动类型

    @Column(name = "change_time")
    private LocalDateTime changeTime; // 变动时间

    @Column(name = "remarks", length = 255)
    private String remarks; // 备注信息

    public enum ChangeType {
        RECHARGE, PAYMENT, REFUND, BONUS
    }

    // getter 和 setter
    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    public Integer getChangeAmount() { return changeAmount; }
    public void setChangeAmount(Integer changeAmount) { this.changeAmount = changeAmount; }
    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }
    public LocalDateTime getChangeTime() { return changeTime; }
    public void setChangeTime(LocalDateTime changeTime) { this.changeTime = changeTime; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}