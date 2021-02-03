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

## 问题（待解决）

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

