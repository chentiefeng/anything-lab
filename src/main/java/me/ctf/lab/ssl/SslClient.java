package me.ctf.lab.ssl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.ssl.SSLSocketFactoryBuilder;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * @author chentiefeng
 * @date 2020-06-10 10:45
 */
public class SslClient {
    public static void main(String[] args) throws Exception {
        //客户端jks路径
        InputStream inputStream = new ClassPathResource("ssl/client/127.0.0.1-ssl-client.jks").getInputStream();
        InputStream trustInputStream = new ClassPathResource("ssl/client/127.0.0.1-ssl-client-trust.jks").getInputStream();
        //生成时的密码，最好两次一样
        String ksPwd = "127";
        String trustKsPwd = "127";
        //ssl 密钥对
        KeyStore ownKeyStore = KeyStore.getInstance("JKS");
        ownKeyStore.load(inputStream, ksPwd.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ownKeyStore, ksPwd.toCharArray());
        //信任 密钥对
        KeyStore trustKeyStore = KeyStore.getInstance("JKS");
        trustKeyStore.load(trustInputStream, trustKsPwd.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustKeyStore);

        SSLSocketFactory sslSocketFactory = SSLSocketFactoryBuilder.create()
                .setKeyManagers(kmf.getKeyManagers())
                .setTrustManagers(tmf.getTrustManagers())
                .setProtocol("TLSv1.2")
                .setSecureRandom(new SecureRandom())
                .build();
        //假设花旗的接口地址是 https://127.0.0.1:4000/ssl
        HttpRequest httpRequest = HttpUtil.createPost("https://127.0.0.1:4000/ssl");
        httpRequest.setSSLSocketFactory(sslSocketFactory);
        String host = "127.0.0.1-client";
        httpRequest.form("host",host);
        System.out.println("请求花旗报文：" + host);
        String resp = httpRequest.execute().body();
        System.out.println("花旗返回报文：" + resp);
    }
}
