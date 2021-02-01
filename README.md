# 第一天

使用netty实现一个游戏服务器，地址为127.0.0.1，端口为12345，游戏前端测试地址为

http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1

测试结果如图：

![image-20210201162119083](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201162119083.png)

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

![image-20210201162414395](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201162414395.png)

测试工具：

![image-20210201162431158](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201162431158.png)

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

![image-20210201163816507](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201163816507.png)

可以看到成功生成java文件

![image-20210201163925547](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201163925547.png)

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

![image-20210201194453377](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201194453377.png)

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

![image-20210201201717917](C:\Users\qiaozixin\AppData\Roaming\Typora\typora-user-images\image-20210201201717917.png)