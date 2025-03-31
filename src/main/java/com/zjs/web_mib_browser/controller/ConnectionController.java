package com.zjs.web_mib_browser.controller;

import com.zjs.web_mib_browser.service.ConnectionService;
import io.github.pengxianggui.crud.dynamic.Crud;
import io.github.pengxianggui.crud.dynamic.CrudService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Crud
@RestController
@RequestMapping("connection")
public class ConnectionController {

    @CrudService
    @Resource
    private ConnectionService connectionService;

}
