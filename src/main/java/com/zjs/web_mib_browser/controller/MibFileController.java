package com.zjs.web_mib_browser.controller;

import com.zjs.web_mib_browser.service.MibFileService;
import com.zjs.web_mib_browser.domain.MibFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@RestController
@RequestMapping("mib_file")
public class MibFileController extends BaseController<MibFile>{

    @Resource
    private MibFileService mibFileService;

    public MibFileController(MibFileService mibFileService) {
        super(mibFileService);
    }

}
