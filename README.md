# web-mib-browser

为了替代mib-browser工具。

## 为什么要替代？

1. mib-browser在各自电脑上安装使用，因此每个人都需要在设备snmp manager列表里添加自己电脑的ip。但是设备往往存在限制，因此常出现互踢情况；
2. mib-browser彼此独立使用，可能出现mib文件不一致导致结果不一致的潜在可能；
3. 共用一个web-mib-browser，还能便于沟通分享(链接可指向mib节点并携带设备ip)；
4. 提供了一个统一维护mib文件和设备连接的地方；

