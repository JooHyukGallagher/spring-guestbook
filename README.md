Spring MVC를 이용해서 방명록 만들기
=================
* 방명록 요구사항 1/6
	* 방명록 정보는 guestbook 테이블에 저장된다.
	* id는 자동으로 입력된다.
	* id, 이름, 내용, 등록일을 저장한다.
* 방명록 요구사항 2/6
	* http://localhost:8080/guestbook/을 요청하면 자동으로 /guestbook/list로 리다이렉팅 한다.
	* 방명록이 없으면 건수는 0이 나오고 아래에 방명록을 입력하는 폼이 보여진다.
* 방명록 요구사항 3/6
	* 이름과 내용을 입력하고, 등록버튼을 누르면 /guestbook/write URL로 입력한 값을 전달하여 저장한다.
	* 값이 저장된 이후에는 /guestbook/list로 리다이렉트 된다.
* 방명록 요구사항 4/6
	* 입력한 한개의 정보가 보여진다.
	* 방명록 내용과 폼 사이의 숫자는 방명록 페이지 링크. 방명록 5건당 1페이지로 설정한다.
* 방명록 요구사항 5/6
	* 방명록이 6건 입력되자 아래 페이지 수가 2건 보여진다. 1페이지를 누르면 /guestbook/list?start=0 을 요청하고,
	2페이지를 누르면 /guestbook/list?start=5를 요청하게 된다.
    * /guestbook/list는 /guestbook/list?start=0과 결과가 같다.
* 방명록 요구사항 6/6
    * 방명록에 글을 쓰거나, 방명록의 글을 삭제할 때는 Log테이블에 클라이언트의 ip주소, 등록(삭제) 시간, 등록/삭제(method컬럼)정보를 데이터베이스에 저장한다.
    * 사용하는 테이블은 log이다.
    * id는 자동으로 입력되도록 한다.

1. 개발 환경 설정
    * Maven 프로젝트 생성후 pom.xml에 Spring MVC 관련 의존성 주입
        ~~~
            <spring.version>4.3.5.RELEASE</spring.version>
            <!-- jackson -->
            <jackson2.version>2.8.6</jackson2.version>
        ~~~
        ~~~
         <!-- SPRING -->
            <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-context</artifactId>
              <version>${spring.version}</version>
            </dependency>
            <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-jdbc</artifactId>
              <version>${spring.version}</version>
            </dependency>
            <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-webmvc</artifactId>
              <version>${spring.version}</version>
            </dependency>
            <dependency>
              <groupId>org.springframework</groupId>
              <artifactId>spring-tx</artifactId>
              <version>${spring.version}</version>
            </dependency>
            <!-- MYSQL -->
        
            <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>5.1.45</version>
            </dependency>
            <!-- DATASOURCE -->
            <dependency>
              <groupId>org.apache.commons</groupId>
              <artifactId>commons-dbcp2</artifactId>
              <version>2.0</version>
            </dependency>
        
        
            <!-- Servlet JSP JSTL -->
            <dependency>
              <groupId>javax.servlet</groupId>
              <artifactId>javax.servlet-api</artifactId>
              <version>3.1.0</version>
              <scope>provided</scope>
            </dependency>
            <dependency>
              <groupId>javax.servlet.jsp</groupId>
              <artifactId>javax.servlet.jsp-api</artifactId>
              <version>2.3.1</version>
              <scope>provided</scope>
            </dependency>
            <dependency>
              <groupId>jstl</groupId>
              <artifactId>jstl</artifactId>
              <version>1.2</version>
            </dependency>
        
        
            <!-- Jackson Module -->
            <dependency>
              <groupId>com.fasterxml.jackson.core</groupId>
              <artifactId>jackson-databind</artifactId>
              <version>${jackson2.version}</version>
            </dependency>
            <dependency>
              <groupId>com.fasterxml.jackson.datatype</groupId>
              <artifactId>jackson-datatype-jdk8</artifactId>
              <version>${jackson2.version}</version>
            </dependency>
        ~~~
    * Web 관련 Java Config 설정 파일 생성
        ~~~
        @Configuration
        @EnableWebMvc
        @ComponentScan(basePackages = "kr.or.connect.guestbook.controller")
        public class WebMvcContextConfiguration extends WebMvcConfigurerAdapter {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCachePeriod(31556926);
                registry.addResourceHandler("/img/**").addResourceLocations("/img/").setCachePeriod(31556926);
                registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(31556926);
            }
        
            // default servlet handler를 사용하게 합니다.
            @Override
            public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
                configurer.enable();
            }
        
            @Override
            public void addViewControllers(final ViewControllerRegistry registry) {
                System.out.println("addViewControllers가 호출됩니다. ");
                registry.addViewController("/").setViewName("index");
            }
        
            @Bean
            public InternalResourceViewResolver getInternalResourceViewResolver() {
                InternalResourceViewResolver resolver = new InternalResourceViewResolver();
                resolver.setPrefix("/WEB-INF/views/");
                resolver.setSuffix(".jsp");
                return resolver;
            }
        }
        ~~~
    * Application 관련 설정 파일 생성
        ~~~
        @Configuration
        @EnableTransactionManagement
        public class DBConfig implements TransactionManagementConfigurer {
            private String driverClassName = "com.mysql.jdbc.Driver";
        
            private String url = "jdbc:mysql://localhost:3306/connectdb?useUnicode=true&characterEncoding=utf8";
        
            private String username = "connectuser";
        
            private String password = "connect123!@#";
        
            @Bean
            public DataSource dataSource() {
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName(driverClassName);
                dataSource.setUrl(url);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                return dataSource;
            }
        
            @Override
            public PlatformTransactionManager annotationDrivenTransactionManager() {
                return transactionManger();
            }
        
            @Bean
            public PlatformTransactionManager transactionManger() {
                return new DataSourceTransactionManager(dataSource());
            }
        }
        ~~~
        * @EnableTransactionManagement 어노테이션은 트랜잭션관련 설정을 자동으로 해준다.
        ~~~
        @Configuration
        @ComponentScan(basePackages = {"kr.or.connect.guestbook.service", "kr.or.connect.guestbook.dao"})
        @Import({DBConfig.class})
        public class ApplicationConfig {
            
        }
        ~~~
        * web.xml에 ContextLoaderListener, DispatcherSerlvet 등록
        ~~~
        <web-app>
            <display-name>Archetype Created Web Application</display-name>
        
            <context-param>
                <param-name>contextClass</param-name>
                <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext
                </param-value>
            </context-param>
            <context-param>
                <param-name>contextConfigLocation</param-name>
                <param-value>kr.or.connect.guestbook.config.ApplicationConfig
                </param-value>
            </context-param>
            <listener>
                <listener-class>org.springframework.web.context.ContextLoaderListener
                </listener-class>
            </listener>
        
            <servlet>
                <servlet-name>mvc</servlet-name>
                <servlet-class>org.springframework.web.servlet.DispatcherServlet
                </servlet-class>
                <init-param>
                    <param-name>contextClass</param-name>
                    <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext
                    </param-value>
                </init-param>
                <init-param>
                    <param-name>contextConfigLocation</param-name>
                    <param-value>kr.or.connect.guestbook.config.WebMvcContextConfiguration
                    </param-value>
                </init-param>
                <load-on-startup>1</load-on-startup>
            </servlet>
            <servlet-mapping>
                <servlet-name>mvc</servlet-name>
                <url-pattern>/</url-pattern>
            </servlet-mapping>
        
            <filter>
                <filter-name>encodingFilter</filter-name>
                <filter-class>org.springframework.web.filter.CharacterEncodingFilter
                </filter-class>
                <init-param>
                    <param-name>encoding</param-name>
                    <param-value>UTF-8</param-value>
                </init-param>
            </filter>
            <filter-mapping>
                <filter-name>encodingFilter</filter-name>
                <url-pattern>/*</url-pattern>
            </filter-mapping>
        </web-app>
        ~~~
    * WEB-INF -> views 폴더 생성 -> index.jsp 작성
        ~~~
        <%@ page language="java" contentType="text/html; charset=UTF-8"
                 pageEncoding="UTF-8"%>
        <%
            response.sendRedirect("list");
        %>
        ~~~