package me.ctf.lab.k8s;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author chentiefeng
 * @date 2021/3/17 11:17
 */
@Configuration(proxyBeanMethods = false)
public class LocalRibbonConfiguration {
    private final static String SERVER_LIST_FILE = System.getProperty("user.home") + "/server_list.properties";

    /**
     * 开发环境注册本地文件
     */
    static class LocalRegister implements CommandLineRunner {
        @Value("${server.port}")
        private int port;
        @Value("${spring.application.name}")
        private String serviceName;

        @Override
        public void run(String... args) throws Exception {
            File file = new File(SERVER_LIST_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            Properties props = new Properties();
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            props.load(in);
            OutputStream fos = new FileOutputStream(file);
            props.setProperty(serviceName, String.valueOf(port));
            props.store(fos, "Update '" + serviceName + "' value");
            in.close();
            fos.close();
        }
    }

    @Bean
    public LocalRegister localRegister() {
        return new LocalRegister();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config) {
        LocalServerList serverList = new LocalServerList();
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    /**
     * 本地服务列表
     */
    @Slf4j
    static class LocalServerList extends AbstractServerList<Server> implements ServerList<Server> {
        IClientConfig clientConfig;

        @Override
        public void initWithNiwsConfig(IClientConfig clientConfig) {
            this.clientConfig = clientConfig;
        }

        @Override
        public List<Server> getInitialListOfServers() {
            return Collections.emptyList();
        }

        @Override
        public List<Server> getUpdatedListOfServers() {
            File file = new File(SERVER_LIST_FILE);
            if (file.exists()) {
                Properties props = new Properties();
                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(file));
                    props.load(in);
                    String serviceName = clientConfig.getClientName();
                    if (props.containsKey(serviceName)) {
                        log.info("local ribbon active, static ip is 127.0.0.1");
                        return Collections.singletonList(new Server("127.0.0.1", Integer.parseInt(props.getProperty(serviceName))));
                    }
                    return Collections.emptyList();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return Collections.emptyList();
        }

    }

}
