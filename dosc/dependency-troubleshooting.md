# 依赖与模块扫描问题排查记录

记录日期：2026-07-17

## 背景

项目在导入 Maven 模块、解析 `exchange-common-core` 依赖、编译 `exchange-auth` 时连续出现多个问题：

- `exchange-common-core` 依赖扫不到
- `exchange-common` 子模块在 IDEA 中显示不完整
- Maven 下载 `spring-boot-actuator` 失败
- Lombok 生成方法缺失
- WebFlux 接口启动时报 `HttpServletRequest` 构造器异常

## 问题 1：exchange-common-core 依赖扫不到

### 现象

执行 Maven 校验时报错：

```text
'dependencies.dependency.version' for jakarta.servlet:jakarta.servlet-api:jar is missing
'dependencies.dependency.version' for org.projectlombok:lombok:jar is missing
```

### 原因

`exchange-common-core` 中声明了 `jakarta.servlet-api` 和 `lombok`，但父级 `exchange-parent` 只导入了 Spring Cloud 和 Spring Cloud Alibaba BOM，没有导入 Spring Boot BOM，因此这些依赖版本无法被管理。

### 处理

在 `exchange-parent/pom.xml` 中增加：

```xml
<spring-boot.version>3.2.9</spring-boot.version>
```

并在 `dependencyManagement` 中导入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>${spring-boot.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

## 问题 2：IDEA 中 exchange-common 子模块显示不完整

### 现象

`exchange-common/pom.xml` 已经声明：

```xml
<modules>
    <module>exchange-common-core</module>
    <module>exchange-common-redis</module>
</modules>
```

但 IDEA 模块树没有正确显示所有子模块。

### 原因

Maven reactor 能识别子模块，问题主要是 IDEA `.idea/modules.xml` 中存在旧模块缓存，例如旧路径 `exchange-parent/exchange-gateway`。

### 验证

```bash
mvn -f exchange-parent/pom.xml validate -DskipTests
```

Maven reactor 可以正确识别：

```text
exchange-parent
exchange-common
exchange-common-core
exchange-common-redis
exchange-api
exchange-gateway
```

### 建议

在 IDEA 中重新加载 Maven 项目。如果仍异常，关闭 IDEA 后清理旧 `.iml` / `.idea/modules.xml`，再用 `exchange-parent/pom.xml` 重新导入。

## 问题 3：spring-boot-actuator 下载失败

### 现象

```text
Could not transfer artifact org.springframework.boot:spring-boot-actuator:jar:3.2.9
Premature end of Content-Length delimited message body
```

### 原因

从阿里云 Maven 仓库下载 jar 时网络中断，本地仓库只留下了 `.lastUpdated` 失败记录，没有完整 jar。

### 处理

使用 `-U` 强制刷新依赖：

```bash
mvn -U -f exchange-parent/pom.xml -pl :exchange-common-core -am dependency:resolve -DskipTests
```

确认 jar 已下载：

```text
~/.m2/repository/org/springframework/boot/spring-boot-actuator/3.2.9/spring-boot-actuator-3.2.9.jar
```

## 问题 4：Lombok 生成方法缺失

### 现象

编译时报错：

```text
StatusCode.SUCCESS.getCode() 找不到符号
StatusCode.SUCCESS.getMessage() 找不到符号
```

随后 `exchange-auth` 中也出现：

```text
变量 log 找不到符号
LoginDto.getUsername() 找不到符号
LoginDto.getPassword() 找不到符号
```

### 原因

当前 Maven 使用 JDK 26，Spring Boot 3.2.9 默认管理的 Lombok 版本较旧，且部分模块没有正确继承父 POM 的编译插件配置。

### 处理

在 `exchange-parent/pom.xml` 中统一配置：

```xml
<lombok.version>1.18.46</lombok.version>
```

并在 `dependencyManagement` 中覆盖 Lombok 版本：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${lombok.version}</version>
</dependency>
```

同时配置 `maven-compiler-plugin`：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>${maven.compiler.target}</release>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

另外给 `exchange-auth` 显式增加 Lombok 依赖，避免依赖传递不清晰。

## 问题 5：HttpServletRequest 在 WebFlux 中报构造器异常

### 现象

启动或请求 `exchange-auth` 接口时报：

```text
java.lang.IllegalStateException: No primary or single unique constructor found for interface jakarta.servlet.http.HttpServletRequest
```

### 原因

`exchange-auth` 使用的是：

```xml
<artifactId>spring-boot-starter-webflux</artifactId>
```

但 `AuthController` 中方法参数使用了 Servlet 栈类型：

```java
HttpServletRequest request
```

WebFlux 不会把 `HttpServletRequest` 当作内置请求对象解析，Spring 会尝试把它当普通参数绑定并实例化，最终因为接口没有构造器而报错。

### 处理

将 controller 参数改为 WebFlux 类型：

```java
ServerHttpRequest request
```

同时 `IpUtil` 增加通用重载：

```java
getClientIp(String realClientIp, String forwardedFor, String realIp, String remoteAddr)
```

Servlet 版本继续保留，WebFlux controller 通过 headers 和 remote address 调用通用方法。

## 父 POM 路径修正

`exchange-auth`、`exchange-api`、`exchange-gateway` 的 parent 增加：

```xml
<relativePath>../exchange-parent/pom.xml</relativePath>
```

避免 Maven 从本地仓库读取旧的 `coin.exchange:exchange-parent:1.0` POM。

## 问题 6：exchange-business-user 没有扫描到父级依赖

### 现象

`exchange-business-user` 模块无法正确继承或扫描到父级依赖管理。

尝试通过父工程定位模块：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-business-user -am validate -DskipTests
```

实际报错：

```text
'packaging' with value 'jar' is invalid. Aggregator projects require 'pom' as packaging.
```

### 原因

`exchange-business/pom.xml` 同时具备两个角色：

- 作为 `exchange-parent` 下的子模块
- 作为 `exchange-business-user` 的聚合父模块

但当前 `exchange-business/pom.xml` 写的是：

```xml
<packaging>jar</packaging>

<modules>
    <module>exchange-business-user</module>
</modules>
```

Maven 规则要求：只要一个项目声明了 `<modules>`，它就是 aggregator，`packaging` 必须是 `pom`，不能是 `jar`。

另外，`exchange-business/pom.xml` 的父级是 `exchange-parent`，但没有写：

```xml
<relativePath>../exchange-parent/pom.xml</relativePath>
```

从 `exchange-business` 目录看，Maven 默认父 POM 路径是 `../pom.xml`，该路径并不是当前项目的 `exchange-parent/pom.xml`。找不到相对父 POM 时，Maven 会回退到本地仓库解析 `coin.exchange:exchange-parent:1.0`。如果本地仓库里存在旧版父 POM，就会表现为父级依赖、BOM、插件配置没有被扫描到。

### 建议处理

将 `exchange-business/pom.xml` 改为聚合模块：

```xml
<packaging>pom</packaging>
```

并补上正确父路径：

```xml
<parent>
    <groupId>coin.exchange</groupId>
    <artifactId>exchange-parent</artifactId>
    <version>1.0</version>
    <relativePath>../exchange-parent/pom.xml</relativePath>
</parent>
```

`exchange-business-user/pom.xml` 的父级是 `exchange-business`，默认 `relativePath` 会指向 `../pom.xml`，这个路径是正确的；如果希望更显式，也可以写：

```xml
<relativePath>../pom.xml</relativePath>
```

### 验证命令

修复后执行：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-business-user -am validate -DskipTests
```

如果需要继续确认依赖和编译：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-business-user -am compile -DskipTests
```

## 最终验证

执行：

```bash
mvn -f exchange-parent/pom.xml compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

通过模块：

```text
exchange-parent
exchange-common
exchange-common-core
exchange-common-redis
exchange-api
exchange-gateway
exchange-auth
```

## 问题 6 修复记录：exchange-business-user 父级依赖扫描

### 用户问题

用户要求：

```text
帮我修复一下
```

上下文是上一节记录的 `exchange-business-user` 没有扫描到父级依赖。

### 实际修复

修改 `exchange-business/pom.xml`：

- 给父 POM 增加正确相对路径：

```xml
<relativePath>../exchange-parent/pom.xml</relativePath>
```

- 将聚合模块打包类型从 `jar` 改为 `pom`：

```xml
<packaging>pom</packaging>
```

- 移除聚合父模块中的业务依赖，避免把父聚合模块当业务 jar 使用。

修改 `exchange-business/exchange-business-user/pom.xml`：

- 给父模块增加显式路径：

```xml
<relativePath>../pom.xml</relativePath>
```

- 将模块名称从错误的 `exchange-module-user` 改为：

```xml
<name>exchange-business-user</name>
```

- 增加业务服务启动和调用需要的依赖：

```xml
<dependency>
    <groupId>coin.exchange</groupId>
    <artifactId>exchange-common-core</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### 验证结果

单模块链路编译通过：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-business-user -am compile -DskipTests
```

全项目编译通过：

```bash
mvn -f exchange-parent/pom.xml compile -DskipTests
```

最终 reactor 共 11 个模块通过：

```text
exchange-parent
exchange-common
exchange-common-core
exchange-common-redis
exchange-api
exchange-api-user
exchange-gateway
exchange-auth
exchange-module
exchange-business
exchange-business-user
```

## 后续问答记录规则

从本次开始，当前会话中关于该项目的排查、修复、验证结论都追加记录到本文档。

记录内容包括：

- 用户提出的问题或报错
- 定位到的原因
- 修改过的文件和关键改动
- 执行过的验证命令
- 最终结果

## 问题 7：exchange-common-core 没有指定 JDK

### 用户问题

用户反馈 IDEA 编译时报：

```text
java: 没有为模块 'exchange-common-core' 指定 JDK
```

### 定位过程

检查 IDEA 配置文件：

```bash
sed -n '1,180p' .idea/misc.xml
sed -n '1,220p' .idea/modules.xml
find . -name '*.iml' -print
```

发现：

- `.idea/misc.xml` 不存在，项目级 JDK 配置丢失
- `.idea/modules.xml` 中残留了不存在的旧模块路径
- `exchange-common-core.iml` 不是标准 Java 模块结构，缺少 `NewModuleRootManager` 和 `inheritedJdk`

同时 Maven 编译验证通过：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-common-core -am compile -DskipTests
```

说明问题在 IDEA 项目配置，不在 Maven 编译链路。

### 实际修复

新增 `.idea/misc.xml`，恢复项目级 JDK 和 Maven 导入入口：

```xml
<component name="MavenProjectsManager">
  <option name="originalFiles">
    <list>
      <option value="$PROJECT_DIR$/exchange-parent/pom.xml" />
    </list>
  </option>
</component>
<component name="ProjectRootManager" version="2" languageLevel="JDK_17" default="true" project-jdk-name="ms-17" project-jdk-type="JavaSDK">
  <output url="file://$PROJECT_DIR$/out" />
</component>
```

清理 `.idea/modules.xml` 中不存在的旧模块引用，只保留当前存在的 `exchange-common-core.iml`。

修复 `exchange-common/exchange-common-core/exchange-common-core.iml`：

```xml
<module type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://$MODULE_DIR$">
      <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
      <sourceFolder url="file://$MODULE_DIR$/src/main/resources" type="java-resource" />
      <excludeFolder url="file://$MODULE_DIR$/target" />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
  </component>
</module>
```

### 验证结果

Maven 侧验证通过：

```bash
mvn -f exchange-parent/pom.xml -pl :exchange-common-core -am compile -DskipTests
```

结果：

```text
BUILD SUCCESS
```

### 备注

如果 IDEA 仍显示旧错误，需要在 IDEA 中执行 Maven Reload，或重新打开项目，使 `.idea/misc.xml` 和 `.iml` 的 JDK 配置重新加载。
