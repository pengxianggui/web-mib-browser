package com.zjs.web_mib_browser.service.impl;

import com.zjs.web_mib_browser.SnmpContextManager;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.mapper.ConnectionMapper;
import com.zjs.web_mib_browser.service.ConnectionService;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;

@Service
public class ConnectionServiceImpl extends BaseServiceImpl<Connection, ConnectionMapper> implements ConnectionService {

    @Resource
    private ConnectionMapper connectionMapper;
    @Resource
    private SnmpContextManager snmpContextManager;

    @Override
    public void init() {
        this.baseMapper = connectionMapper;
        this.clazz = Connection.class;
    }


    @Override
    public boolean updateById(UpdateModelWrapper<Connection> modelWrapper) {
        snmpContextManager.clearAll();
        return super.updateById(modelWrapper);
    }

    @Override
    public boolean removeById(Serializable id) {
        snmpContextManager.clearAll();
        return super.removeById(id);
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        snmpContextManager.clearAll();
        return super.removeByIds(list);
    }

    @Override
    public Connection getByIp(String ip) {
        return connectionMapper.getByIp(ip);
    }
}
