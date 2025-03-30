package com.zjs.web_mib_browser.mapper;

import com.zjs.web_mib_browser.domain.MibFile;
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
public interface MibFileMapper extends BaseMapper<MibFile> {

    @Select("select * from mib_file where type = #{type}")
    MibFile getMibFile(String type);
}
