package me.ctf.lab.ssl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chentiefeng
 * @date 2020-06-10 10:50
 */
@RestController
public class SslServer {
    @RequestMapping("/ssl")
    public String hello(@RequestParam("host") String host) {
        return "hello " + host;
    }
}
