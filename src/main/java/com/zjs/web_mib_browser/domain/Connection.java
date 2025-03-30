package com.zjs.web_mib_browser.domain;

import java.time.LocalDateTime;

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
}
