package com.zjs.web_mib_browser.controller;

import com.zjs.web_mib_browser.service.ConnectionService;
import com.zjs.web_mib_browser.domain.Connection;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@RestController
@RequestMapping("connection")
public class ConnectionController extends BaseController<Connection>{

    @Resource
    private ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        super(connectionService);
    }

}
