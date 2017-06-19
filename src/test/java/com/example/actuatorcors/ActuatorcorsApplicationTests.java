package com.example.actuatorcors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.test.web.servlet.setup.MockMvcConfigurerAdapter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.RequestContextFilter;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActuatorcorsApplicationTests {

    @Autowired
    private ManagementServerProperties managementServerProperties;

    @Resource
    private WebApplicationContext wac;

    @Autowired
    private RequestContextFilter requestContextFilter;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    protected MockMvc mockMvc;

    protected MockMvc mockMvcHttps;

    @Before
    public void setUp() {
        HttpSession defaultSession = new MockHttpSession();
        MockMvcConfigurer sessionConfigurer = new MockMvcConfigurerAdapter() {
            @Override
            public RequestPostProcessor beforeMockMvcCreated(ConfigurableMockMvcBuilder<?> builder, WebApplicationContext context) {
                return request -> {
                    request.setSession(defaultSession);
                    return request;
                };
            }
        };
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilters(springSecurityFilter).alwaysDo(print()).apply(sessionConfigurer).build();

        MockHttpServletRequest sessionRequest = new MockHttpServletRequest();
        sessionRequest.setSession(defaultSession);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(sessionRequest));
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void testAccessingActuatorsWithTokenPreFlightRequestWorksFine() throws Exception {

        String token = "invalid-token";

        mockMvc.perform(get(managementServerProperties.getContextPath() + "/env").header("x-auth-token", token).secure(true)).andExpect(
                        status().isUnauthorized());
        //
        //        token = mockMvc.perform(get(repositoryRestProperties.getBasePath() + "/auth").header(NemesisHttpHeaders.X_NEMESIS_USERNAME, "admin").header(
        //                        NemesisHttpHeaders.X_NEMESIS_PASSWORD, "nimda").accept(MediaType.APPLICATION_JSON_UTF8_VALUE).contentType(
        //                        MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn().getResponse().getHeader(NemesisHttpHeaders.X_NEMESIS_TOKEN);

        //        mockMvc.perform(options(managementServerProperties.getContextPath() + "/health").accept(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(
        //                        status().isOk()).andExpect(
        //                        header().string("access-control-allow-methods", is(equalTo("GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS")))).andExpect(
        //                        header().string("access-control-allow-headers", is(equalTo("x-nemesis-token, x-requested-with")))).andExpect(
        //                        header().string("access-control-allow-origin", is(equalTo("http://localhost:8080"))));

        mockMvc.perform(options(managementServerProperties.getContextPath() + "/env").accept(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(
                        status().isOk()).andExpect(
                        header().string("access-control-allow-methods", is(equalTo("GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS")))).andExpect(
                        header().string("access-control-allow-headers", is(equalTo("x-nemesis-token, x-requested-with")))).andExpect(
                        header().string("access-control-allow-origin", is(equalTo("http://localhost:8080"))));
    }

}
