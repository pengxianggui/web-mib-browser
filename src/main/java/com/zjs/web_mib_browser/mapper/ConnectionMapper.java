package com.zjs.web_mib_browser.mapper;

import com.zjs.web_mib_browser.domain.Connection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author pengxg
 * @since 2025-03-30
 */
public interface ConnectionMapper extends BaseMapper<Connection> {

    @Select("select * from connection where ip = #{ip}")
    Connection getByIp(String ip);
}
