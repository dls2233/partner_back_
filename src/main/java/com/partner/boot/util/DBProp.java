package com.partner.boot.util;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

@Data
@Builder
public class DBProp {
    private String url;
    private String username;
    private String password;

}
