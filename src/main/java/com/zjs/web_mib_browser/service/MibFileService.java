package com.zjs.web_mib_browser.service;

import cn.hutool.core.lang.tree.Tree;
import io.github.pengxianggui.crud.BaseService;
import com.zjs.web_mib_browser.domain.MibFile;
import net.percederberg.mibble.MibLoaderException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface MibFileService extends BaseService<MibFile> {

    File getFile(String type);

    File getFile(MibFile mibFile);

    Tree<String> getTree(String type, boolean invalidCache) throws MibLoaderException, IOException;
}