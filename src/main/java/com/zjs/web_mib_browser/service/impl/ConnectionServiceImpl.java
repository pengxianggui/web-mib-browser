package com.zjs.web_mib_browser.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.zjs.web_mib_browser.SnmpContextManager;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.mapper.ConnectionMapper;
import com.zjs.web_mib_browser.service.ConnectionService;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectionServiceImpl extends BaseServiceImpl<Connection, ConnectionMapper> implements ConnectionService {
    private static final Map<String, Boolean> IP_REACHABLE_CACHE = new ConcurrentHashMap<>();

    @Resource
    private ConnectionMapper connectionMapper;
    @Resource
    private SnmpContextManager snmpContextManager;

    @Override
    public void init() {
        this.baseMapper = connectionMapper;
        this.clazz = Connection.class;

        reachableDetect();
    }

    @Override
    public Pager<Connection> queryPage(PagerQuery query) {
        Pager<Connection> pager = super.queryPage(query);
        pager.getRecords().forEach(c -> {
            if (StrUtil.isBlank(c.getId())) {
                return;
            }
            if (IP_REACHABLE_CACHE.containsKey(c.getIp())) {
                c.setReachable(IP_REACHABLE_CACHE.get(c.getIp()));
            }
        });
        return pager;
    }

    @Override
    public List<Connection> list() {
        List<Connection> connections = super.list();
        connections.stream().forEach(c -> {
            if (StrUtil.isBlank(c.getId())) {
                return;
            }
            if (IP_REACHABLE_CACHE.containsKey(c.getIp())) {
                c.setReachable(IP_REACHABLE_CACHE.get(c.getIp()));
            }
        });
        return connections;
    }

    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void reachableDetect() {
        List<Connection> connections = list();
        connections.forEach(connection -> IP_REACHABLE_CACHE.put(connection.getIp(), NetUtil.ping(connection.getIp())));
    }

    public boolean reachableDetect(Connection connection) {
        Assert.notNull(connection.getIp(), "ip不能为空");
        boolean reachable = NetUtil.ping(connection.getIp());
        return IP_REACHABLE_CACHE.put(connection.getIp(), reachable);
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
