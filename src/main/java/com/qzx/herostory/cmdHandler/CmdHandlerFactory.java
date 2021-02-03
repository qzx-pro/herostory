package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import com.qzx.herostory.util.PackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiaozixin
 * @Date: 2021/2/2 - 02 - 02 - 19:23
 * @Description: 命令处理器工厂
 * @version: 1.0
 */
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
        // 获取GameMsgProtocolNew所有的内部类
        Class<?>[] innerClazzArray = GameMsgProtocolLogin.class.getDeclaredClasses();
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
