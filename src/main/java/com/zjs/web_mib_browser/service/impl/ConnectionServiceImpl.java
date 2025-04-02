package com.zjs.web_mib_browser.service.impl;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.zjs.web_mib_browser.SnmpContextManager;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.mapper.ConnectionMapper;
import com.zjs.web_mib_browser.service.ConnectionService;
import com.zjs.web_mib_browser.socket.MsgWebSocketHandler;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
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
    @Resource
    private MsgWebSocketHandler socketHandler;

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

    @Scheduled(fixedRateString = "${task.connection-detect-freq: 180000}")
    public void reachableDetect() {
        Map<String, Boolean> changedMsg = new HashMap<>();
        list().stream().forEach(c -> {
            Boolean oldReachable = IP_REACHABLE_CACHE.get(c.getIp());
            Boolean newReachable = NetUtil.ping(c.getIp());
            boolean changed = (oldReachable != newReachable);
            if (changed) {
                IP_REACHABLE_CACHE.put(c.getIp(), newReachable);
                changedMsg.put(c.getIp(), newReachable);
            }
        });
        if (!changedMsg.isEmpty()) {
            socketHandler.boardcast(MsgWebSocketHandler.Type.CONNECTION_REACHABLE_REFRESH, changedMsg);
        }
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
