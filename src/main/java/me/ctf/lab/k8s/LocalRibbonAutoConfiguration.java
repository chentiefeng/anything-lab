package me.ctf.lab.k8s;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author chentiefeng
 * @date 2021/3/17 16:00
 */
@Profile(value = {"dev"})
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = {LocalRibbonConfiguration.class})
public class LocalRibbonAutoConfiguration {
    public LocalRibbonAutoConfiguration() {
    }
}
