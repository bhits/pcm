package gov.samhsa.c2s.pcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan(basePackageClasses = {PcmApplication.class, Jsr310JpaConverters.class})
@EnableAspectJAutoProxy
public class PcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(PcmApplication.class, args);
    }
}