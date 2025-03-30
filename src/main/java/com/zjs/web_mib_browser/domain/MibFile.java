package com.zjs.web_mib_browser.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("mib_file")
public class MibFile {

    private String id;

    /**
     * Mib类型
     */
    private String type;

    /**
     * 文件路径
     */
    private String url;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
