package com.zjs.web_mib_browser.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Connection {

    private String id;

    private String ip;

    private Integer port;

    private String community;

    private String version;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /**
     * 设备能否ping通过
     */
    @TableField(exist = false)
    private Boolean reachable;
}
