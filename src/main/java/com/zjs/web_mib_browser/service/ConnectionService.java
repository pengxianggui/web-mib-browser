package com.zjs.web_mib_browser.service;

import io.github.pengxianggui.crud.BaseService;
import com.zjs.web_mib_browser.domain.Connection;

public interface ConnectionService extends BaseService<Connection> {

    void reachableDetect();

    Connection getByIp(String ip);
}