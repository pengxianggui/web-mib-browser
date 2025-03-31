package com.zjs.web_mib_browser.controller;

import com.zjs.web_mib_browser.service.MibFileService;
import io.github.pengxianggui.crud.dynamic.Crud;
import io.github.pengxianggui.crud.dynamic.CrudService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Crud
@RestController
@RequestMapping("mib_file")
public class MibFileController {

    @CrudService
    @Resource
    private MibFileService mibFileService;

}
