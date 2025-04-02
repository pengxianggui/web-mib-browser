package com.zjs.web_mib_browser.apires;

import lombok.Data;

/**
 * @author pengxg
 * @date 2025-03-17 11:41
 */
@Data
public class ApiRes<T> {
    private String code;
    private String msg;
    private T data;
    private StackTraceElement[] stackTrace;

    public static <T> ApiRes ok(T data) {
        ApiRes apiRes = new ApiRes();
        apiRes.setCode("0");
        apiRes.setData(data);
        return apiRes;
    }

    public static <T> ApiRes ok(T data, String msg) {
        ApiRes apiRes = new ApiRes();
        apiRes.setCode("0");
        apiRes.setMsg(msg);
        apiRes.setData(data);
        return apiRes;
    }

    public static ApiRes error(String msg) {
        ApiRes apiRes = new ApiRes();
        apiRes.setCode("-1");
        apiRes.setMsg(msg);
        return apiRes;
    }

    public static ApiRes error(String msg, StackTraceElement[] stackTrace) {
        ApiRes apiRes = ApiRes.error(msg);
        apiRes.setStackTrace(stackTrace);
        return apiRes;
    }

}
