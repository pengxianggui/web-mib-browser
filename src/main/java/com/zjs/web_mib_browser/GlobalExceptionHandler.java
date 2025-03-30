package com.zjs.web_mib_browser;

import com.zjs.mibparser.snmp.SnmpRespException;
import com.zjs.mibparser.snmp.VerifyNotPassException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 * @author pengxg
 * @date 2025-03-26 14:26
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(VerifyNotPassException.class)
    public ApiRes handleException(VerifyNotPassException e, HttpServletResponse response) {
        log.error("验证错误：", e);
        return ApiRes.error("验证错误:" + e.getMessage(), e.getStackTrace());
    }

    @ExceptionHandler(SnmpRespException.class)
    public ApiRes handleException(SnmpRespException e, HttpServletResponse response) {
        log.error("snmp响应错误：", e);
        return ApiRes.error(e.getMessage(), e.getStackTrace());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiRes handleException(IllegalArgumentException e, HttpServletResponse response) {
        log.error("参数错误：", e);
        return ApiRes.error(e.getMessage(), e.getStackTrace());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiRes handleDuplicateKeyException(DuplicateKeyException e, HttpServletResponse response) {
        log.error("数据库约束错误：", e.getMessage());
        return ApiRes.error("数据约束错误:" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiRes handleException(Exception e, HttpServletResponse response) {
        log.error("系统错误：", e);
        return ApiRes.error("系统错误，请稍后再试", e.getStackTrace());
    }
}
