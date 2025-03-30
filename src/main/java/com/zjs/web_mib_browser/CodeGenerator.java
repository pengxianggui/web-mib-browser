package com.zjs.web_mib_browser;

import io.github.pengxianggui.crud.autogenerate.CodeAutoGenerator;

/**
 * @author pengxg
 * @date 2025/3/29 20:23
 */
public class CodeGenerator {
    public static void main(String[] args) {
        CodeAutoGenerator.builder()
                .author("pengxg") // 改成实际的作者名
                .url("jdbc:mysql://127.0.0.1:3306/web_mib_browser") // 替换成你的数据库连接地址
                .username("root")  // 替换成你的数据库用户名
                .password("123456")  // 替换成你的数据库密码
                .parentPkg("com.zjs.web_mib_browser")  // 替换成你的包根目录
                .build()
                .generate();
    }
}
