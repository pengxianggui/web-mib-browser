package com.zjs.web_mib_browser;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import com.zjs.mibparser.snmp.ParamSet;
import com.zjs.mibparser.snmp.SnmpContext;
import com.zjs.mibparser.snmp.SnmpRespException;
import com.zjs.web_mib_browser.domain.Connection;
import com.zjs.web_mib_browser.domain.MibFile;
import com.zjs.web_mib_browser.service.ConnectionService;
import com.zjs.web_mib_browser.service.MibFileService;
import lombok.extern.slf4j.Slf4j;
import net.percederberg.mibble.MibLoaderException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025-03-17 11:36
 */
@Slf4j
@RestController
public class ApiController {
    @Resource
    private MibFileService mibFileService;
    @Resource
    private ConnectionService connectionService;
    @Resource
    private SnmpContextManager snmpContextManager;

    @GetMapping(value = "/mib/types")
    public ApiRes<List<String>> getTypes() {
        List<String> types = mibFileService.list().stream().map(MibFile::getType).collect(Collectors.toList());
        return ApiRes.ok(types);
    }

    @GetMapping(value = "/mib/{type}/tree")
    public ApiRes<Tree<String>> getMibTree(@PathVariable String type, @RequestParam(required = false, defaultValue = "false") Boolean invalidCache) {
        try {
            Tree<String> tree = mibFileService.getTree(type, invalidCache);
            return ApiRes.ok(tree);
        } catch (MibLoaderException e) {
            return ApiRes.error("mib文件可能存在语法错误:" + e.getLog().toString(), e.getStackTrace());
        } catch (IOException e) {
            return ApiRes.error("mib文件读取失败", e.getStackTrace());
        }
    }

    @GetMapping(value = "/connections")
    public ApiRes<List<Connection>> getConnections() {
        // TODO 通过ip telnet查询设备的真实设备类型，然后在Connection中体现，方便前端根据type筛选连接
        return ApiRes.ok(connectionService.list());
    }

    @PostMapping(value = "{type}/getTableData")
    public ApiRes<List<Map>> getTableData(@PathVariable String type, @RequestParam String ip, @RequestParam String nodeName) throws SnmpRespException {
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
        return ApiRes.ok(snmpContext.tableView(nodeName));
    }

    @GetMapping(value = "{type}/getRowData")
    public ApiRes<Map> getRowData(@PathVariable String type, @RequestParam String ip, @RequestParam String nodeName, @RequestParam String index) throws SnmpRespException {
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
        List<Map<String, Object>> rows = snmpContext.tableView(nodeName, index);
        return rows.isEmpty() ? ApiRes.error("未查询到数据") : ApiRes.ok(rows.get(0));
    }

    @PostMapping(value = "{type}/setRowData")
    public ApiRes<String> setRowData(@PathVariable String type,
                                     @RequestParam String ip,
                                     @RequestBody Map<String, Object> data) throws SnmpRespException {
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
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
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
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
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
        return ApiRes.ok(snmpContext.get(nodeName));
    }

    @PostMapping(value = "{type}/setSingleData")
    public ApiRes setSingleData(@PathVariable String type,
                                @RequestParam String ip,
                                @RequestBody ParamSet data) throws SnmpRespException {
        SnmpContext snmpContext = snmpContextManager.getSnmpContext(type, ip);
        snmpContext.set(data.getName(), data.getValue());
        return ApiRes.ok(null, "设置成功");
    }
}
