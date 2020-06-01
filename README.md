

# ZMS

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.rocketmq/rocketmq-all/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Corg.apache.rocketmq)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 什么是ZMS ?

ZMS是ZTO Message Service的缩写，使用方与集群解耦，屏蔽各消息集群差异，并对消息集群进行安装、管理、监控、告警管理的平台。

## ZMS能做什么 ?

**客户端快速接入**

* 屏蔽消息接入方使用不同类型集群之间的差异（zms-client）
* 加密消息（待开源）
* 灰度消息（待开源）
* 消息轨迹（待开源）

**自动化运维**

* 集群一键安装、可视化运维
* 集群资源自定义告警
* 动态集群、主题、消费组迁移，客户端无感知

**监控**

* 接入客户端发送、消费指标实时监控
* 集群节点监控检测、指标监控

**告警**

* 支持自定义告警：tps、消费延迟、最后消费时间

## 快速启动

### 	通过安装包快速启动，免去编译、打包步骤

> 安装包在710M左右，里面包含可以通过ZMS自动安装的第三方安装包

>  依赖：jdk1.8+   	mysql 5.6+

GitHub下载地址：[安装包](https://github.com/ZTO-Express/zms/releases/download/1.0.0/zms-assembly-1.0.0.RELEASE.zip)

1. **创建数据库**

   解压安装包，将初始化脚本导入到mysql数据库

   > 例如连接msql客户端，通过原生命令导入 ${project.dir}:项目根目录

   ```she
   > source ${project.dir}/sql/zmsdb.sql
   ```

2. **修改ZMS启动配置**

   > ${project.dir}/conf/application.properties

   ```properties
   spring.datasource.url = jdbc:mysql://${you.mysql.host:port}/zms?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   spring.datasource.username = ${username}
   spring.datasource.password = ${password}
   
   zms.portal.url=http://${portal.host:port}
   ```

   以下参数需要替换成自己的配置

   > ${you.mysql.host:port}：mysql数据库地址:端口
   >
   > ${username}：mysql user
   >
   > ${password}：mysql password
   >
   > ${portal.host:port}：ZMS管理后台启动地址，其他服务通过这个地址与ZMS管理后台进行交互（ZMS后台安装的真实ip、端，或域名映射）。如果 zms.portal.url 配置为空，默认取ZMS管理后台进程服务器的网卡地址

3. **启动ZMS管理后台**

   ```she
   > ${project.dir}bin/server.sh start
   ```

4. **当显示下面的日志，说明启动成功**

   ``` she
    server.port:8088 
    logging.path:/data/logs/zms-portal 
    Start success,pid:[12866] 
   ```

5. **访问服务**

   地址：http://localhost:8088

   账号/密码：admin/admin


### 通过源码手动打包安装

> 依赖：jdk1.8+     nodejs 9-12    maven 3+
>

1. 下载源码

   ``` shell
    git clone https://github.com/ZTO-Express/zms.git
   ```

2. 下载第三方软件安装包（可选）

   如果跳过此步骤，ZMS添加主机时，主机必须有JDK环境，并且有外部网络访问权限

   > 为了提升主机初始化速度，我们可以将主机需要安装软件包提前下载到ZMS管理后台的软件包目录，在主机进行初始化下载软件包时，直接通从ZMS的本地文件目录直接下载。

   1. 下载地址:

       [服务安装包](https://github.com/ZTO-Express/zms/releases/download/1.0.0/zms-lib.zip)

   2. 解压文件：

      将压缩包解压到 ${project.dir}/distribution/lib/

3. **进入项目根目录，初始化库**

   > 设置数据库地址：${project.dir}/distribution/pom.xml

   ```xml
      <driver>com.mysql.jdbc.Driver</driver>
       <url>jdbc:mysql://${you.mysql.host:port}</url>
       <username>${username}</username>
       <password>${password}</password>
   ```

   > ${project.dir}/zms-portal/src/main/resources/application.properties

   ```properties
   spring.datasource.url = jdbc:mysql://${you.mysql.host:port}/zms?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   spring.datasource.username = ${username}
   spring.datasource.password = ${password}
   
   zms.portal.url=http://${portal.host:port}
   ```
   > ${you.mysql.host:port}：mysql数据库地址:端口
   >
   > ${username}：mysql user
   >
   > ${password}：mysql password
   >
   > ${portal.host:port}：ZMS管理后台启动地址，其他服务通过这个地址与ZMS管理后台进行交互（ZMS后台安装的真实ip、端，或域名映射）。如果 zms.portal.url 配置为空，默认取ZMS管理后台进程服务器的网卡地址

   

   初始化库命令，进入目录：${project.dir}/distribution（只需初始化一次）

   ```she
   mvn sql:execute
   ```

4. **项目根目录打包**

   > 生成压缩包: distribution/target/zms-assembly-${project.version}.tar.gz

   ```she
   > mvn clean install -DskipTests -P npm-build
   ```

5. **解压压缩包**

   ```sh
   > tar -zxvf zms-assembly-${project.version}.tar.gz
   ```

6. **启动服务**

   ```sh
   > cd zms-assembly-${project.version}
   > bin/server.sh start
   ```

7. **当显示下面的日志，说明启动成功**

   ``` she
    server.port:8088 
    logging.path:/data/logs/zms-portal 
    Start success,pid:[12866] 
   ```

8. **访问服务**

   地址：http://locallhost:8088

   账号/密码：admin/admin

### 使用文档

1. **zms管理后台使用文档**

   [zms管理系统使用文档](https://github.com/ZTO-Express/zms/wiki/zms%E7%AE%A1%E7%90%86%E7%B3%BB%E7%BB%9F%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3)

2. **zms客户端接入文档**

   [zms-client接入手册](https://github.com/ZTO-Express/zms/wiki/zms-client%E6%8E%A5%E5%85%A5%E6%89%8B%E5%86%8C)
   
3.  **zms设计文档**

    [zms设计文档](https://github.com/ZTO-Express/zms/wiki/zms%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3)

  
