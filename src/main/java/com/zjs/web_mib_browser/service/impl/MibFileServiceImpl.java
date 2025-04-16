package com.zjs.web_mib_browser.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.net.url.UrlQuery;
import com.zjs.mibparser.meta.MibUtil;
import com.zjs.web_mib_browser.SnmpContextManager;
import com.zjs.web_mib_browser.domain.MibFile;
import com.zjs.web_mib_browser.mapper.MibFileMapper;
import com.zjs.web_mib_browser.service.MibFileService;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import net.percederberg.mibble.MibLoaderException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MibFileServiceImpl extends BaseServiceImpl<MibFile, MibFileMapper> implements MibFileService {
    private static final Map<String, Tree<String>> TREE_CACHE = new ConcurrentHashMap<>();

    @Resource
    private MibFileMapper mibFileMapper;
    @Resource
    private SnmpContextManager snmpContextManager;

    @Override
    public void init() {
        this.baseMapper = mibFileMapper;
        this.clazz = MibFile.class;

        for (MibFile mibFile : list()) {
            File file = getFile(mibFile);
            try {
                String type = mibFile.getType();
                TREE_CACHE.put(type, MibUtil.buildTree(file));
            } catch (MibLoaderException e) {
                log.error("mib文件解析失败：" + e.getLog().toString(), e);
            } catch (IOException e) {
                log.error("mib文件读取失败", e);
            }
        }
    }

    @Override
    public boolean updateById(UpdateModelWrapper<MibFile> modelWrapper) {
        snmpContextManager.clearAll();
        TREE_CACHE.remove(modelWrapper.getModel().getType());
        return super.updateById(modelWrapper);
    }

    @Override
    public boolean removeById(Serializable id) {
        snmpContextManager.clearAll();
        MibFile mibFile = getById(id);
        if (mibFile != null) {
            TREE_CACHE.remove(mibFile.getType());
        }
        return super.removeById(id);
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        snmpContextManager.clearAll();
        list.stream().forEach(id -> {
            MibFile mibFile = getById((Serializable) id);
            if (mibFile != null) {
                TREE_CACHE.remove(mibFile.getType());
            }
        });
        return super.removeByIds(list);
    }

    @Override
    public File getFile(String type) {
        return getFile(mibFileMapper.getMibFile(type));
    }

    @Override
    public File getFile(MibFile mibFile) {
        if (mibFile == null) {
            return null;
        }
        try {
            UrlQuery urlQuery = UrlQuery.of(new URI(mibFile.getUrl()).getQuery(), Charset.defaultCharset());
            return new File(urlQuery.get("path").toString());
        } catch (URISyntaxException e) {
            log.error("根据mibFile无法解析文件地址, url:" + mibFile.getUrl(), e);
            return null;
        }
    }

    @Override
    public Tree<String> getTree(String type, boolean invalidCache) throws MibLoaderException, IOException {
        if (invalidCache == true) {
            TREE_CACHE.remove(type);
        }
        Tree<String> tree = TREE_CACHE.get(type);
        if (tree != null) {
            return tree;
        }

        File file = getFile(type);
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("请先维护此类型的mib文件:" + type);
        }
        tree = MibUtil.buildTree(file);
        TREE_CACHE.put(type, tree);
        return tree;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        String url = super.upload(row, col, file);
        TREE_CACHE.clear();
        return url;
    }
}
