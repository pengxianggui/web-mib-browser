package com.zjs.web_mib_browser;

import cn.hutool.core.lang.Assert;
import com.zjs.mibparser.meta.MibUtil;
import com.zjs.mibparser.snmp.SnmpContext;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.service.ConnectionService;
import com.zjs.web_mib_browser.service.MibFileService;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoaderException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pengxg
 * @date 2025/3/30 12:56
 */
@Component
public class SnmpContextManager {
    /**
     * snmp上下文缓存。key为 "${type}:${ip}"
     */
    private static final Map<String, SnmpContext> SNMP_CONTEXT_CACHE = new ConcurrentHashMap<>();
    @Resource
    private MibFileService mibFileService;
    @Resource
    private ConnectionService connectionService;

    public void clearAll() {
        SNMP_CONTEXT_CACHE.clear();
    }

    public SnmpContext getSnmpContext(String type, String ip) {
        final String key = type + ":" + ip;
        if (SNMP_CONTEXT_CACHE.containsKey(key)) {
            return SNMP_CONTEXT_CACHE.get(key);
        } else {
            Connection connection = connectionService.getByIp(ip);
            Assert.isTrue(connection != null, "先维护连接信息,ip:" + ip);
            File file = mibFileService.getFile(type);
            Assert.isTrue(file != null, "先维护mib文件,type:" + type);
            try {
                Mib mib = MibUtil.loadMib(file);
                SnmpContext snmpContext = new SnmpContext(connection.getIp(), connection.getPort(), connection.getCommunity(), mib);
                SNMP_CONTEXT_CACHE.put(key, snmpContext);
                return snmpContext;
            } catch (IOException | MibLoaderException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
