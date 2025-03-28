package com.zjs.web_mib_browser;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import com.zjs.mibparser.meta.MibUtil;
import com.zjs.mibparser.snmp.ParamSet;
import com.zjs.mibparser.snmp.SnmpContext;
import com.zjs.mibparser.snmp.SnmpRespException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pengxg
 * @date 2025-03-17 11:36
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    private static final String MIB_FILE_DIR = "mib";
    private static final Map<String, File> MIB_FILE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Tree<String>> TREE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Connection> CONNECTION_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, SnmpContext> SNMP_CONTEXT_CACHE = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        URL resource = getClass().getClassLoader().getResource(MIB_FILE_DIR);
        File dir = new File(resource.getFile());
        if (dir.exists() && dir.isDirectory()) {
            for (File mibFile : dir.listFiles()) {
                try {
                    String type = mibFile.getName();
                    MIB_FILE_CACHE.put(type, mibFile);
                    TREE_CACHE.put(type, MibUtil.buildTree(mibFile));
                } catch (MibLoaderException e) {
                    log.error(e.getLog().toString());
                } catch (IOException e) {
                    log.error("mib文件读取失败", e);
                }
            }
        }
        // TODO for test
        Connection connection = new Connection();
        connection.setIp("192.168.199.139");
        connection.setPort(161);
        connection.setCommunity("saisi");
        connection.setVersion("2");
        addConnection(connection);
    }

    @GetMapping(value = "/mib/types")
    public ApiRes<List<String>> getTypes() {
        return ApiRes.ok(TREE_CACHE.keySet());
    }

    @GetMapping(value = "/mib/{type}/tree")
    public ApiRes<Tree<String>> getMibTree(@PathVariable String type) {
        Tree<String> tree = TREE_CACHE.get(type);
        return tree == null ? ApiRes.error("请先上传mib文件") : ApiRes.ok(TREE_CACHE.get(type));
    }

    @PostMapping(value = "/mib/upload")
    public ApiRes<Tree<String>> uploadMibFile(MultipartFile file) throws IOException {
        File mibFile = Files.createTempFile(file.getName(), ".txt").toFile();
        file.transferTo(mibFile);
        String type = mibFile.getName();
        try {
            Tree<String> tree = MibUtil.buildTree(mibFile);
            MIB_FILE_CACHE.put(type, mibFile);
            TREE_CACHE.put(type, tree);
            return ApiRes.ok(tree);
        } catch (MibLoaderException e) {
            log.error("mib文件语法错误", e);
            return ApiRes.error(e.getMessage());
        }
    }

    @GetMapping(value = "/connections")
    public ApiRes<List<Connection>> getConnections() {
        return ApiRes.ok(CONNECTION_CACHE.values());
    }

    @PostMapping(value = "connection")
    public ApiRes addConnection(@RequestBody Connection connection) {
        if (CONNECTION_CACHE.containsKey(connection.getIp())) {
            Connection d = CONNECTION_CACHE.get(connection.getIp());
            d.setCommunity(connection.getCommunity());
            d.setPort(connection.getPort());
            d.setVersion(connection.getVersion());
            return ApiRes.ok(connection, "已更新连接信息");
        } else {
            CONNECTION_CACHE.put(connection.getIp(), connection);
            return ApiRes.ok(connection, "已添加连接信息");
        }
    }

    @PostMapping(value = "{type}/getTableData")
    public ApiRes<List<Map>> getTableData(@PathVariable String type, @RequestParam String ip, @RequestParam String nodeName) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        return ApiRes.ok(snmpContext.tableView(nodeName));
    }

    @GetMapping(value = "{type}/getRowData")
    public ApiRes<Map> getRowData(@PathVariable String type, @RequestParam String ip, @RequestParam String nodeName, @RequestParam String index) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        List<Map<String, Object>> rows = snmpContext.tableView(nodeName, index);
        return rows.isEmpty() ? ApiRes.error("未查询到数据") : ApiRes.ok(rows.get(0));
    }

    @PostMapping(value = "{type}/setRowData")
    public ApiRes<String> setRowData(@PathVariable String type,
                                     @RequestParam String ip,
                                     @RequestBody Map<String, Object> data) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        String index = String.valueOf(data.get("index"));
        Assert.notBlank(index, "index不能为空");
        List<ParamSet> paramSets = new ArrayList<>(data.size());
        data.entrySet().forEach(entry -> {
            if (!entry.getKey().equals("index")) {
                paramSets.add(new ParamSet(entry.getKey(), index, entry.getValue()));
            }
        });
        snmpContext.multiSet(paramSets);
        return ApiRes.ok("设置成功");
    }

    @PostMapping(value = "{type}/multiSetRowData")
    public ApiRes<String> multiSetRowData(@PathVariable String type,
                                          @RequestParam String ip,
                                          @RequestBody List<Map<String, Object>> data) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        List<ParamSet> paramSets = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            String index = String.valueOf(row.get("index"));
            Assert.notBlank(index, "第{}行的index为空!", i + 1);
            row.entrySet().forEach(entry -> {
                if (!entry.getKey().equals("index")) {
                    paramSets.add(new ParamSet(entry.getKey(), index, entry.getValue()));
                }
            });
        }
        snmpContext.multiSet(paramSets);
        return ApiRes.ok("设置成功");
    }


    @GetMapping(value = "{type}/getSingleData")
    public ApiRes getSingleData(@PathVariable String type,
                                @RequestParam String ip,
                                @RequestParam String nodeName) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        return ApiRes.ok(snmpContext.get(nodeName));
    }

    @PostMapping(value = "{type}/setSingleData")
    public ApiRes setSingleData(@PathVariable String type,
                                @RequestParam String ip,
                                @RequestBody ParamSet data) throws SnmpRespException {
        SnmpContext snmpContext = getSnmpContext(type, ip);
        snmpContext.set(data.getName(), data.getValue());
        return ApiRes.ok(null, "设置成功");
    }

    private SnmpContext getSnmpContext(String type, String ip) {
        final String key = type + ":" + ip;
        if (SNMP_CONTEXT_CACHE.containsKey(key)) {
            return SNMP_CONTEXT_CACHE.get(key);
        } else {
            Connection connection = CONNECTION_CACHE.get(ip);
            Assert.isTrue(connection != null, "缺少对应的连接信息,ip:" + ip);
            File mibFile = MIB_FILE_CACHE.get(type);
            Assert.isTrue(mibFile != null, "缺少对应的mib文件,type:" + type);
            try {
                Mib mib = MibUtil.loadMib(mibFile);
                SnmpContext snmpContext = new SnmpContext(connection.getIp(), connection.getPort(), connection.getCommunity(), mib);
                SNMP_CONTEXT_CACHE.put(key, snmpContext);
                return snmpContext;
            } catch (IOException | MibLoaderException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    public static class Connection {
        private String ip;
        private int port;
        private String community;
        private String version;
    }
}
