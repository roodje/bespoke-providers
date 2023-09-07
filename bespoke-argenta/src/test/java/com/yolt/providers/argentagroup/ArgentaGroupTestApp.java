package com.yolt.providers.argentagroup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ArgentaGroupTestConfiguration.class)
public class ArgentaGroupTestApp {

}
