<p align="center">
<a href="" target="_blank">
    <img src="assets/images/icon.png" width="300" alt="">
</a>
</p>

<h1 align="center">Pilipala</h1>
<p align="center">
<span><strong>噼里啪啦视频弹幕网</strong></span><br/>
</p>
<div align="center">
    <a href="https://github.com/CxyJerry/pilipala"><img alt="后端" src="https://img.shields.io/badge/github-%E5%90%8E%E7%AB%AF-red"></a> 
    <a href="https://github.com/CxyJerry/pilipala-web"><img alt="前端" src="https://img.shields.io/badge/github-%E5%89%8D%E7%AB%AF-red"></a>
    <a href="https://github.com/CxyJerry/pilipala/blob/master/LICENSE" target="_blank">
        <img alt="开源协议" src="https://img.shields.io/badge/%E5%BC%80%E6%BA%90%E5%8D%8F%E8%AE%AE-GPL-blue">
    </a>
    <a href="https://github.com/CxyJerry/pilipala/actions/workflows/docker-image.yml"><img src="https://github.com/CxyJerry/pilipala/actions/workflows/docker-image.yml/badge.svg"></a>
</div>

## 项目介绍

噼里啪啦视频弹幕网是一个专注于做视频点播的后端服务项目，通过对 [FFmpeg](https://github.com/FFmpeg/FFmpeg) 的集成，半遵循广电视频规格要求对视频规格进行限制（支持 1080P及以下规格视频），产出视频生产计划，并通过流水线+状态机完成视频转码。本项目持续更新中！

## 环境搭建

### 开发环境

1. 使用 `Git` 拉取项目源代码
2. 配置 `JDK` 版本为 `17`
3. 使用 `Maven` 完成依赖包导入
4. 通过 `Docker Compose` 搭建 `Neo4j`、`Redis`、`MongoDB 集群`
5. 完成 `application-dev.yml` 配置信息修改
6. 启动 `PiliPalaApplication`

### 测试环境

1. 构建镜像

   `docker build . -t pilipala`

2. 运行镜像

   `docker run -p 8080:8080 pilipala`

### 加入社群

<img src="assets/images/qrcode.png" alt="" style="zoom:50%;" />
