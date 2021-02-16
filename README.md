# 第一天

使用netty实现一个游戏服务器，地址为127.0.0.1，端口为12345，游戏前端测试地址为

http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1

测试结果如图：

![image-20210201162119083](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201162119083.png)

服务器代码如下：

```java
package com.qzx.herostory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//测试地址：http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1
public class ServerMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            ChannelFuture future = bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(65536),
                                    new WebSocketServerProtocolHandler("/websocket"),
                                    new GameMsgHandler()
                            );
                        }
                    })
                    .bind(12345).sync();
            if(future.isSuccess()){
                LOGGER.info("服务器启动成功");
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }
}
```

# 第二天

使用protobuf自定义消息协议，首先下载工具

https://github.com/protocolbuffers/protobuf/releases/download/v3.10.0-rc1/protoc-3.10.0-rc-1-win64.zip

然后解压到D盘，配置环境变量

![image-20210201162414395](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201162414395.png)

测试工具：

![image-20210201162431158](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201162431158.png)

编写消息协议文件GameMsgProtocol.proto

```protobuf
syntax = "proto3";

package msg;
option java_package = "com.qzx.herostory.msg";

// 消息代号
enum MsgCode {
    USER_ENTRY_CMD = 0;
    USER_ENTRY_RESULT = 1;
    WHO_ELSE_IS_HERE_CMD = 2;
    WHO_ELSE_IS_HERE_RESULT = 3;
    USER_MOVE_TO_CMD = 4;
    USER_MOVE_TO_RESULT = 5;
    USER_QUIT_RESULT = 6;
    USER_STOP_CMD = 7;
    USER_STOP_RESULT = 8;
    USER_ATTK_CMD = 9;
    USER_ATTK_RESULT = 10;
    USER_SUBTRACT_HP_RESULT = 11;
    USER_DIE_RESULT = 12;
};

// 
// 用户入场
///////////////////////////////////////////////////////////////////////
// 指令
message UserEntryCmd {
    // 用户 Id
    uint32 userId = 1;
    // 英雄形象
    string heroAvatar = 2;
}

// 结果
message UserEntryResult {
    // 用户 Id
    uint32 userId = 1;
    // 英雄形象
    string heroAvatar = 2;
}

//
// 还有谁在场
///////////////////////////////////////////////////////////////////////
// 指令
message WhoElseIsHereCmd {
}

// 结果
message WhoElseIsHereResult {
    // 用户信息数组
    repeated UserInfo userInfo = 1;

    // 用户信息
    message UserInfo {
        // 用户 Id
        uint32 userId = 1;
        // 英雄形象
        string heroAvatar = 2;
    }
}

// 
// 用户移动
///////////////////////////////////////////////////////////////////////
// 指令
message UserMoveToCmd {
    // 
    // XXX 注意: 用户移动指令中没有用户 Id,
    // 这是为什么?
    // 
    // 移动到位置 X
    float moveToPosX = 1;
    // 移动到位置 Y
    float moveToPosY = 2;
}

// 结果
message UserMoveToResult {
    // 移动用户 Id
    uint32 moveUserId = 1;
    // 移动到位置 X
    float moveToPosX = 2;
    // 移动到位置 Y
    float moveToPosY = 3;
}

// 
// 用户退场
///////////////////////////////////////////////////////////////////////
// 
// XXX 注意: 用户退场不需要指令, 因为是在断开服务器的时候执行
// 
// 结果
message UserQuitResult {
    // 退出用户 Id
    uint32 quitUserId = 1;
}

//
// 用户停驻
///////////////////////////////////////////////////////////////////////
// 指令
message UserStopCmd {
}

// 结果
message UserStopResult {
    // 停驻用户 Id
    uint32 stopUserId = 1;
    // 停驻在位置 X
    float stopAtPosX = 2;
    // 停驻在位置 Y
    float stopAtPosY = 3;
}

//
// 用户攻击
///////////////////////////////////////////////////////////////////////
// 指令
message UserAttkCmd {
    // 目标用户 Id
    uint32 targetUserId = 1;
}

// 结果
message UserAttkResult {
    // 发动攻击的用户 Id
    uint32 attkUserId = 1;
    // 目标用户 Id
    uint32 targetUserId = 2;
}

// 用户减血结果
message UserSubtractHpResult {
    // 目标用户 Id
    uint32 targetUserId = 1;
    // 减血量
    uint32 subtractHp = 2;
}

// 死亡结果
message UserDieResult {
    // 目标用户 Id
    uint32 targetUserId = 1;
}

```

执行命令

```shell
protoc --java_out=D:\herostory\src\main\java\ GameMsgProtocol.proto
```

![image-20210201163816507](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201163816507.png)

可以看到成功生成java文件

![image-20210201163925547](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201163925547.png)

**该自定义消息的特点是前两个字节代表消息体的长度，后两个字节代表消息的编号，最后部分代表了消息体。**

## 消息解码器

根据服务器打印的日志可以发现，客户端发送的消息类型为BinaryWebSocketFrame类型，我们需要将其转化为ByteBuf才能进行处理，为此添加消息解码器GameMsgDecoder，目前只对客户端登陆服务端的消息进行处理

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 18:15
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(ctx==null||msg==null) return;
        if(!(msg instanceof BinaryWebSocketFrame)) return;

        try {
            BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
            ByteBuf content = webSocketFrame.content();// 获取消息内容
            content.readShort();// 先读取消息长度
            int msgCode = content.readShort();// 再读取消息编号,代表了消息类型
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);// 读取消息体
            // 根据不同的消息编号进行封装，GeneratedMessageV3为所有消息的父类
            GeneratedMessageV3 cmd = null;
            switch (msgCode) {
                // 入场消息
                case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
                    cmd = GameMsgProtocol.UserEntryCmd.parseFrom(bytes);
                    break;
                default:
                    break;
            }

            if (cmd != null) {
                // 处理完成后，传递该消息给下一个处理器
                ctx.fireChannelRead(cmd);
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}

```

## 消息编码器

考虑到有多个用户会登陆到同一个服务端，并且在当前用户界面展现其他用户，所以需要广播接收到的当前登录的客户端到所有已经登陆的客户端上，为此需要编码器将消息封装为BinaryWebSocketFrame，从而发送给服务端。

由于目前只考虑登陆消息，在**消息处理器**获得解码后的消息后就直接广播登录响应消息GameMsgProtocol.UserEntryResult给所有的客户端，对于每一个接受到的channel分别进行解码然后转发即可。

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 19:13
 * @Description: 消息编码器
 * @version: 1.0
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(ctx==null||msg==null) return;
        if(!(msg instanceof GeneratedMessageV3)){
            super.write(ctx, msg, promise);
            return;
        }

        try {
            int msgCode = -1;// 消息类型
            if(msg instanceof GameMsgProtocol.UserEntryResult){
                // 入场返回消息
                GameMsgProtocol.UserEntryResult result = (GameMsgProtocol.UserEntryResult) msg;
                byte[] content = result.toByteArray();// 消息体
                msgCode = GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE;
                // 包装成ByteBuf
                ByteBuf buffer = ctx.alloc().buffer();
                buffer.writeShort((short)content.length);// 设置消息长度
                buffer.writeShort((short)msgCode);// 设置消息编号
                buffer.writeBytes(content);// 设置消息体
                // 封装为BinaryWebSocketFrame发送给客户端
                BinaryWebSocketFrame webSocketFrame = new BinaryWebSocketFrame(buffer);
                ctx.writeAndFlush(webSocketFrame);
            }else{
                LOGGER.error("无法识别的消息: msg:{}",msg);
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}

```



## 消息处理器

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);
    /**
     * 客户端信道数组, 一定要使用 static, 否则无法实现群发
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 将当前channel加入到group中进行管理
        CHANNELS.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(channelHandlerContext==null||o==null) return;
        if(!(o instanceof GeneratedMessageV3)) return; // 不是protobuf类型的消息返回

        try {
            if(o instanceof GameMsgProtocol.UserEntryCmd){
                // 入场消息
                int userId = ((GameMsgProtocol.UserEntryCmd) o).getUserId();
                String heroAvatar = ((GameMsgProtocol.UserEntryCmd) o).getHeroAvatar();
                // 构建一个UserEntryResult对象
                GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
                builder.setUserId(userId);
                builder.setHeroAvatar(heroAvatar);
                GameMsgProtocol.UserEntryResult userEntryResult = builder.build();
                // 将该消息进行广播
                CHANNELS.writeAndFlush(userEntryResult);
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }

        LOGGER.info("收到消息：msg:{}",o);
    }
}

```

## 测试1（问题发现）

访问http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1和http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=2，可以发现

![image-20210201194453377](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201194453377.png)

**两个客户端中只有一个客户端接受到了另外一个客户端登陆的消息，而另外一个却没有，这是因为第一个先登陆的消息在广播的时候第二个客户端并没有登陆所以没有接受到。**

## 解决方案

每一次登陆的客户端向服务器发送“谁还在”的消息，从而获取登陆列表。

那么首先得使用User对象存储所有的用户，并且使用用户字典记录所有进行登录的用户(在接受到登陆消息的时候记录)，然后对于客户端发送到的WhoElseIsHereCmd消息需要添加解码方案，具体做法就是和登陆一样，只需要使用GameMsgProtocol.WhoElseIsHereCmd.parseFrom方法进行解析就好，将解码后的数据进行传递给消息处理器，对于消息处理器也需要针对WhoElseIsHereCmd消息给出处理方案，具体的做法就是构建一个WhoElseIsHereResult消息，将用户字典中所有已经登陆的用户信息添加到其中进行返回，而对于编码器只需要更改msgCode指定消息类型即可。

### 解码器

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 18:15
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(ctx==null||msg==null) return;
        if(!(msg instanceof BinaryWebSocketFrame)) return;

        try {
            BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
            ByteBuf content = webSocketFrame.content();// 获取消息内容
            content.readShort();// 先读取消息长度
            int msgCode = content.readShort();// 再读取消息编号,代表了消息类型
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);// 读取消息体
            // 根据不同的消息编号进行封装，GeneratedMessageV3为所有消息的父类
            GeneratedMessageV3 cmd = null;
            switch (msgCode) {
                // 入场消息
                case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
                    cmd = GameMsgProtocol.UserEntryCmd.parseFrom(bytes);
                    break;
                // 谁还在消息
                case GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE:
                    cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(bytes);
                    break;
                default:
                    break;
            }

            if (cmd != null) {
                // 处理完成后，传递该消息给下一个处理器
                ctx.fireChannelRead(cmd);
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}

```

### 处理器

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);
    /**
     * 客户端信道数组, 一定要使用 static, 否则无法实现群发
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 用户字典，存储所有已经登陆的用户信息
     */
    private static ConcurrentHashMap<Integer,User> USER_MAP = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 将当前channel加入到group中进行管理
        CHANNELS.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(channelHandlerContext==null||o==null) return;
        if(!(o instanceof GeneratedMessageV3)) return; // 不是protobuf类型的消息返回

        try {
            if(o instanceof GameMsgProtocol.UserEntryCmd){
                // 入场消息
                int userId = ((GameMsgProtocol.UserEntryCmd) o).getUserId();
                String heroAvatar = ((GameMsgProtocol.UserEntryCmd) o).getHeroAvatar();
                // 存储该登陆用户
                USER_MAP.putIfAbsent(userId,new User(userId,heroAvatar));
                // 构建一个UserEntryResult对象
                GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
                builder.setUserId(userId);
                builder.setHeroAvatar(heroAvatar);
                GameMsgProtocol.UserEntryResult userEntryResult = builder.build();
                // 将该消息进行广播
                CHANNELS.writeAndFlush(userEntryResult);
            }else if(o instanceof GameMsgProtocol.WhoElseIsHereCmd){
                // 谁还在消息
                // 构建一个WhoElseIsHereResult消息进行返回
                GameMsgProtocol.WhoElseIsHereResult.Builder builder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
                // 在WhoElseIsHereResult消息中将所有用户字典的用户添加到UserInfo中
                for (User user : USER_MAP.values()) {
                    if(user==null) continue;
                    GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                    userInfoBuilder.setUserId(user.getUserId());
                    userInfoBuilder.setHeroAvatar(user.getHeroAvatar());
                    builder.addUserInfo(userInfoBuilder);
                }
                // 返回消息(无需广播)
                channelHandlerContext.writeAndFlush(builder.build());
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }

        LOGGER.info("收到消息：msg:{}",o);
    }
}

```

### 编码器

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 19:13
 * @Description: 消息编码器
 * @version: 1.0
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(ctx==null||msg==null) return;
        if(!(msg instanceof GeneratedMessageV3)){
            super.write(ctx, msg, promise);
            return;
        }

        try {
            int msgCode = -1;// 消息类型
            GeneratedMessageV3 result = (GeneratedMessageV3) msg;
            if(msg instanceof GameMsgProtocol.UserEntryResult){
                // 入场返回消息
                msgCode = GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE;
            }else if(msg instanceof GameMsgProtocol.WhoElseIsHereResult){
                // 谁还在消息
                msgCode = GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE;
            } else{
                LOGGER.error("无法识别的消息: msg:{}",msg);
                return;
            }
            byte[] content = result.toByteArray();// 消息体
            // 包装成ByteBuf
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeShort((short)content.length);// 设置消息长度
            buffer.writeShort((short)msgCode);// 设置消息编号
            buffer.writeBytes(content);// 设置消息体
            // 封装为BinaryWebSocketFrame发送给客户端
            BinaryWebSocketFrame webSocketFrame = new BinaryWebSocketFrame(buffer);
            ctx.writeAndFlush(webSocketFrame);
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}

```

### User对象

```java
package com.qzx.herostory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 19:59
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public class User {
    private Integer userId;
    private String heroAvatar;

    public User(Integer userId, String heroAvatar) {
        this.userId = userId;
        this.heroAvatar = heroAvatar;
    }

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", heroAvatar='" + heroAvatar + '\'' +
                '}';
    }
}

```

## 测试2（问题解决）

再次访问http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1和http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=2可以看到客户端的角色已经同步。

![image-20210201201717917](https://github.com/qzx-pro/herostory/blob/master/src/main/resources/images/image-20210201201717917.png)

# 第三天

在昨天的代码中解决了多个用户登陆的问题，但是多个用户之间移动并同步的问题并未解决，首先处理当前角色移动对其他用户可见，同时在当前角色下线的时候通知用户下线的逻辑处理。

用户移动的消息处理逻辑为获取到解码后的消息后构建UserMoveToResult消息并返回，不过该消息需要一个移动客户的ID，所以在该用户登陆的时候得先将其ID存储在channel中。对于编码器只需要更新其msgCode表明是移动消息即可。

```java
// 记录当前用户ID到channel，相当于存储在session中
channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(userId);
```

用户下线的消息处理逻辑为首先将该用户从用户字典中移除，然后构建UserQuitResult消息进行群发，对于编码器只需要更新其msgCode表明是离线消息即可。

**注意：**用户下线无需接受命令，因为是客户端断开连接，在channel消失的时候就可以主动移除。

```java
	@Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) return;
        // 处理用户离线逻辑
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) return;

        // 将该用户从用户字典中移除
        USER_MAP.remove(userId);

        // 构建UserQuitResult消息
        GameMsgProtocol.UserQuitResult.Builder newBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        newBuilder.setQuitUserId(userId);

        // 群发该用户离线的消息
        CHANNELS.writeAndFlush(newBuilder.build());
    }
```

## 问题发现（已解决，见第四天）

不过现在依然存在一个问题，在用户离线并重新上线后，之前已经登陆的人没有在当前客户端中同步到正确位置而是初始位置。

## 重构（第一阶段）

可以看到消息处理器，解码器和编码器中存在冗长的if...else和switch...case语句，如果在后期添加新的消息处理逻辑只会让该语句块更加冗长，并且也不符合开闭原则。这里对这三个类进行重构，首先是消息处理器中的ChannelGroup对象抽取为BroadCaster广播工具类。

```java
package com.qzx.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:38
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public final class BroadCaster {
    /**
     * 客户端信道数组, 一定要使用 static, 否则无法实现群发
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 私有化构造方法
     */
    private BroadCaster(){}
    /**
    * 功能描述: 添加channel到ChannelGroup中
    * @param: 待添加的channel
    * @return: void
    */
    public static void addChannel(Channel channel){
        if(channel==null) return;

        CHANNELS.add(channel);
    }
    /**
    * 功能描述: 从ChannelGroup中移除channel
    * @param: 待移除的channel
    * @return: void
    */
    public static void removeChannel(Channel channel){
        if(channel==null) return;

        CHANNELS.remove(channel);
    }
    /**
    * 功能描述 ：广播消息o
    * @param: 待广播的消息
    * @return: void
    */
    public static void broadcast(Object o){
        if(o==null) return;

        CHANNELS.writeAndFlush(o);
    }
}

```

将用户字典抽取成UserManager用户管理类，对于已经登陆的用户信息进行管理。

```java
package com.qzx.herostory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:50
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public final class UserManager {
    /**
     * 用户字典，存储所有已经登陆的用户信息
     */
    private static final ConcurrentHashMap<Integer, User> USER_MAP = new ConcurrentHashMap<>();
    /**
     * 私有化构造方法
     */
    private UserManager(){}
    /**
    * 功能描述 : 根据用户ID删除该用户
    * @param: 待删除的用户ID
    * @return: void
    */
    public static void removeByUserId(Integer userId){
        if(userId==null) return; 
        
        USER_MAP.remove(userId);
    }
    /**
     * 功能描述 : 存储登陆的用户
     * @param: 待存储的用户
     * @return: void
     */
    public static void save(User user){
        if(user==null) return;
        
        USER_MAP.putIfAbsent(user.getUserId(),user);
    }
    /**
    * 功能描述
    * @param: null
    * @return: 用户集合
    */
    public static Collection<User> getUserCollection(){
        return USER_MAP.values();
    }
}

```

### 重构消息处理器GameMsgHandler（静态Map）

对于消息处理器中存在着对不同的消息类型进行不同的消息处理逻辑，这导致if...else语句块冗长，并且会在有新的消息的时候需要修改处理器代码。

首先针对冗长问题进行处理，我们将每一个消息处理逻辑抽象到每一个处理类的handle方法中，比如UserEntryCmd消息的处理就使用UserEntryCmdHandler类中的handle方法进行处理，这样抽取后可以看到GameMsgHandler核心代码简化如下：

```java 
if (msg instanceof GameMsgProtocol.UserEntryCmd) {
    new UserEntryCmdHandler().handle(channelHandlerContext,(GeneratedMessageV3)msg);
} else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
    new WhoElseIsHereCmdHandler().handle(channelHandlerContext,(GeneratedMessageV3)msg);
} else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
    new UserMoveToCmdHandler().handle(channelHandlerContext,(GeneratedMessageV3)msg);
}
```

然后根据开闭原则抽取公共代码，可以看到在上面不同的handle调用从表明上看也就是只有前面的对象不一样，如果可以将这些对象抽象出一个接口让它们实现，这样就可以实现一次调用了。

**命令处理逻辑接口**

```java
public interface ICmdHandler<T extends GeneratedMessageV3> {
    /**
    * 功能描述 : 命令处理逻辑
    * @param: channle处理器上下文和待处理消息
    * @return: void
    */
    void handle(ChannelHandlerContext channelHandlerContext, T msg);
}
```

让每一个命令处理器实现该接口，然后修改GameMsgHandler核心代码如下：

```java
ICmdHandler cmdHandler = null;
if (msg instanceof GameMsgProtocol.UserEntryCmd) {
    cmdHandler = new UserEntryCmdHandler();
} else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
    cmdHandler = new WhoElseIsHereCmdHandler();
} else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
    cmdHandler = new UserMoveToCmdHandler();
}
if(cmdHandler!=null){
    cmdHandler.handle(channelHandlerContext,(GeneratedMessageV3)msg);
}
```

我们可以看到对于不同的命令我们现在依然需要构造不同的命令解析器，这些逻辑不应该出现在消息处理器中，这里使用一个工厂专门来生产对于的命令解析器。

```java
public final class CmdHandlerFactory {
    /**
     * 命令类型->命令解析器字典
     */
    private static final ConcurrentHashMap<Class<?>,ICmdHandler<? extends GeneratedMessageV3> > CMD_HANDLER_MAP = new ConcurrentHashMap<>();
    /**
     * 私有化构造方法
     */
    private CmdHandlerFactory(){}

    static {
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.UserEntryCmd.class,new UserEntryCmdHandler());
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.WhoElseIsHereCmd.class,new WhoElseIsHereCmdHandler());
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.UserMoveToCmd.class,new UserMoveToCmdHandler());
    }

    /**
    * 功能描述 : 创造命令对应的命令解析器
    * @param: 命令的字节码
    * @return: 命令处理器
    */
    public static ICmdHandler<? extends GeneratedMessageV3> createCmdHandler(Class<?> clazz){
        if(clazz==null) return null;

        return CMD_HANDLER_MAP.get(clazz);
    }
}
```

### 重构消息解/编码器GameMsgDecoder/GameMsgEncoder（静态Map）

消息解/编码器都是根据msgCode，也就是消息的类型来进行不同的处理，逻辑本身较为简单，可以直接使用一个消息识别类GameMsgRecognizer同时完成消息构建器获取getMsgBuilderByMsgCode和消息类型获取getMsgCodeByMsgClazz的方法。

消息构建器的获取是利用每一个GeneratedMessageV3消息对象都有一个newBuilderForType()，这样我们将每一个传入的消息类型msgCode与消息对象进行一一映射，这样获得消息对象后就可以使用newBuilderForType()获得对应的消息构建器了。

### 消息识别器

```java
public final class GameMsgRecognizer {
    /**
     * msgCode->消息对象字典
     */
    private static final ConcurrentHashMap<Integer, GeneratedMessageV3> MSGCODE_MESSAGE_MAP = new ConcurrentHashMap<>();
    /**
     * GeneratedMessageV3消息字节码->msgCode
     */
    private static final ConcurrentHashMap<Class<?>,Integer> CLAZZ_MSGCODE_MAP = new ConcurrentHashMap<>();
    /**
     * 私有化构造方法
     */
    private GameMsgRecognizer(){}

    static {
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE,GameMsgProtocol.UserEntryCmd.getDefaultInstance());
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE,GameMsgProtocol.WhoElseIsHereCmd.getDefaultInstance());
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE,GameMsgProtocol.UserMoveToCmd.getDefaultInstance());

        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.UserEntryResult.class,GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE);
        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.WhoElseIsHereResult.class,GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE);
        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.UserMoveToResult.class,GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE);
    }

    /**
     * 根据消息类型获取对应的消息构建器
     * @param msgCode 消息类型
     * @return Message.Builder 消息构建器
     */
    public static Message.Builder getMsgBuilderByMsgCode(int msgCode){
        if(msgCode<0) return null;

        return MSGCODE_MESSAGE_MAP.get(msgCode).newBuilderForType();
    }

    /**
     * 根据消息字节码获取对应的消息类型
     * @param clazz 消息字节码
     * @return msgCode
     */
    public static int getMsgCodeByMsgClazz(Class<?> clazz){
        if(clazz==null) return -1;
        return CLAZZ_MSGCODE_MAP.get(clazz);
    }
}
```

```java
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx == null || msg == null) return;
        if (!(msg instanceof BinaryWebSocketFrame)) return;

        try {
            BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
            ByteBuf content = webSocketFrame.content();// 获取消息内容

            content.readShort();// 先读取消息长度
            int msgCode = content.readShort();// 再读取消息编号,代表了消息类型
            byte[] msgBody = new byte[content.readableBytes()];
            content.readBytes(msgBody);// 读取消息体

            // 获取消息构建器
            Message.Builder builder = GameMsgRecognizer.getMsgBuilderByMsgCode(msgCode);
            if(builder == null) return;

            // 构建消息
            builder.mergeFrom(msgBody);
            Message message = builder.build();

            if (message != null) {
                // 处理完成后，传递该消息给下一个处理器
                ctx.fireChannelRead(message);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
```

```java
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (ctx == null || msg == null) return;
        if (!(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }

        try {
            // 获取消息类型
            int msgCode = GameMsgRecognizer.getMsgCodeByMsgClazz(msg.getClass());

            if(msgCode<0){
                LOGGER.error(
                        "无法识别的消息, msgClazz = {}",
                        msg.getClass().getName()
                );
                return;
            }
            // 获取消息体
            GeneratedMessageV3 result = (GeneratedMessageV3) msg;
            byte[] content = result.toByteArray();

            // 包装成ByteBuf
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeShort((short) content.length);// 设置消息长度
            buffer.writeShort((short) msgCode);// 设置消息编号
            buffer.writeBytes(content);// 设置消息体

            // 封装为BinaryWebSocketFrame发送给客户端
            BinaryWebSocketFrame webSocketFrame = new BinaryWebSocketFrame(buffer);
            ctx.writeAndFlush(webSocketFrame);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
```

现阶段算是完成了第一阶段的重构了，之后再需要添加业务逻辑，只需要增加map中的映射关系即可，但是依然需要修改工厂和消息识别器，仍然不满足开闭原则，第二阶段需要使用反射完成Map的动态构建过程，对于新添加的业务逻辑只需要添加新的handler处理器即可。

## 重构（第二阶段）

### 重构消息识别器GameMsgRecognizer（反射）

```java
public final class GameMsgRecognizer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);
    /**
     * msgCode->消息对象字典
     */
    private static final ConcurrentHashMap<Integer, GeneratedMessageV3> MSGCODE_MESSAGE_MAP = new ConcurrentHashMap<>();
    /**
     * GeneratedMessageV3消息类->msgCode
     */
    private static final ConcurrentHashMap<Class<?>, Integer> CLAZZ_MSGCODE_MAP = new ConcurrentHashMap<>();

    /**
     * 私有化构造方法
     */
    private GameMsgRecognizer() {
    }

    /**
     * 初始化MAP
     */
    public static void init(){
        LOGGER.info("开始初始化GameMsgRecognizer");
        // 获取GameMsgProtocol所有的内部类
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();
        GameMsgProtocol.MsgCode[] msgCodes = GameMsgProtocol.MsgCode.values();
        for (Class<?> clazz : innerClazzArray) {
             // 如果不是消息类就跳过
            if(clazz==null||
                    !GeneratedMessageV3.class.isAssignableFrom(clazz)
            ){
                continue;
            }
            // 获取简单类名小写
            String clazzName = clazz.getSimpleName().toLowerCase();

            for (GameMsgProtocol.MsgCode msgCode : msgCodes) {
                // 将所有的下划线去除并转化为小写
                String msgCodeString = msgCode.toString().replaceAll("_", "").toLowerCase();

                if(msgCodeString.startsWith(clazzName)){
                    // 对应上消息类型和消息类名称
                    try {
                        // 调用clazz类的getDefaultInstance方法获得对象
                        Object instance = clazz.getDeclaredMethod("getDefaultInstance").invoke(clazz);

                        // 存储Map
                        MSGCODE_MESSAGE_MAP.putIfAbsent(msgCode.getNumber(),(GeneratedMessageV3) instance);
                        CLAZZ_MSGCODE_MAP.putIfAbsent(clazz,msgCode.getNumber());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(),e);
                    }
                }
            }

        }
        LOGGER.info("GameMsgRecognizer初始化完成");
    }

    /**
     * 根据消息类型获取对应的消息构建器
     *
     * @param msgCode 消息类型
     * @return Message.Builder 消息构建器
     */
    public static Message.Builder getMsgBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }

        return MSGCODE_MESSAGE_MAP.get(msgCode).newBuilderForType();
    }

    /**
     * 根据消息类获取对应的消息类型
     *
     * @param clazz 消息字节码
     * @return msgCode
     */
    public static int getMsgCodeByMsgClazz(Class<?> clazz) {
        if (clazz == null) {
            return -1;
        }
        Integer result = CLAZZ_MSGCODE_MAP.get(clazz);
        return result == null ? -1 : result;
    }
}
```

### 重构命令处理器工厂CmdHandlerFactory（反射）

在这个地方使用反射有两种方式，第一种就是和消息识别器一样根据命名规范来完成类名的获取从而获得处理器对象，第二种方式是获取所有处理器实现接口ICmdHandler的泛型，然后根据其泛型来进行反射获取处理器对象，之所以可以这么做是因为ICmdHandler的泛型就是当前处理器处理的命令类型，而我们恰好需要的就是命令类型到命令处理器的映射关系。

#### 依赖命名规范使用反射

```java
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 命令类型->命令解析器字典
     */
    private static final ConcurrentHashMap<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> CMD_HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * 私有化构造方法
     */
    private CmdHandlerFactory() {
    }

    /**
     * 初始化Map
     */
    public static void init() {
        LOGGER.info("开始初始化CmdHandlerFactory");
        // 获取GameMsgProtocol所有的内部类
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();
        for (Class<?> clazz : innerClazzArray) {
            // 如果不是消息类就跳过
            if (clazz == null ||
                    !GeneratedMessageV3.class.isAssignableFrom(clazz)
            ) {
                continue;
            }

            try {
                // 根据资源路径名称获取资源对象
                String path = CmdHandlerFactory.class.getPackage().getName().replaceAll("\\.", "/") + "/" + clazz.getSimpleName() + "Handler.class";
                URL resource = CmdHandlerFactory.class.getResource("/" + path);

                if (resource == null) {
                    continue;
                }

                // 获取全限定类名称
                String[] strings = resource.toString().replaceAll("/", ".").split("\\.");
                StringBuilder fullClassName = new StringBuilder();
                for (int i = 5; i < strings.length - 1; ++i) {
                    fullClassName.append(strings[i]);
                    if (i < strings.length - 2) {
                        fullClassName.append(".");
                    }
                }
                // 获取clazz对应的handler类对象
                Class<?> handlerClazz = CmdHandlerFactory.class.getClassLoader().loadClass(fullClassName.toString());
                Object handlerInstance = handlerClazz.getConstructor().newInstance();
                // 存储clazz->handler映射关系
                CMD_HANDLER_MAP.putIfAbsent(clazz, (ICmdHandler<? extends GeneratedMessageV3>) handlerInstance);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("初始化CmdHandlerFactory完成");
    }

    /**
     * 功能描述 : 创造命令对应的命令解析器
     *
     * @param: 命令的字节码
     * @return: 命令处理器
     */
    public static ICmdHandler<? extends GeneratedMessageV3> createCmdHandler(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return CMD_HANDLER_MAP.get(clazz);
    }
}
```

#### 依赖ICmdHandler接口的泛型使用反射

为了获取ICmdHandler接口的泛型，我们需要知道在cmdHandler包下有多少实现了ICmdHandler接口的类，所以需要一个工具类PackageUtil,通过listSubClazz方法来获取CmdHandlerFactory所在目录下的所有实现了ICmdHandler接口的子类。

```java 
package com.qzx.herostory.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author: qzx
 * @date: 2021/2/3 - 02 - 03 - 12:16
 * @description: 名称空间实用工具
 * @version: 1.0
 */
public final class PackageUtil {
    /**
     * 类默认构造器
     */
    private PackageUtil() {
    }

    /**
     * 列表指定包中的所有子类
     *
     * @param packageName 包名称
     * @param recursive   是否递归查找
     * @param superClazz  父类的类型
     * @return 子类集合
     */
    static public Set<Class<?>> listSubClazz(
            String packageName,
            boolean recursive,
            Class<?> superClazz) {
        if (superClazz == null) {
            return Collections.emptySet();
        } else {
            return listClazz(packageName, recursive, superClazz::isAssignableFrom);
        }
    }

    /**
     * 列表指定包中的所有类
     *
     * @param packageName 包名称
     * @param recursive   是否递归查找?
     * @param filter      过滤器
     * @return 符合条件的类集合
     */
    static public Set<Class<?>> listClazz(
            String packageName, boolean recursive, IClazzFilter filter) {

        if (packageName == null ||
                packageName.isEmpty()) {
            return null;
        }

        // 将点转换成斜杠
        final String packagePath = packageName.replace('.', '/');
        // 获取类加载器
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // 结果集合
        Set<Class<?>> resultSet = new HashSet<>();

        try {
            // 获取 URL 枚举
            Enumeration<URL> urlEnum = cl.getResources(packagePath);

            while (urlEnum.hasMoreElements()) {
                // 获取当前 URL
                URL currUrl = urlEnum.nextElement();
                // 获取协议文本
                final String protocol = currUrl.getProtocol();
                // 定义临时集合
                Set<Class<?>> tmpSet = null;

                if ("FILE".equalsIgnoreCase(protocol)) {
                    // 从文件系统中加载类
                    tmpSet = listClazzFromDir(
                            new File(currUrl.getFile()), packageName, recursive, filter
                    );
                } else if ("JAR".equalsIgnoreCase(protocol)) {
                    // 获取文件字符串
                    String fileStr = currUrl.getFile();

                    if (fileStr.startsWith("file:")) {
                        // 如果是以 "file:" 开头的,
                        // 则去除这个开头
                        fileStr = fileStr.substring(5);
                    }

                    if (fileStr.lastIndexOf('!') > 0) {
                        // 如果有 '!' 字符,
                        // 则截断 '!' 字符之后的所有字符
                        fileStr = fileStr.substring(0, fileStr.lastIndexOf('!'));
                    }

                    // 从 JAR 文件中加载类
                    tmpSet = listClazzFromJar(
                            new File(fileStr), packageName, recursive, filter
                    );
                }

                if (tmpSet != null) {
                    // 如果类集合不为空,
                    // 则添加到结果中
                    resultSet.addAll(tmpSet);
                }
            }
        } catch (Exception ex) {
            // 抛出异常!
            throw new RuntimeException(ex);
        }

        return resultSet;
    }

    /**
     * 从目录中获取类列表
     *
     * @param dirFile     目录
     * @param packageName 包名称
     * @param recursive   是否递归查询子包
     * @param filter      类过滤器
     * @return 符合条件的类集合
     */
    static private Set<Class<?>> listClazzFromDir(
            final File dirFile, final String packageName, final boolean recursive, IClazzFilter filter) {

        if (!dirFile.exists() ||
                !dirFile.isDirectory()) {
            // 如果参数对象为空,
            // 则直接退出!
            return null;
        }

        // 获取子文件列表
        File[] subFileArr = dirFile.listFiles();

        if (subFileArr == null ||
                subFileArr.length <= 0) {
            return null;
        }

        // 文件队列, 将子文件列表添加到队列
        Queue<File> fileQ = new LinkedList<>(Arrays.asList(subFileArr));

        // 结果对象
        Set<Class<?>> resultSet = new HashSet<>();

        while (!fileQ.isEmpty()) {
            // 从队列中获取文件
            File currFile = fileQ.poll();

            if (currFile.isDirectory() &&
                    recursive) {
                // 如果当前文件是目录,
                // 并且是执行递归操作时,
                // 获取子文件列表
                subFileArr = currFile.listFiles();

                if (subFileArr != null &&
                        subFileArr.length > 0) {
                    // 添加文件到队列
                    fileQ.addAll(Arrays.asList(subFileArr));
                }
                continue;
            }

            if (!currFile.isFile() ||
                    !currFile.getName().endsWith(".class")) {
                // 如果当前文件不是文件,
                // 或者文件名不是以 .class 结尾,
                // 则直接跳过
                continue;
            }

            // 类名称
            String clazzName;

            // 设置类名称
            clazzName = currFile.getAbsolutePath();
            // 清除最后的 .class 结尾
            clazzName = clazzName.substring(dirFile.getAbsolutePath().length(), clazzName.lastIndexOf('.'));
            // 转换目录斜杠
            clazzName = clazzName.replace('\\', '/');
            // 清除开头的 /
            clazzName = trimLeft(clazzName, "/");
            // 将所有的 / 修改为 .
            clazzName = join(clazzName.split("/"), ".");
            // 包名 + 类名
            clazzName = packageName + "." + clazzName;

            try {
                // 加载类定义
                Class<?> clazzObj = Class.forName(clazzName);

                if (null != filter &&
                        !filter.accept(clazzObj)) {
                    // 如果过滤器不为空,
                    // 且过滤器不接受当前类,
                    // 则直接跳过!
                    continue;
                }

                // 添加类定义到集合
                resultSet.add(clazzObj);
            } catch (Exception ex) {
                // 抛出异常
                throw new RuntimeException(ex);
            }
        }

        return resultSet;
    }

    /**
     * 从 .jar 文件中获取类列表
     *
     * @param jarFilePath .jar 文件路径
     * @param recursive   是否递归查询子包
     * @param filter      类过滤器
     * @return 符合条件的类集合
     */
    static private Set<Class<?>> listClazzFromJar(
            final File jarFilePath, final String packageName, final boolean recursive, IClazzFilter filter) {

        if (jarFilePath == null ||
                jarFilePath.isDirectory()) {
            // 如果参数对象为空,
            // 则直接退出!
            return null;
        }

        // 结果对象
        Set<Class<?>> resultSet = new HashSet<>();

        try {
            // 创建 .jar 文件读入流
            JarInputStream jarIn = new JarInputStream(new FileInputStream(jarFilePath));
            // 进入点
            JarEntry entry;

            while ((entry = jarIn.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                // 获取进入点名称
                String entryName = entry.getName();

                if (!entryName.endsWith(".class")) {
                    // 如果不是以 .class 结尾,
                    // 则说明不是 JAVA 类文件, 直接跳过!
                    continue;
                }

                if (!recursive) {
                    //
                    // 如果没有开启递归模式,
                    // 那么就需要判断当前 .class 文件是否在指定目录下?
                    //
                    // 获取目录名称
                    String tmpStr = entryName.substring(0, entryName.lastIndexOf('/'));
                    // 将目录中的 "/" 全部替换成 "."
                    tmpStr = join(tmpStr.split("/"), ".");

                    if (!packageName.equals(tmpStr)) {
                        // 如果包名和目录名不相等,
                        // 则直接跳过!
                        continue;
                    }
                }

                String clazzName;

                // 清除最后的 .class 结尾
                clazzName = entryName.substring(0, entryName.lastIndexOf('.'));
                // 将所有的 / 修改为 .
                clazzName = join(clazzName.split("/"), ".");

                // 加载类定义
                Class<?> clazzObj = Class.forName(clazzName);

                if (null != filter &&
                        !filter.accept(clazzObj)) {
                    // 如果过滤器不为空,
                    // 且过滤器不接受当前类,
                    // 则直接跳过!
                    continue;
                }

                // 添加类定义到集合
                resultSet.add(clazzObj);
            }

            // 关闭 jar 输入流
            jarIn.close();
        } catch (Exception ex) {
            // 抛出异常
            throw new RuntimeException(ex);
        }

        return resultSet;
    }

    /**
     * 类名称过滤器
     *
     * @author hjj2019
     */
    @FunctionalInterface
    static public interface IClazzFilter {
        /**
         * 是否接受当前类?
         *
         * @param clazz 被筛选的类
         * @return 是否符合条件
         */
        boolean accept(Class<?> clazz);
    }

    /**
     * 使用连接符连接字符串数组
     *
     * @param strArr 字符串数组
     * @param conn   连接符
     * @return 连接后的字符串
     */
    static private String join(String[] strArr, String conn) {
        if (null == strArr ||
                strArr.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < strArr.length; i++) {
            if (i > 0) {
                // 添加连接符
                sb.append(conn);
            }

            // 添加字符串
            sb.append(strArr[i]);
        }

        return sb.toString();
    }

    /**
     * 清除源字符串左边的字符串
     *
     * @param src     原字符串
     * @param trimStr 需要被清除的字符串
     * @return 清除后的字符串
     */
    static private String trimLeft(String src, String trimStr) {
        if (null == src ||
                src.isEmpty()) {
            return "";
        }

        if (null == trimStr ||
                trimStr.isEmpty()) {
            return src;
        }

        if (src.equals(trimStr)) {
            return "";
        }

        while (src.startsWith(trimStr)) {
            src = src.substring(trimStr.length());
        }

        return src;
    }
}


```

```java
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 命令类型->命令解析器字典
     */
    private static final ConcurrentHashMap<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> CMD_HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * 私有化构造方法
     */
    private CmdHandlerFactory() {
    }

    public static void init() {
//        initByNameSpace();
        initByGeneric();
    }

    /**
     * 根据泛型初始化Map
     */
    public static void initByGeneric() {
        LOGGER.info("开始初始化CmdHandlerFactory");
        // 获取当前factory的包名
        String packageName = CmdHandlerFactory.class.getPackage().getName();
        // 获取当前包名下所有实现了ICmdHandler的子类
        Set<Class<?>> listSubClazz = PackageUtil.listSubClazz(packageName, true, ICmdHandler.class);
        // 获取每一个子类中的泛型，也就是该子类的具体类型
        for (Class<?> subClazz : listSubClazz) {

            // 如果为空，抽象类或者接口就跳过
            if (subClazz == null ||
                    (subClazz.getModifiers() & Modifier.ABSTRACT) != 0 ||
                    subClazz.isInterface()) {
                continue;
            }
            // 获取子类的方法数组
            Method[] methods = subClazz.getDeclaredMethods();
            // 最终的handle方法的第二个参数，也就是该子类的泛型
            Class<?> msgType = null;

            for (Method method : methods) {
                if (!"handle".equals(method.getName())) {
                    // 不是handle方法就跳过
                    continue;
                }
                // 获取该类的方法参数数组
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length < 2 ||
                        (parameterTypes[1] == GeneratedMessageV3.class) ||
                        !GeneratedMessageV3.class.isAssignableFrom(parameterTypes[1])
                ) {
                    // 参数个数小于2，第二个参数是GeneratedMessageV3类型(父类)或者不是GeneratedMessageV3的子类都跳过
                    continue;
                }
                // parameterTypes[1]就是泛型,也即是命令类型
                msgType = parameterTypes[1];
                break;
            }

            if (msgType == null) {
                continue;
            }

            try {
                // 获取当前子类对象,并存储 命令类型msgType->命令解析器cmdHandler的映射
                ICmdHandler<?> cmdHandler = (ICmdHandler<?>) subClazz.newInstance();
                CMD_HANDLER_MAP.putIfAbsent(msgType, cmdHandler);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("初始化CmdHandlerFactory完成");
    }

    /**
     * 根据命名空间初始化Map
     */
    public static void initByNameSpace() {
        LOGGER.info("开始初始化CmdHandlerFactory");
        // 获取GameMsgProtocol所有的内部类
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();
        for (Class<?> clazz : innerClazzArray) {
            // 如果不是消息类就跳过
            if (clazz == null ||
                    !GeneratedMessageV3.class.isAssignableFrom(clazz)
            ) {
                continue;
            }

            try {
                // 根据资源路径名称获取资源对象
                String path = CmdHandlerFactory.class.getPackage().getName().replaceAll("\\.", "/") + "/" + clazz.getSimpleName() + "Handler.class";
                URL resource = CmdHandlerFactory.class.getResource("/" + path);

                if (resource == null) {
                    continue;
                }

                // 获取全限定类名称
                String[] strings = resource.toString().replaceAll("/", ".").split("\\.");
                StringBuilder fullClassName = new StringBuilder();
                for (int i = 5; i < strings.length - 1; ++i) {
                    fullClassName.append(strings[i]);
                    if (i < strings.length - 2) {
                        fullClassName.append(".");
                    }
                }
                // 获取clazz对应的handler类对象
                Class<?> handlerClazz = CmdHandlerFactory.class.getClassLoader().loadClass(fullClassName.toString());
                Object handlerInstance = handlerClazz.getConstructor().newInstance();
                // 存储clazz->handler映射关系
                CMD_HANDLER_MAP.putIfAbsent(clazz, (ICmdHandler<? extends GeneratedMessageV3>) handlerInstance);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("初始化CmdHandlerFactory完成");
    }

    /**
     * 功能描述 : 创造命令对应的命令解析器
     *
     * @param: 命令的字节码
     * @return: 命令处理器
     */
    public static ICmdHandler<? extends GeneratedMessageV3> createCmdHandler(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return CMD_HANDLER_MAP.get(clazz);
    }
}
```

这样就完成了整个代码的重构过程，现阶段只需要添加相应的命令处理器类就可以实现添加新的业务逻辑的需求了。

# 第四天

## 解决方案（第三天遗留）

由于在第三天中遗留了一个问题，在两个客户端移动角色之后，其中一个客户端如果刷新重进游戏后，出现在了初始位置，而不是最后移动的位置，为了解决这个问题，重新设计了消息本身，在发送移动消息的时候带上了开始移动的位置和时间，还有移动状态。

解决的原理：首先当客户端A的角色需要移动的时候会将当前角色的初始位置，目标位置和时间戳发送给服务器端，服务器端进行转发给客户端B，这样客户端B就可以根据该消息中初始位置和目标位置所确定的方向并结合当前收到的时间戳来绘制客户端A角色在每一时刻具体的位置，这样就可以同步A和B之间角色的移动状态，其次在B重新进入游戏的时候会发送WhoElseIsHereCmd消息来查询还有谁在游戏中的时候，服务器会将A客户端角色移动的状态带回给B，这样B收到这样的消息后就会同步出自己最新的位置。

重新设计的消息为GameMsgProtocol_new.proto，在终端输入如下命令即可重新生成消息协议类GameMsgProtocolNew

```shell
protoc --java_out=D:\herostory\src\main\java\ GameMsgProtocol_new.proto
```

修改代码中所有的GameMsgProtocol为GameMsgProtocolNew

为了实现上述方案，首先需要在本地使用MoveState类来存储用户的移动状态，并且将其内置到每一个用户对象中

```java
package com.qzx.herostory.model;

/**
 * @author: qzx
 * @date: 2021/2/4 - 02 - 04 - 16:16
 * @description: 用户的移动状态
 * @version: 1.0
 */
public class MoveState {
    /**
     * 移动起始位置X
     */
    private float fromPosX;

    /**
     * 移动起始位置Y
     */
    private float fromPosY;

    /**
     * 移动目标位置X
     */
    private float toPosX;

    /**
     * 移动目标位置Y
     */
    private float toPosY;

    /**
     * 移动起始时间
     */
    private long startTime;

    public float getFromPosX() {
        return fromPosX;
    }

    public void setFromPosX(float fromPosX) {
        this.fromPosX = fromPosX;
    }

    public float getFromPosY() {
        return fromPosY;
    }

    public void setFromPosY(float fromPosY) {
        this.fromPosY = fromPosY;
    }

    public float getToPosX() {
        return toPosX;
    }

    public void setToPosX(float toPosX) {
        this.toPosX = toPosX;
    }

    public float getToPosY() {
        return toPosY;
    }

    public void setToPosY(float toPosY) {
        this.toPosY = toPosY;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "MoveState{" +
                "fromPosX=" + fromPosX +
                ", fromPosY=" + fromPosY +
                ", toPosX=" + toPosX +
                ", toPosY=" + toPosY +
                ", startTime=" + startTime +
                '}';
    }
}

```

```java
public class User {
    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户头像
     */
    private String heroAvatar;

    /**
     * 用户的移动状态
     */
    private final MoveState moveState = new MoveState();
}
```

然后需要修改收到用户移动后的处理逻辑，首先需要为该移动用户保存其移动状态到本地，并将移动状态广播给所有的客户端。

```java
public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocolNew.UserMoveToCmd> {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolNew.UserMoveToCmd msg) {
        // 用户移动消息,构建UserMoveToResult消息
        GameMsgProtocolNew.UserMoveToResult.Builder newBuilder = GameMsgProtocolNew.UserMoveToResult.newBuilder();

        // 获取用户id
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        // 获取用户的移动状态
        User user = UserManager.getUserByUserId(userId);

        if (user == null) {
            return;
        }

        MoveState userMoveState = user.getMoveState();
        // 设置用户的移动状态，目的是为了在收到WhoElseIsHereCmd命令的时候，将其带回同步自己的位置
        long startTime = System.currentTimeMillis();
        userMoveState.setStartTime(startTime);
        userMoveState.setFromPosX(msg.getMoveFromPosX());
        userMoveState.setFromPosY(msg.getMoveFromPosY());
        userMoveState.setToPosX(msg.getMoveToPosX());
        userMoveState.setToPosY(msg.getMoveToPosY());

        newBuilder.setMoveUserId(userId);
        // 记录移动的起始位置
        newBuilder.setMoveFromPosX(msg.getMoveFromPosX());
        newBuilder.setMoveFromPosY(msg.getMoveFromPosY());
        newBuilder.setMoveToPosX(msg.getMoveToPosX());
        newBuilder.setMoveToPosY(msg.getMoveToPosY());
        // 记录移动的开始时间
        newBuilder.setMoveStartTime(startTime);

        // 广播消息
        BroadCaster.broadcast(newBuilder.build());
    }
}
```

最后需要修改WhoElseIsHereCmdHandler处理器，当有用户查询还有谁在游戏中的时候就添加上在游戏中的用户的移动状态。

```java
public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocolNew.WhoElseIsHereCmd> {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolNew.WhoElseIsHereCmd msg) {
        // 谁还在消息
        // 构建一个WhoElseIsHereResult消息进行返回
        GameMsgProtocolNew.WhoElseIsHereResult.Builder builder = GameMsgProtocolNew.WhoElseIsHereResult.newBuilder();
        // 在WhoElseIsHereResult消息中将所有用户字典的用户添加到UserInfo中
        Collection<User> userCollection = UserManager.getUserCollection();
        for (User user : userCollection) {

            if (user == null) {
                continue;
            }

            // 构建用户信息
            GameMsgProtocolNew.WhoElseIsHereResult.UserInfo.Builder
                    userInfoBuilder = GameMsgProtocolNew.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(user.getUserId());
            userInfoBuilder.setHeroAvatar(user.getHeroAvatar());

            MoveState userMoveState = user.getMoveState();

            // 利用本地的MoveState构建UserInfo中的MoveState消息
            GameMsgProtocolNew.WhoElseIsHereResult.UserInfo.MoveState.Builder
                    moveStateBuilder = GameMsgProtocolNew.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            moveStateBuilder.setFromPosX(userMoveState.getFromPosX());
            moveStateBuilder.setFromPosY(userMoveState.getFromPosY());
            moveStateBuilder.setToPosX(userMoveState.getToPosX());
            moveStateBuilder.setToPosY(userMoveState.getToPosY());
            moveStateBuilder.setStartTime(userMoveState.getStartTime());

            // 设置每一个用户的移动状态
            userInfoBuilder.setMoveState(moveStateBuilder.build());

            builder.addUserInfo(userInfoBuilder);
        }
        // 返回消息(无需广播)
        channelHandlerContext.writeAndFlush(builder.build());
    }
}
```

由于修改了消息协议，所以测试地址也发生了变化。

http://cdn0001.afrxvk.cn/hero_story/demo/step020/index.html?serverAddr=127.0.0.1:12345&userId=1

http://cdn0001.afrxvk.cn/hero_story/demo/step020/index.html?serverAddr=127.0.0.1:12345&userId=2

## 攻击消息处理器UserAttkCmdHandler

当角色A攻击角色B的时候，会发送UserAttkCmd消息给服务器，然后服务器需要先广播UserAttkResult消息，如果攻击者有攻击对象，那么还需要广播UserSubtractHpResult消息，从而更新被攻击者的血量，如果被攻击者的血量小于等于0了，说明该角色"
已死"，广播UserDieResult消息告诉其他客户端该角色已经死亡。

```java
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocolNew.UserAttkCmd> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    private static final Random RANDOM = new Random();

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolNew.UserAttkCmd msg) {

        if (channelHandlerContext == null || msg == null) {
            return;
        }

        // 获取攻击方id
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        // 获取攻击用户
        User user = UserManager.getUserByUserId(userId);

        if (user == null) {
            return;
        }

        // 获取攻击目标
        int targetUserId = msg.getTargetUserId();
        if (targetUserId <= 0) {
            // 没有攻击对象就广播攻击行为
            broadcastUserAttkResult(userId, 0);
        } else {
            // 每一次攻击的伤害
            int subtractHp = RANDOM.nextInt(10) + 1;
            User targetUser = UserManager.getUserByUserId(targetUserId);
            // 攻击对象还活着就掉血
            if (targetUser.getCurrentHp() > 0) {
                // 广播攻击消息UserAttkResult
                broadcastUserAttkResult(userId, targetUserId);
                // 广播掉血消息(有攻击对象才会掉血)
                broadcastUserSubtractHpResult(targetUserId, subtractHp);
            } else {
                // 攻击对象已经死去就广播攻击行为,并不再广播死亡行为
                broadcastUserAttkResult(userId, 0);
                return;
            }
            // 更新当前血量
            int currentHp = targetUser.getCurrentHp() - subtractHp;
            targetUser.setCurrentHp(currentHp);

            // 血量减小到0，广播UserDieResult消息
            if (currentHp <= 0) {
                broadcastUserDieResult(targetUserId);
            }
        }
    }

    /**
     * 广播攻击结果消息
     *
     * @param attkUserId   攻击者ID
     * @param targetUserId 被攻击者ID
     */
    private static void broadcastUserAttkResult(int attkUserId, int targetUserId) {
        // 构建UserAttkResult消息
        GameMsgProtocolNew.UserAttkResult.Builder newBuilder = GameMsgProtocolNew.UserAttkResult.newBuilder();
        newBuilder.setAttkUserId(attkUserId);
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserAttkResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

    /**
     * 广播掉血消息
     *
     * @param targetUserId 被攻击者ID
     * @param subtractHp   掉血的血量
     */
    private static void broadcastUserSubtractHpResult(int targetUserId, int subtractHp) {
        // 构建UserSubtractHpResult消息
        GameMsgProtocolNew.UserSubtractHpResult.Builder newBuilder = GameMsgProtocolNew.UserSubtractHpResult.newBuilder();
        newBuilder.setSubtractHp(subtractHp);
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserSubtractHpResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

    /**
     * 广播被攻击用户死亡消息
     * @param targetUserId 被攻击者的ID
     */
    private static void broadcastUserDieResult(int targetUserId) {
        // 构建UserDieResult消息
        GameMsgProtocolNew.UserDieResult.Builder newBuilder = GameMsgProtocolNew.UserDieResult.newBuilder();
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserDieResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

}
```

## 问题发现

我们在上述代码中添加一段输出线程名称的日志，并再次使用多名角色进行测试会发现如下现象

![image-20210204201158174](D:\herostory\src\main\resources\images\image-20210204201158174.png)

可以看到不同的角色进行攻击的时候使用的是不同的线程，这样在不同角色攻击同一个角色的时候会出现线程安全问题，两个攻击者显示的被攻击方的血量会不一致。

## 解决方案

对于线程安全的问题，第一印象就是使用加锁的方式来解决，但是加锁存在死锁的隐患，并且会让业务逻辑和控制逻辑耦合，不利于代码的简洁。这里采用将服务器的入口设置称为单线程的方式来处理，简单点说，就是让服务器只有一个线程来处理所有的事情，这样就不会出现跨线程同步的问题了。

为此，得新建MainMsgHandler类作为所有消息处理器的入口，在该类的实例设置为单例，并且内部只有一个单线程线程池，所有的消息处理交给线程池处理，这样就在入口处完成了单线程设计。

```java
package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.cmdHandler.CmdHandlerFactory;
import com.qzx.herostory.cmdHandler.ICmdHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: qzx
 * @date: 2021/2/6 - 02 - 06 - 15:30
 * @description: 所有消息处理器的入口
 * @version: 1.0
 */
public final class MainMsgHandler {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainMsgHandler.class);
    /**
     * 单例对象
     */
    private static final MainMsgHandler MSG_HANDLER = new MainMsgHandler();
    /**
     * 构建单线程线程池
     */
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (runnable) -> {
                Thread thread = new Thread(runnable);
                // 设置线程的名字
                thread.setName("MainMsgHandler");
                return thread;
            });

    /**
     * 私有化构造方法
     */
    private MainMsgHandler() {

    }

    /**
     * 获取MainMsgHandler单例对象
     *
     * @return MainMsgHandler单例
     */
    public MainMsgHandler getInstance() {
        return MSG_HANDLER;
    }

    /**
     * 处理逻辑入口
     *
     * @param channelHandlerContext channelHandlerContext
     * @param msg                   命令
     */
    public static void process(ChannelHandlerContext channelHandlerContext, Object msg) {
        // 提交任务到线程池中执行
        EXECUTOR_SERVICE.submit(() -> {
            try {
                ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.createCmdHandler(msg.getClass());

                if (null == cmdHandler) {
                    LOGGER.error(
                            "未找到相对应的命令处理器, msgClazz = {}",
                            msg.getClass().getName()
                    );
                    return;
                }
                // 解析命令
                cmdHandler.handle(channelHandlerContext, cast(msg));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 将GeneratedMessageV3类型的消息转化为其子类
     *
     * @param msg 接受到的消息
     * @param <T> GeneratedMessageV3子类
     * @return T
     */
    private static <T extends GeneratedMessageV3> T cast(Object msg) {
        if (!(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (T) msg;
    }
}

```

这样一来原先的消息处理器GameMsgHandler简化如此。

```java
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    /**
     * 用户加入逻辑
     * @param ctx ChannelHandlerContext
     * @throws Exception Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) {
            return;
        }

        // 将当前channel加入到group中进行管理
        BroadCaster.addChannel(ctx.channel());
    }

    /**
     * 用户离线逻辑
     * @param ctx ChannelHandlerContext
     * @throws Exception Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) {
            return;
        }

        // 移除用户channel
        BroadCaster.removeChannel(ctx.channel());

        // 处理用户离线逻辑
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            return;
        }

        // 将该用户从用户字典中移除
        UserManager.removeByUserId(userId);

        // 构建UserQuitResult消息
        GameMsgProtocolNew.UserQuitResult.Builder newBuilder = GameMsgProtocolNew.UserQuitResult.newBuilder();
        newBuilder.setQuitUserId(userId);

        // 群发该用户离线的消息
        BroadCaster.broadcast(newBuilder.build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        if (!(msg instanceof GeneratedMessageV3)) {
            return; // 不是protobuf类型的消息返回
        }

        //处理命令
        MainMsgHandler.process(channelHandlerContext, msg);

        LOGGER.info("收到消息：msg:{}", msg);
    }

}
```

## 用户登陆消息处理器

在此前的前端页面中都是在地址指定UserID来决定使用哪个游戏角色进行的登录，在这里结合mybatis实现用户查询数据库登陆游戏，并在用户不存在的时候完成用户注册。

首先需要新建数据库hero_story和用户表t_users

```mysql
CREATE DATABASE hero_story DEFAULT CHARACTER SET utf8;

USE hero_story;

CREATE TABLE `t_user`
(
    `user_id`     int(11) NOT NULL AUTO_INCREMENT,
    `user_name`   varchar(64) DEFAULT NULL,
    `password`    varchar(64) DEFAULT NULL,
    `hero_avatar` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`user_id`)
);
```

在pom.xml文件中添加mybatis和mysql-connector依赖，并添加扫描xml文件配置

```xml-dtd
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.6</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.22</version>
</dependency>


<resources>
    <resource>
        <directory>src/main/resources</directory>
        <includes>
        <include>**/*.properties</include>
        <include>**/*.xml</include>
        </includes>
    </resource>
    <resource>
        <directory>src/main/java</directory>
        <includes>
        <include>**/*.xml</include>
        </includes>
    </resource>
</resources>
```

由于要处理用户登陆消息，所以得更换消息协议为GameMsgProtocol_login.proto,执行完命令后得到GameMsgProtocolLogin类

```protobuf
syntax = "proto3";

package msg;
option java_package = "com.qzx.herostory.msg";

// 消息代号
enum MsgCode {
  USER_ENTRY_CMD = 0;
  USER_ENTRY_RESULT = 1;
  WHO_ELSE_IS_HERE_CMD = 2;
  WHO_ELSE_IS_HERE_RESULT = 3;
  USER_MOVE_TO_CMD = 4;
  USER_MOVE_TO_RESULT = 5;
  USER_QUIT_RESULT = 6;
  USER_STOP_CMD = 7;
  USER_STOP_RESULT = 8;
  USER_ATTK_CMD = 9;
  USER_ATTK_RESULT = 10;
  USER_SUBTRACT_HP_RESULT = 11;
  USER_DIE_RESULT = 12;
  USER_LOGIN_CMD = 13;
  USER_LOGIN_RESULT = 14;
  SELECT_HERO_CMD = 15;
  SELECT_HERO_RESULT = 16;
  GET_RANK_CMD = 17;
  GET_RANK_RESULT = 18;
};

//
// 用户入场
///////////////////////////////////////////////////////////////////////
// 指令
message UserEntryCmd {
}

// 结果
message UserEntryResult {
  // 用户 Id
  uint32 userId = 1;
  // 用户名称
  string userName = 2;
  // 英雄形象
  string heroAvatar = 3;
}

//
// 还有谁在场
///////////////////////////////////////////////////////////////////////
// 指令
message WhoElseIsHereCmd {
}

// 结果
message WhoElseIsHereResult {
  // 用户信息数组
  repeated UserInfo userInfo = 1;

  // 用户信息
  message UserInfo {
    // 用户 Id
    uint32 userId = 1;
    // 用户名称
    string userName = 2;
    // 英雄形象
    string heroAvatar = 3;
    // 移动状态
    MoveState moveState = 4;

    // 移动状态
    message MoveState {
      // 起始位置 X
      float fromPosX = 1;
      // 起始位置 Y
      float fromPosY = 2;
      // 移动到位置 X
      float toPosX = 3;
      // 移动到位置 Y
      float toPosY = 4;
      // 启程时间戳
      uint64 startTime = 5;
    }
  }
}

//
// 用户移动
///////////////////////////////////////////////////////////////////////
// 指令
message UserMoveToCmd {
  //
  // XXX 注意: 用户移动指令中没有用户 Id,
  // 这是为什么?
  //
  // 起始位置 X
  float moveFromPosX = 1;
  // 起始位置 Y
  float moveFromPosY = 2;
  // 移动到位置 X
  float moveToPosX = 3;
  // 移动到位置 Y
  float moveToPosY = 4;
}

// 结果
message UserMoveToResult {
  // 移动用户 Id
  uint32 moveUserId = 1;
  // 起始位置 X
  float moveFromPosX = 2;
  // 起始位置 Y
  float moveFromPosY = 3;
  // 移动到位置 X
  float moveToPosX = 4;
  // 移动到位置 Y
  float moveToPosY = 5;
  // 启程时间戳
  uint64 moveStartTime = 6;
}

//
// 用户退场
///////////////////////////////////////////////////////////////////////
//
// XXX 注意: 用户退场不需要指令, 因为是在断开服务器的时候执行
//
// 结果
message UserQuitResult {
  // 退出用户 Id
  uint32 quitUserId = 1;
}

//
// 用户停驻
///////////////////////////////////////////////////////////////////////
// 指令
message UserStopCmd {
}

// 结果
message UserStopResult {
  // 停驻用户 Id
  uint32 stopUserId = 1;
  // 停驻在位置 X
  float stopAtPosX = 2;
  // 停驻在位置 Y
  float stopAtPosY = 3;
}

//
// 用户攻击
///////////////////////////////////////////////////////////////////////
// 指令
message UserAttkCmd {
  // 目标用户 Id
  uint32 targetUserId = 1;
}

// 结果
message UserAttkResult {
  // 发动攻击的用户 Id
  uint32 attkUserId = 1;
  // 目标用户 Id
  uint32 targetUserId = 2;
}

// 用户减血结果
message UserSubtractHpResult {
  // 目标用户 Id
  uint32 targetUserId = 1;
  // 减血量
  uint32 subtractHp = 2;
}

// 死亡结果
message UserDieResult {
  // 目标用户 Id
  uint32 targetUserId = 1;
}

//
// 用户登录
///////////////////////////////////////////////////////////////////////
// 指令
message UserLoginCmd {
  // 用户名称
  string userName = 1;
  // 用户密码
  string password = 2;
}

// 结果
message UserLoginResult {
  // 用户 Id,
  // 如果是 -1 则说明登录失败
  uint32 userId = 1;
  // 用户名称
  string userName = 2;
  // 英雄形象
  string heroAvatar = 3;
}

//
// 选择英雄
///////////////////////////////////////////////////////////////////////
// 指令
message SelectHeroCmd {
  // 英雄形象
  string heroAvatar = 1;
}

// 结果
message SelectHeroResult {
  // 英雄形象,
  // 如果是空字符串则说明失败
  string heroAvatar = 1;
}

//
// 获取排行榜
///////////////////////////////////////////////////////////////////////
// 指令
message GetRankCmd {
}

// 结果
message GetRankResult {
  // 排名条目
  repeated RankItem rankItem = 1;

  // 用户信息
  message RankItem {
    // 排名 Id
    uint32 rankId = 1;
    // 用户 Id
    uint32 userId = 2;
    // 用户名称
    string userName = 3;
    // 英雄形象
    string heroAvatar = 4;
    // 胜利次数
    uint32 win = 5;
  }
}

```

新建表对象UserEntity，dao层接口和xml文件

```java 
package com.qzx.herostory.login.db;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:38
 * @description: 用户实体
 * @version: 1.0
 */
public class UserEntity {
    /**
     * 用户 Id
     */
    public int userId;

    /**
     * 用户名称
     */
    public String userName;

    /**
     * 密码
     */
    public String password;

    /**
     * 英雄形象
     */
    public String heroAvatar;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", heroAvatar='" + heroAvatar + '\'' +
                '}';
    }
}

```

```java
package com.qzx.herostory.login.db;

import org.apache.ibatis.annotations.Param;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:39
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public interface IUserDao {
    /**
     * 根据用户名称获取用户实体
     *
     * @param userName 用户名称
     * @return 用户实体
     */
    UserEntity getUserByName(@Param("userName") String userName);

    /**
     * 添加用户实体
     *
     * @param newUserEntity 用户实体
     */
    void insertInto(UserEntity newUserEntity);
}

```

```xml-dtd
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.qzx.herostory.login.db.IUserDao">
    <resultMap id="userEntity" type="com.qzx.herostory.login.db.UserEntity">
        <id property="userId" column="user_id"/>
        <result property="userName" column="user_name"/>
        <result property="password" column="password"/>
        <result property="heroAvatar" column="hero_avatar"/>
    </resultMap>

    <select id="getUserByName" resultMap="userEntity">
        SELECT user_id, user_name, `password`, hero_avatar
        FROM t_user
        WHERE user_name = #{userName};
    </select>

    <insert id="insertInto">
        <selectKey resultType="java.lang.Integer" order="AFTER" keyProperty="userId">
            SELECT last_insert_id() AS user_id
        </selectKey>
        INSERT INTO t_user VALUES (default, #{userName}, #{password}, #{heroAvatar} );
    </insert>
</mapper>
```

添加数据源配置文件mybatisConfig.xml

```xml-dtd
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!-- default引用environment的id,当前所使用的环境 -->
    <environments default="default">
        <!-- 声明可以使用的环境 -->
        <environment id="default">
            <!-- 使用原生JDBC事务 -->
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://127.0.0.1:3306/hero_story?serverTimezone=GMT%2B8"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper class="com.qzx.herostory.login.db.IUserDao"/>
    </mappers>
</configuration>

```

建立mysql会话工厂以便提供连接

```java
package com.qzx.herostory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 16:26
 * @description: MySql 会话工厂
 * @version: 1.0
 */
public class MySqlSessionFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlSessionFactory.class);

    private MySqlSessionFactory() {

    }

    private static SqlSessionFactory sqlSessionFactory;

    public static void init() {
        LOGGER.info("开始连接数据库");

        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder()
                    .build(Resources.getResourceAsStream("mybatisConfig.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("连接数据库成功");
    }

    public static SqlSession getConnection() {

        if (sqlSessionFactory == null) {
            throw new RuntimeException("sqlSessionFactory 尚未初始化");
        }

        return sqlSessionFactory.openSession();
    }
}

```

添加登录核心逻辑LoginService

```java
package com.qzx.herostory.login;

import com.qzx.herostory.MySqlSessionFactory;
import com.qzx.herostory.login.db.IUserDao;
import com.qzx.herostory.login.db.UserEntity;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:42
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public class LoginService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService LOGIN_SERVICE = new LoginService();

    /**
     * 私有化构造方法
     */
    private LoginService() {

    }

    /**
     * 获取LoginService对象
     *
     * @return LoginService对象
     */
    public static LoginService getInstance() {
        return LOGIN_SERVICE;
    }

    /**
     * 根据用户名获取用户对象
     *
     * @param userName 用户名
     * @param password 登陆密码
     * @return 用户对象
     */
    public UserEntity login(String userName, String password) {
        if (userName == null) {
            LOGGER.error("userName为空");
            return null;
        }
        UserEntity userEntity = null;
        try (SqlSession session = MySqlSessionFactory.getConnection()) {
            if (session == null) {
                LOGGER.error("获取连接失败");
                return null;
            }

            IUserDao iUserDao = session.getMapper(IUserDao.class);

            if (iUserDao == null) {
                LOGGER.error("iUserDao为空");
                return null;
            }
            userEntity = iUserDao.getUserByName(userName);

            if (userEntity == null) {
                LOGGER.info("用户不存在，开始创建用户");

                userEntity = new UserEntity();
                userEntity.setUserName(userName);
                userEntity.setPassword(password);
                userEntity.setHeroAvatar("Hero_Shaman");
                iUserDao.insertInto(userEntity);
                session.commit();

                LOGGER.info("创建成功，并开始登陆");
            } else {
                if (!userEntity.getPassword().equals(password)) {
                    LOGGER.error("密码不正确");
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return userEntity;
    }


}

```

构建用户登录消息处理器UserLoginCmdHandler，在登录成功后就该用户存储到本地，然后将当前userId存储在channel中，最后构建UserLoginResult结果对象并返回给客户端。

```java
package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.login.LoginService;
import com.qzx.herostory.login.db.UserEntity;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 16:05
 * @description: 用户登陆消息处理器
 * @version: 1.0
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserLoginCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserLoginCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        // 登录
        String userName = msg.getUserName();
        String password = msg.getPassword();
        UserEntity userEntity = LoginService.getInstance().login(userName, password);

        if (userEntity == null) {
            LOGGER.info("登陆失败");
            return;
        }
        // 存放该用户到channel中
        LOGGER.info("登录成功");

        // 创建本地User对象
        User user = new User();
        user.setUserId(userEntity.getUserId());
        user.setCurrentHp(100);
        user.setHeroAvatar(userEntity.getHeroAvatar());
        user.setUserName(userName);

        // 保存当前user对象到本地
        UserManager.save(user);

        // 将当前userId存储在channel中
        channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(user.getUserId());

        // 构建UserLoginResult结果对象
        GameMsgProtocolLogin.UserLoginResult.Builder newBuilder = GameMsgProtocolLogin.UserLoginResult.newBuilder();
        newBuilder.setUserId(user.getUserId());
        newBuilder.setUserName(user.getUserName());
        newBuilder.setHeroAvatar(user.getHeroAvatar());

        // 发送消息给客户端
        channelHandlerContext.writeAndFlush(newBuilder.build());
    }
}

```

由于添加了登录消息，在登录成功后会将当前登录用户存储在本地，所以入场消息就无需携带用户信息，直接根据channel取出userId，然后获取用户即可，并且由于消息中添加了UserName字段，所以在User对象中也得添加UserName字段并在登录成功后进行设置

```java
public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserEntryCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserEntryCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        // 获取userId
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            LOGGER.error("userId为空");
            return;
        }
        // 获取当前登录对象
        User user = UserManager.getUserByUserId(userId);
        if (user == null) {
            LOGGER.error("user为空");
            return;
        }

        // 构建一个UserEntryResult对象
        GameMsgProtocolLogin.UserEntryResult.Builder builder = GameMsgProtocolLogin.UserEntryResult.newBuilder();
        builder.setUserId(userId);
        builder.setHeroAvatar(user.getHeroAvatar());
        builder.setUserName(user.getUserName());
        GameMsgProtocolLogin.UserEntryResult userEntryResult = builder.build();

        // 将该消息进行广播
        BroadCaster.broadcast(userEntryResult);
    }
}
```

同样的在WhoElseIsHereCmd消息处理器中也得添加用户的UserName字段。

```java
userInfoBuilder.setUserName(user.getUserName());
```

## 问题发现（IO问题，已解决，见第五天）

在运行并且登录成功后系统会打印如下日志

![image-20210208150528624](D:\herostory\src\main\resources\images\image-20210208150528624.png)

也就是说当前的处理登录的IO线程和业务处理的线程是同一个线程，如果查询数据库阻塞这个系统也会阻塞。

# 第五天

## 解决方案（第四天遗留）

在昨天的代码中发现IO线程和业务逻辑处理线程是同一个线程，这就好像一个餐厅的服务员在接受到客户的点菜请求后就去后台炒菜去了，这样就无法获得其他客户的点菜请求，在这里我们分离出IO线程来解决这个问题。

首先我们提供一个异步处理器（在这里专门处理IO逻辑）AsyncOperationProcessor，它为外部提供一个process方法，只要调用process并传递所需要执行的任务，该方法就会把任务提交到另外一个线程池中执行（目前是单线程）。

```java
package com.qzx.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: qzx
 * @date: 2021/2/8 - 02 - 08 - 15:08
 * @description: 异步处理器
 * @version: 1.0
 */
public class AsyncOperationProcessor {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor ASYNC_OPERATION_PROCESSOR = new AsyncOperationProcessor();

    /**
     * 构建单线程线程池
     */
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (runnable) -> {
                Thread thread = new Thread(runnable);
                // 设置线程的名字
                thread.setName("AsyncOperationProcessor");
                return thread;
            });

    /**
     * 私有化构造方法
     */
    private AsyncOperationProcessor() {

    }

    /**
     * 获取AsyncOperationProcessor单例对象
     * @return AsyncOperationProcessor单例对象
     */
    public static AsyncOperationProcessor getInstance() {
        return ASYNC_OPERATION_PROCESSOR;
    }

    /**
     * 执行任务
     * @param r 任务
     */
    public void process(Runnable r) {
        EXECUTOR_SERVICE.submit(r);
    }

}

```

我们之前的处理流程是在UserLoginCmdHandler收到登陆消息，然后调用了LoginService提供的login方法来进行登录，我们现在需要将login方法更改为异步处理，具体做法就是将login的执行逻辑封装为一个task并调用AsyncOperationProcessor的process方法，将task传递其中即可。

但是同时我们需要考虑另外一个问题，就是既然登录逻辑在另外一个线程中执行，那么如何获取登录成功后返回来的UserEntity对象并返回给UserLoginCmdHandler呢？

这里采用回调函数的方法，我们修改login方法的返回值为void并且添加一个回调函数callback的方法，在登录成功后就将UserEntity对象返回给调用login方法的线程，也就是这里的UserLoginCmdHandler，所以在UserLoginCmdHandler中调用login方法的过程也得发生变化，将获得用户对象后的所有处理逻辑封装为回调函数传递给login中，也就是将UserLoginCmdHandler处理逻辑也放在另外一个线程里面进行处理。

```java
package com.qzx.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: qzx
 * @date: 2021/2/8 - 02 - 08 - 15:08
 * @description: 异步处理器
 * @version: 1.0
 */
public class AsyncOperationProcessor {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor ASYNC_OPERATION_PROCESSOR = new AsyncOperationProcessor();

    /**
     * 构建单线程线程池
     */
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (runnable) -> {
                Thread thread = new Thread(runnable);
                // 设置线程的名字
                thread.setName("AsyncOperationProcessor");
                return thread;
            });

    /**
     * 私有化构造方法
     */
    private AsyncOperationProcessor() {

    }

    /**
     * 获取AsyncOperationProcessor单例对象
     * @return AsyncOperationProcessor单例对象
     */
    public static AsyncOperationProcessor getInstance() {
        return ASYNC_OPERATION_PROCESSOR;
    }

    /**
     * 执行任务
     * @param r 任务
     */
    public void process(Runnable r) {
        EXECUTOR_SERVICE.submit(r);
    }

}

```

```java
/**
 * 根据用户名获取用户对象
 *
 * @param userName 用户名
 * @param password 登陆密码
 * @return
 */
public void login(String userName,String password,Function<UserEntity, Void> callback){
        if(userName==null){
        LOGGER.error("userName为空");
        return;
        }

        AsyncOperationProcessor.getInstance().process(()->{
        try(SqlSession session=MySqlSessionFactory.getConnection()){
        if(session==null){
        LOGGER.error("获取连接失败");
        return;
        }

        IUserDao iUserDao=session.getMapper(IUserDao.class);
        LOGGER.info("当前线程为："+Thread.currentThread().getName());
        if(iUserDao==null){
        LOGGER.error("iUserDao为空");
        return;
        }
        UserEntity userEntity=iUserDao.getUserByName(userName);

        if(userEntity==null){
        LOGGER.info("用户不存在，开始创建用户");

        userEntity=new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);
        userEntity.setHeroAvatar("Hero_Shaman");
        iUserDao.insertInto(userEntity);
        session.commit();

        LOGGER.info("创建成功，并开始登陆");
        }else{
        if(!userEntity.getPassword().equals(password)){
        LOGGER.error("密码不正确");
        return;
        }
        }

        if(callback!=null){
        callback.apply(userEntity);
        }
        }catch(Exception e){
        LOGGER.error(e.getMessage(),e);
        }
        });
        }
```

```java
/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 16:05
 * @description: 用户登录消息处理器
 * @version: 1.0
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserLoginCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserLoginCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        // 登录
        String userName = msg.getUserName();
        String password = msg.getPassword();

        LOGGER.info("开始登录");
        LOGGER.info("当前线程为：" + Thread.currentThread().getName());
        LoginService.getInstance().login(userName, password, (userEntity) -> {
            if (userEntity == null) {
                LOGGER.info("登录失败");
                return null;
            }
            // 存放该用户到channel中
            LOGGER.info("登录成功");

            // 创建本地User对象
            User user = new User();
            user.setUserId(userEntity.getUserId());
            user.setCurrentHp(100);
            user.setHeroAvatar(userEntity.getHeroAvatar());
            user.setUserName(userName);

            // 保存当前user对象到本地
            UserManager.save(user);

            // 将当前userId存储在channel中
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(user.getUserId());

            // 构建UserLoginResult结果对象
            GameMsgProtocolLogin.UserLoginResult.Builder newBuilder = GameMsgProtocolLogin.UserLoginResult.newBuilder();
            newBuilder.setUserId(user.getUserId());
            newBuilder.setUserName(user.getUserName());
            newBuilder.setHeroAvatar(user.getHeroAvatar());

            // 发送消息给客户端
            channelHandlerContext.writeAndFlush(newBuilder.build());
            return null;
        });
    }
}
```

我们测试登录可以看到登录逻辑和业务处理逻辑在两个线程进行。

![image-20210208154548485](D:\herostory\src\main\resources\images\image-20210208154548485.png)

## 问题发现

虽然我们现在分离了IO线程和业务处理线程，但是对于只有一个线程的IO处理依然会出现阻塞的情况，那么登录逻辑阻塞的话也会影响用户入场，从而无法进行业务处理。

同时，由于我们将登录成功后的业务处理逻辑封装为了一个回调函数传递给异步线程，在登录成功后，后序的登录成功业务逻辑直接在异步线程完成了。这就好像一个厨师炒完菜后直接从后厨端菜给餐厅的客人，这样在逻辑上属于跨线程了，存在着数据的脏读写问题。

## 解决方案

### 分离IO和业务处理逻辑

我们先解决如何将完全分离IO逻辑和业务处理逻辑的问题，因为登录的过程在异步线程中处理，而**登录完成后**
的业务逻辑得在主线程中处理，所以我们可以将登录逻辑进行再一次封装，这样在执行异步逻辑的时候使用异步线程，在执行完毕后就将回调方法提交到主线程中执行就好，这里说的比较抽象，看下面代码即可，主要思路就是将登陆和业务执行封装为doAsync和doFinish方法，doAsync方法在异步线程中执行，doFinish方法在主线程中执行。

首先抽取出一个异步执行接口IAsyncOperation，包含了异步执行逻辑和执行完毕后的回调逻辑(不一定有)。

```java
package com.qzx.herostory.async;

/**
 * @author: qzx
 * @date: 2021/2/8 - 02 - 08 - 16:24
 * @description: 异步操作接口
 * @version: 1.0
 */
public interface IAsyncOperation {
    /**
     * 异步执行逻辑
     */
    void doAsync();

    /**
     * 异步执行结束后的逻辑处理
     */
    default void doFinish() {

    }
}

```

修改AsyncOperationProcessor的process方法，实现执行IO处理和业务逻辑的完全分离

```java
/**
 * 执行任务
 * @param r 任务
 */
public void process(IAsyncOperation r){
        EXECUTOR_SERVICE.submit(()->{
        // 执行异步逻辑
        r.doAsync();
        // 在主线程中执行回调逻辑(业务逻辑)
        MainMsgHandler.process(r::doFinish);
        });
        }
```

由于分离了IO，所以需要在LoginService中抽取登陆逻辑在AsyncLogin内部类中，它实现了IAsyncOperation接口，在其doAsync方法中完成登录逻辑。

同时在构造异步操作的时候，重新doFinish方法，将回调函数执行逻辑添加在其中。

```java
/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:42
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public class LoginService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService LOGIN_SERVICE = new LoginService();

    /**
     * 私有化构造方法
     */
    private LoginService() {

    }

    /**
     * 获取LoginService对象
     *
     * @return LoginService对象
     */
    public static LoginService getInstance() {
        return LOGIN_SERVICE;
    }

    /**
     * 根据用户名获取用户对象
     *
     * @param userName 用户名
     * @param password 登陆密码
     * @return
     */
    public void login(String userName, String password, Function<UserEntity, Void> callback) {
        if (userName == null) {
            LOGGER.error("userName为空");
            return;
        }

        IAsyncOperation asyncOperation = new AsyncLogin(userName, password) {
            @Override
            public void doFinish() {
                if (callback != null) {
                    LOGGER.info("执行登录成功后回调函数，当前线程为：" + Thread.currentThread().getName());
                    callback.apply(this.getUserEntity());
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOperation);
    }

    private static class AsyncLogin implements IAsyncOperation {
        /**
         * 日志对象
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogin.class);
        /**
         * 用户名
         */
        String username;

        /**
         * 密码
         */
        String password;

        /**
         * 结果对象
         */
        UserEntity userEntity;

        AsyncLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public UserEntity getUserEntity() {
            return userEntity;
        }

        /**
         * 执行异步登录操作
         */
        @Override
        public void doAsync() {
            try (SqlSession session = MySqlSessionFactory.getConnection()) {
                if (session == null) {
                    LOGGER.error("获取连接失败");
                    return;
                }

                IUserDao iUserDao = session.getMapper(IUserDao.class);

                LOGGER.info("开始执行登录IO操作，当前线程为：" + Thread.currentThread().getName());

                if (iUserDao == null) {
                    LOGGER.error("iUserDao为空");
                    return;
                }

                UserEntity userEntity = iUserDao.getUserByName(this.username);

                if (userEntity == null) {
                    LOGGER.info("用户不存在，开始创建用户");

                    userEntity = new UserEntity();
                    userEntity.setUserName(this.username);
                    userEntity.setPassword(this.password);
                    userEntity.setHeroAvatar("Hero_Shaman");
                    iUserDao.insertInto(userEntity);
                    session.commit();

                    LOGGER.info("创建成功，并开始登陆");
                } else {
                    if (!userEntity.getPassword().equals(this.password)) {
                        LOGGER.error("密码不正确");
                        return;
                    }
                }
                this.userEntity = userEntity;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
```

运行项目测试登录可以看到，最开始的UserLoginCmdHandler由主线程MainMsgHandler处理，然后轮到AsyncOperationProcessor执行登录逻辑，登录成功后又回到MainMsgHandler完成最后的业务逻辑处理部分。

![image-20210208180816091](D:\herostory\src\main\resources\images\image-20210208180816091.png)

### 实现IO多线程设计

一个IO线程也有可能会阻塞，虽然不会阻塞主线程，但是会影响到主线程的业务处理，解决的方法就是使用多线程池实现IO处理。同时，如果在多线程下一个用户点击多次登录请求，就有可能会导致一个用户有两个userId(初始没有该用户)
，为了避免该现象，我们得绑定当前用户的IO操作和其userId，也就是一个用户的IO操作都会在一个线程上执行，在当前代码中体现就是在IAsyncOperation线程中绑定userId了。

首先在IAsyncOperation接口中添加获取bindId的方法

```java 
/**
     * 获取当前线程绑定的id
     * @return id
     */
    default int getBindId(){
        return 0;
    }
```

然后在LoginService中的AsyncLogin构造过程实现该方法

```java
IAsyncOperation asyncOperation=new AsyncLogin(userName,password){
@Override
public int getBindId(){
        // 取最后一个字符作为选择的线程id
        int bindId=userName.charAt(userName.length()-1);
        LOGGER.info("获取bindId: "+bindId);
        return bindId;
        }

@Override
public void doFinish(){
        if(callback!=null){
        LOGGER.info("执行登录成功后回调函数，当前线程为："+Thread.currentThread().getName());
        callback.apply(this.getUserEntity());
        }
        }
        };
```

最后在异步处理器AsyncOperationProcessor中修改单线程池为单线程数组，在构造方法中进行实例化，并通过bindId获取对应的线程执行任务。

```java
public class AsyncOperationProcessor {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);

    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor ASYNC_OPERATION_PROCESSOR = new AsyncOperationProcessor();

    /**
     * 构建单线程线程数组
     */
    private final ExecutorService[] esArray = new ExecutorService[8];

    /**
     * 私有化构造方法，实例化单线程数组
     */
    private AsyncOperationProcessor() {
        for (int i = 0; i < esArray.length; ++i) {
            String threadName = "AsyncOperationProcessor [ " + i + " ]";
            esArray[i] = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    (runnable) -> {
                        Thread thread = new Thread(runnable);
                        // 设置线程的名字
                        thread.setName(threadName);
                        return thread;
                    });
        }
    }

    /**
     * 获取AsyncOperationProcessor单例对象
     *
     * @return AsyncOperationProcessor单例对象
     */
    public static AsyncOperationProcessor getInstance() {
        return ASYNC_OPERATION_PROCESSOR;
    }

    /**
     * 执行任务
     *
     * @param r 任务
     */
    public void process(IAsyncOperation r) {
        if (r == null) {
            return;
        }

        // 获取绑定的id
        int bindId = r.getBindId();
        // 使用绑定的线程执行
        int index = bindId % esArray.length;
        esArray[index].submit(() -> {
            // 执行异步逻辑
            r.doAsync();
            // 执行回调逻辑(业务逻辑)
            MainMsgHandler.process(r::doFinish);
        });
    }

}

```

启动项目测试登录，可以看到实现了多线程进行处理IO逻辑

![image-20210208190712306](D:\herostory\src\main\resources\images\image-20210208190712306.png)

# 第六天

## 排行榜

现在我们需要实现一个排行榜功能需求，通过统计一个用户的击杀敌人的次数来获取相应的排名，不过这里不打算将排行榜逻辑添加到游戏服务器中，而是单独开辟一个进程来实现排行榜，之所以这么做的原因是因为排行榜的需求有可能会变化，比如增加胜率排名、等级排名等，并且如果排行榜功能出现bug希望不会影响到游戏服务器的正常运行，从而实现线上修改bug，也就是说，我们希望在不影响主要业务功能的前提下，完成功能和扩展新功能。其解决方案如下：

![image-20210214101847547](D:\herostory\src\main\resources\images\image-20210214101847547.png)

当一个用户A击杀用户B的时候就将该消息写入rocketmq中，然后由排行榜进程读取消息并计算排名，并将最新的排名写入redis中，这样在redis中的数据发生变化的时候游戏服务器就会读取最新的排行榜数据从而获取最新的排名。

由于我们在消息中需要传递排名的信息，所以需要修改消息协议。

```protobuf
syntax = "proto3";

package msg;
option java_package = "com.qzx.herostory.msg";

// 消息代号
enum MsgCode {
  USER_ENTRY_CMD = 0;
  USER_ENTRY_RESULT = 1;
  WHO_ELSE_IS_HERE_CMD = 2;
  WHO_ELSE_IS_HERE_RESULT = 3;
  USER_MOVE_TO_CMD = 4;
  USER_MOVE_TO_RESULT = 5;
  USER_QUIT_RESULT = 6;
  USER_STOP_CMD = 7;
  USER_STOP_RESULT = 8;
  USER_ATTK_CMD = 9;
  USER_ATTK_RESULT = 10;
  USER_SUBTRACT_HP_RESULT = 11;
  USER_DIE_RESULT = 12;
  USER_LOGIN_CMD = 13;
  USER_LOGIN_RESULT = 14;
  SELECT_HERO_CMD = 15;
  SELECT_HERO_RESULT = 16;
  GET_RANK_CMD = 17;
  GET_RANK_RESULT = 18;
};

//
// 用户入场
///////////////////////////////////////////////////////////////////////
// 指令
message UserEntryCmd {
}

// 结果
message UserEntryResult {
  // 用户 Id
  uint32 userId = 1;
  // 用户名称
  string userName = 2;
  // 英雄形象
  string heroAvatar = 3;
}

//
// 还有谁在场
///////////////////////////////////////////////////////////////////////
// 指令
message WhoElseIsHereCmd {
}

// 结果
message WhoElseIsHereResult {
  // 用户信息数组
  repeated UserInfo userInfo = 1;

  // 用户信息
  message UserInfo {
    // 用户 Id
    uint32 userId = 1;
    // 用户名称
    string userName = 2;
    // 英雄形象
    string heroAvatar = 3;
    // 移动状态
    MoveState moveState = 4;

    // 移动状态
    message MoveState {
      // 起始位置 X
      float fromPosX = 1;
      // 起始位置 Y
      float fromPosY = 2;
      // 移动到位置 X
      float toPosX = 3;
      // 移动到位置 Y
      float toPosY = 4;
      // 启程时间戳
      uint64 startTime = 5;
    }
  }
}

//
// 用户移动
///////////////////////////////////////////////////////////////////////
// 指令
message UserMoveToCmd {
  //
  // XXX 注意: 用户移动指令中没有用户 Id,
  // 这是为什么?
  //
  // 起始位置 X
  float moveFromPosX = 1;
  // 起始位置 Y
  float moveFromPosY = 2;
  // 移动到位置 X
  float moveToPosX = 3;
  // 移动到位置 Y
  float moveToPosY = 4;
}

// 结果
message UserMoveToResult {
  // 移动用户 Id
  uint32 moveUserId = 1;
  // 起始位置 X
  float moveFromPosX = 2;
  // 起始位置 Y
  float moveFromPosY = 3;
  // 移动到位置 X
  float moveToPosX = 4;
  // 移动到位置 Y
  float moveToPosY = 5;
  // 启程时间戳
  uint64 moveStartTime = 6;
}

//
// 用户退场
///////////////////////////////////////////////////////////////////////
//
// XXX 注意: 用户退场不需要指令, 因为是在断开服务器的时候执行
//
// 结果
message UserQuitResult {
  // 退出用户 Id
  uint32 quitUserId = 1;
}

//
// 用户停驻
///////////////////////////////////////////////////////////////////////
// 指令
message UserStopCmd {
}

// 结果
message UserStopResult {
  // 停驻用户 Id
  uint32 stopUserId = 1;
  // 停驻在位置 X
  float stopAtPosX = 2;
  // 停驻在位置 Y
  float stopAtPosY = 3;
}

//
// 用户攻击
///////////////////////////////////////////////////////////////////////
// 指令
message UserAttkCmd {
  // 目标用户 Id
  uint32 targetUserId = 1;
}

// 结果
message UserAttkResult {
  // 发动攻击的用户 Id
  uint32 attkUserId = 1;
  // 目标用户 Id
  uint32 targetUserId = 2;
}

// 用户减血结果
message UserSubtractHpResult {
  // 目标用户 Id
  uint32 targetUserId = 1;
  // 减血量
  uint32 subtractHp = 2;
}

// 死亡结果
message UserDieResult {
  // 目标用户 Id
  uint32 targetUserId = 1;
}

//
// 用户登录
///////////////////////////////////////////////////////////////////////
// 指令
message UserLoginCmd {
  // 用户名称
  string userName = 1;
  // 用户密码
  string password = 2;
}

// 结果
message UserLoginResult {
  // 用户 Id,
  // 如果是 -1 则说明登录失败
  uint32 userId = 1;
  // 用户名称
  string userName = 2;
  // 英雄形象
  string heroAvatar = 3;
}

//
// 选择英雄
///////////////////////////////////////////////////////////////////////
// 指令
message SelectHeroCmd {
  // 英雄形象
  string heroAvatar = 1;
}

// 结果
message SelectHeroResult {
  // 英雄形象,
  // 如果是空字符串则说明失败
  string heroAvatar = 1;
}

//
// 获取排行榜
///////////////////////////////////////////////////////////////////////
// 指令
message GetRankCmd {
}

// 结果
message GetRankResult {
  // 排名条目
  repeated RankItem rankItem = 1;

  // 用户信息
  message RankItem {
    // 排名 Id
    uint32 rankId = 1;
    // 用户 Id
    uint32 userId = 2;
    // 用户名称
    string userName = 3;
    // 英雄形象
    string heroAvatar = 4;
    // 胜利次数
    uint32 win = 5;
  }
}

```

添加redis客户端和rocketmq的依赖

```xml-dtd
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>3.3.0</version>
</dependency>

<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-client</artifactId>
    <version>4.6.1</version>
</dependency>
```

### RankItem(排名信息条目)

由于服务端需要发送GetRankResult消息给客户端返回排名结果，而该消息中又包含了一个RankItem的消息，所以需要在本地暂存RankItem对象，方便封装使用。

```java 
package com.qzx.herostory.rank;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 10:36
 * @description: 排名条目
 * @version: 1.0
 */
public class RankItem {
    /**
     * 排名Id
     */
    private Integer rankId;

    /**
     * 用户Id
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户形象
     */
    private String heroAvatar;

    /**
     * 胜利次数
     */
    private Integer win;

    public Integer getRankId() {
        return rankId;
    }

    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    public Integer getWin() {
        return win;
    }

    public void setWin(Integer win) {
        this.win = win;
    }
}

```

### RedisUtil(redis工具类)

为了方便我们对redis进行操作，我们封装一个redis工具类来获取redis连接。

```java 
package com.qzx.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 11:02
 * @description: redis工具类
 * @version: 1.0
 */
public final class RedisUtil {
    /**
     * 单例对象
     */
    private static final RedisUtil REDIS_UTIL = new RedisUtil();

    /**
     * jedis连接池
     */
    private static JedisPool JEDIS_POOL;

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * 私有化构造方法
     */
    private RedisUtil() {

    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static RedisUtil getInstance() {
        return REDIS_UTIL;
    }

    /**
     * 初始化jedis连接池
     */
    public static void init() {
        try {
            JEDIS_POOL = new JedisPool("192.168.221.139", 6379);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取jedis客户端
     * @return Jedis
     */
    public static Jedis getJedis() {
        if (JEDIS_POOL == null) {
            LOGGER.error("Jedis连接池未初始化");
            return null;
        }
        return JEDIS_POOL.getResource();
    }
}

```

### RankService(获取排名服务)

接下来，我们需要从redis中读取排名信息，首先，redis中存储的排名是一个List，其名称叫做Rank，每一个对象是由userId(用户Id)和win(当前玩家胜利次数)
所组成，我们需要获取该排名信息并且按照win的大小递减排序，从而获取rankId。其次，操作redis实际上也是一个异步操作，需要在异步线程中执行，这里可以使用AsyncOperationProcessor来执行异步操作，具体的获取排名的逻辑在doAsync方法中实现，返回的结果在doFinish中通过回调函数传递给主线程。

redis中不仅仅是存储了排名，也存储了用户的信息，采用的是一个map的数据结构，每一个key是"User_"
+userId，其中的BasicInfo保存了用户的所有基本信息，其格式是json字符串，该基本信息是在用户登录的时候写入redis的。

```java
package com.qzx.herostory.rank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qzx.herostory.async.AsyncOperationProcessor;
import com.qzx.herostory.async.IAsyncOperation;
import com.qzx.herostory.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 10:39
 * @description: 获取排名服务
 * @version: 1.0
 */
public class RankService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RankService.class);

    /**
     * 单例对象
     */
    private static final RankService RANK_SERVICE = new RankService();

    /**
     * 私有化构造方法
     */
    private RankService() {

    }

    /**
     * 获取单例对象
     *
     * @return RankService
     */
    public static RankService getInstance() {
        return RANK_SERVICE;
    }

    /**
     * 获取排名列表
     *
     * @param callback 回调函数
     */
    public void getRankList(Function<List<RankItem>, Void> callback) {
        // 异步执行从redis中 获取排名
        AsyncOperationProcessor.getInstance().process(new AsyncRankOperation() {
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        });
    }

    private static class AsyncRankOperation implements IAsyncOperation {
        /**
         * 获取排名结果集
         */
        private List<RankItem> rankItemList;

        /**
         * 返回排名列表
         *
         * @return 排名列表
         */
        public List<RankItem> getRankItemList() {
            return rankItemList;
        }

        @Override
        public void doAsync() {
            // 获取jedis对象
            try (Jedis jedis = RedisUtil.getJedis()) {

                if (jedis == null) {
                    return;
                }

                rankItemList = new LinkedList<>();

                // 当前用户排名
                int rank = 0;
                // 排名结果前10集合
                Set<Tuple> tuples = jedis.zrevrangeWithScores("Rank", 0, 9);
                for (Tuple tuple : tuples) {
                    // 获取用户Id
                    int userId = Integer.parseInt(tuple.getElement());
                    // 获取胜利次数
                    int win = (int) tuple.getScore();
                    // 获取用户信息
                    String userInfo = jedis.hget("User_" + userId, "BasicInfo");

                    if (userInfo == null) {
                        continue;
                    }

                    // 构建RankItem
                    JSONObject jsonObject = JSON.parseObject(userInfo);
                    String userName = jsonObject.getString("userName");
                    String heroAvatar = jsonObject.getString("heroAvatar");
                    RankItem rankItem = new RankItem();
                    rankItem.setRankId(++rank);
                    rankItem.setUserId(userId);
                    rankItem.setWin(win);
                    rankItem.setUserName(userName);
                    rankItem.setHeroAvatar(heroAvatar);

                    // 将rankItem添加到rankItemList中
                    rankItemList.add(rankItem);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}

```

同步用户基本信息到redis中，在LoginService中的updateUserInfoToRedis完成该逻辑，在异步执行登录成功后根据返回的UserEntity写入redis。

```java
package com.qzx.herostory.login;

import com.alibaba.fastjson.JSONObject;
import com.qzx.herostory.MySqlSessionFactory;
import com.qzx.herostory.async.AsyncOperationProcessor;
import com.qzx.herostory.async.IAsyncOperation;
import com.qzx.herostory.login.db.IUserDao;
import com.qzx.herostory.login.db.UserEntity;
import com.qzx.herostory.util.RedisUtil;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:42
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public class LoginService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService LOGIN_SERVICE = new LoginService();

    /**
     * 私有化构造方法
     */
    private LoginService() {

    }

    /**
     * 获取LoginService对象
     *
     * @return LoginService对象
     */
    public static LoginService getInstance() {
        return LOGIN_SERVICE;
    }

    /**
     * 根据用户名获取用户对象
     *
     * @param userName 用户名
     * @param password 登陆密码
     */
    public void login(String userName, String password, Function<UserEntity, Void> callback) {
        if (userName == null) {
            LOGGER.error("userName为空");
            return;
        }

        IAsyncOperation asyncOperation = new AsyncLogin(userName, password) {
            @Override
            public int getBindId() {
                // 取最后一个字符作为选择的线程id
                int bindId = userName.charAt(userName.length() - 1);
                LOGGER.info("获取bindId: " + bindId);
                return bindId;
            }

            @Override
            public void doFinish() {
                if (callback != null) {
                    LOGGER.info("执行登录成功后回调函数，当前线程为：" + Thread.currentThread().getName());
                    callback.apply(this.getUserEntity());
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOperation);
    }

    private static class AsyncLogin implements IAsyncOperation {
        /**
         * 日志对象
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogin.class);
        /**
         * 用户名
         */
        String username;

        /**
         * 密码
         */
        String password;

        /**
         * 结果对象
         */
        UserEntity userEntity;

        AsyncLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public UserEntity getUserEntity() {
            return userEntity;
        }

        /**
         * 执行异步登录操作
         */
        @Override
        public void doAsync() {
            try (SqlSession session = MySqlSessionFactory.getConnection()) {
                if (session == null) {
                    LOGGER.error("获取连接失败");
                    return;
                }

                IUserDao iUserDao = session.getMapper(IUserDao.class);

                LOGGER.info("开始执行登录IO操作，当前线程为：" + Thread.currentThread().getName());

                if (iUserDao == null) {
                    LOGGER.error("iUserDao为空");
                    return;
                }

                UserEntity userEntity = iUserDao.getUserByName(this.username);

                if (userEntity == null) {
                    LOGGER.info("用户不存在，开始创建用户");

                    userEntity = new UserEntity();
                    userEntity.setUserName(this.username);
                    userEntity.setPassword(this.password);
                    userEntity.setHeroAvatar("Hero_Shaman");
                    iUserDao.insertInto(userEntity);
                    session.commit();

                    LOGGER.info("创建成功，并开始登陆");
                } else {
                    if (!userEntity.getPassword().equals(this.password)) {
                        LOGGER.error("密码不正确");
                        return;
                    }
                }
                this.userEntity = userEntity;
                // 更新redis中用户的基本信息
                LoginService.getInstance().updateUserInfoToRedis(userEntity);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void updateUserInfoToRedis(UserEntity userEntity) {
        if (userEntity == null) {
            return;
        }

        try (Jedis jedis = RedisUtil.getJedis()) {
            if (jedis == null) {
                return;
            }

            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", userEntity.getUserName());
            jsonObject.put("heroAvatar", userEntity.getHeroAvatar());

            // 更新当前用户数据到redis
            jedis.hset("User_" + userEntity.getUserId(), "BasicInfo", jsonObject.toJSONString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

```

### 获取排名消息处理器GetRankCmdHandler

我们现在获取了排名列表，现在需要根据该列表封装为GetRankResult发送给客户端。

```java
package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.msg.GameMsgProtocolLogin;
import com.qzx.herostory.msg.GameMsgProtocolRank;
import com.qzx.herostory.rank.RankItem;
import com.qzx.herostory.rank.RankService;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;

/**
 * @author: qzx
 * @date: 2021/2/14 - 02 - 14 - 16:21
 * @description: 获取排名消息处理器
 * @version: 1.0
 */
public class GetRankCmdHandler implements ICmdHandler<GameMsgProtocolRank.GetRankCmd> {
    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolRank.GetRankCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }

        // 获取排行榜数据
        RankService.getInstance().getRankList(rankItemList -> {
            if (rankItemList == null) {
                rankItemList = Collections.emptyList();
            }

            GameMsgProtocolLogin.GetRankResult.Builder builder = GameMsgProtocolLogin.GetRankResult.newBuilder();

            for (RankItem rankItem : rankItemList) {
                if (rankItem == null) {
                    continue;
                }

                GameMsgProtocolLogin.GetRankResult.RankItem.Builder rankItemBuilder =
                        GameMsgProtocolLogin.GetRankResult.RankItem.newBuilder();
                rankItemBuilder.setRankId(rankItem.getRankId());
                rankItemBuilder.setHeroAvatar(rankItem.getHeroAvatar());
                rankItemBuilder.setUserName(rankItem.getUserName());
                rankItemBuilder.setWin(rankItem.getWin());
                rankItemBuilder.setUserId(rankItem.getUserId());

                builder.addRankItem(rankItemBuilder.build());
            }

            channelHandlerContext.writeAndFlush(builder.build());

            return null;
        });
    }
}


```

### 消息队列生产者

在角色A击败角色B的时候，会向消息队列中发送一条{winnerId=AId,loseId=BId}的消息(自定义VistorMsg消息)，以供排行榜进程消费该消息计算新的排名。

```java
package com.qzx.herostory.mq;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 18:05
 * @description: 击杀成功消息
 * @version: 1.0
 */
public class VictorMsg {
    /**
     * 胜利者Id
     */
    private Integer winnerId;

    /**
     * 失败者Id
     */
    private Integer loseId;

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getLoseId() {
        return loseId;
    }

    public void setLoseId(Integer loseId) {
        this.loseId = loseId;
    }
}

```

```java
package com.qzx.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 17:15
 * @description: 消息生产者
 * @version: 1.0
 */
public final class MyProducer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyProducer.class);

    /**
     * 生产者
     */
    private static DefaultMQProducer producer = null;

    /**
     * 私有化构造方法
     */
    private MyProducer() {

    }

    /**
     * 初始化消息队列（生产者）
     */
    public static void init() {
        try {
            // 创建生产者
            producer = new DefaultMQProducer("herostory");
            // 设置nameServer
            producer.setNamesrvAddr("192.168.221.139:9876");
            // 启动生产者
            producer.start();
            // 设置发送消息重试次数
            producer.setRetryTimesWhenSendAsyncFailed(3);

            LOGGER.info("消息队列(生产者)启动成功!");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 指定的主题
     * @param msg   消息实体
     */
    public static void sendMsg(String topic, Object msg) {
        if (topic == null || msg == null) {
            return;
        }

        try {
            final Message message = new Message();
            message.setTopic(topic);
            message.setBody(JSONObject.toJSONBytes(msg));

            if (producer == null) {
                LOGGER.error("生产者未初始化!");
                return;
            }

            producer.send(message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

```

在UserAttkCmdHandler中，如果被攻击方的血量小于等于0的时候就发送一条VistorMsg的消息

```java
// 血量减小到0，广播UserDieResult消息
if(currentHp<=0){
        broadcastUserDieResult(targetUserId);
// 发送击杀成功消息到消息队列
final VictorMsg victorMsg=new VictorMsg();
        victorMsg.setWinnerId(userId);
        victorMsg.setLoseId(targetUserId);
        MyProducer.sendMsg("herostory_victor",victorMsg);
        }
```

### 消息队列消费者(排行榜进程)

消费者会订阅herostory_victor主题，并注册回调，在有消息消费的时候就将该消息取出并通过winnerId和loserId来更新redis中的排行榜Rank。而更新redis排行榜的逻辑为：首先统计winnerId和loserId用户胜利和失败的次数，存储在"
User_"+userId中的“Win”和"Lose"字段，然后获取winnerId中的胜利次数(Win字段)，最后修改Rank排名(添加一个{win,winnerId}对象)。

首先在RankService中添加刷新redis排行榜的逻辑

```java
/**
 * 刷新redis排行榜数据
 *
 * @param winnerId 赢家Id
 * @param loserId  输家Id
 */
public void refreshRedis(int winnerId,int loserId){
        if(winnerId<=0||loserId<=0){
        return;
        }

        try(final Jedis jedis=RedisUtil.getJedis()){
        if(jedis==null){
        return;
        }
        // 增加用户胜利和失败的次数
        jedis.hincrBy("User_"+winnerId,"Win",1);
        jedis.hincrBy("User_"+loserId,"Lose",1);
        // 获取winnerId胜利的次数
        int win=Integer.parseInt(jedis.hget("User_"+winnerId,"Win"));
        // 修改排名数据
        jedis.zadd("Rank",win,String.valueOf(winnerId));
        }catch(Exception e){
        LOGGER.error(e.getMessage(),e);
        }
        }
```

然后创建消费者，并在排行榜进程中进行初始化

```java
package com.qzx.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import com.qzx.herostory.rank.RankService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 20:10
 * @description: 消息队列消费者
 * @version: 1.0
 */
public class MyConsumer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumer.class);

    /**
     * 消费者
     */
    private static DefaultMQPushConsumer consumer = null;

    /**
     * 私有化构造方法
     */
    private MyConsumer() {

    }

    /**
     * 初始化consumer
     */
    public static void init() {
        try {
            // 创建消费者
            consumer = new DefaultMQPushConsumer("herostory");
            // 设置nameserver
            consumer.setNamesrvAddr("192.168.221.139:9876");
            // 订阅topic
            consumer.subscribe("herostory_victor", "*");
            // 注册回调
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 消费消息并且写入redis中
                    for (MessageExt msg : msgs) {
                        VictorMsg victorMsg = JSONObject.parseObject(msg.getBody(), VictorMsg.class);
                        LOGGER.info("从消息队列中获取消息：winnerId={},loserId={} ", victorMsg.getWinnerId(), victorMsg.getLoseId());
                        RankService.getInstance().refreshRedis(victorMsg.getWinnerId(), victorMsg.getLoseId());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            // 启动消费者
            consumer.start();
            LOGGER.info("消息队列(消费者)连接成功!");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

```

```java
package com.qzx.herostory;

import com.qzx.herostory.mq.MyConsumer;
import com.qzx.herostory.util.RedisUtil;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 20:09
 * @description: 排行榜进程
 * @version: 1.0
 */
public class RankApp {
    public static void main(String[] args) {
        // 初始化redis
        RedisUtil.init();
        // 初始化消息队列(消费者)
        MyConsumer.init();
    }
}

```

启动ServerMain和RankApp进程，并使用http://cdn0001.afrxvk.cn/hero_story/demo/step040/index.html?serverAddr=127.0.0.1:12345进行测试，可以看到排行榜数据如下：

![image-20210216210026975](D:\herostory\src\main\resources\images\image-20210216210026975.png)