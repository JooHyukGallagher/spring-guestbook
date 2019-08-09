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
2. 방명록 구현
    * guestbook, log 테이블 생성
        ~~~
        CREATE TABLE guestbook (
        	id bigint(20) unsigned NOT NULL auto_increment,
            name varchar(255) NOT NULL,
            content text,
            regdate datetime,
            PRIMARY KEY (id)
        );
        ~~~
        ~~~
        CREATE TABLE log (
        	id bigint(20) unsigned NOT NULL auto_increment,
            ip varchar(255) NOT NULL,
            method varchar(10) NOT NULL,
            regdate datetime,
            PRIMARY KEY (id)
        );
        ~~~
    * 데이터 처리를 위한 GuestBook, Log DTO 생성
        ~~~
        public class GuestBook {
            private Long id;
            private String name;
            private String content;
            private Date regdate;
            public Long getId() {
                return id;
            }
            public void setId(Long id) {
                this.id = id;
            }
            public String getName() {
                return name;
            }
            public void setName(String name) {
                this.name = name;
            }
            public String getContent() {
                return content;
            }
            public void setContent(String content) {
                this.content = content;
            }
            public Date getRegdate() {
                return regdate;
            }
            public void setRegdate(Date regdate) {
                this.regdate = regdate;
            }
            @Override
            public String toString() {
                return "Guestbook [id=" + id + ", name=" + name + ", content=" + content + ", regdate=" + regdate + "]";
            }
        }
        ~~~
        ~~~
        public class Log {
            private Long id;
            private String ip;
            private String method;
            private Date regdate;
            public Long getId() {
                return id;
            }
            public void setId(Long id) {
                this.id = id;
            }
            public String getIp() {
                return ip;
            }
            public void setIp(String ip) {
                this.ip = ip;
            }
            public String getMethod() {
                return method;
            }
            public void setMethod(String method) {
                this.method = method;
            }
            public Date getRegdate() {
                return regdate;
            }
            public void setRegdate(Date regdate) {
                this.regdate = regdate;
            }
            @Override
            public String toString() {
                return "Log [id=" + id + ", ip=" + ip + ", method=" + method + ", regdate=" + regdate + "]";
            }
        }
        ~~~
    * 영속 계층 구현
        * GuestbookDaoSqls.class에 guest테이블에 대한 쿼리 작성
            ~~~
            public class GuestbookDaoSqls {
                public static final String SELECT_PAGING = "SELECT id, name, content, regdate FROM guestbook ORDER BY id DESC limit :start, :limit";
                public static final String DELETE_BY_ID = "DELETE FROM guestbook WHERE id = :id";
                public static final String SELECT_COUNT = "SELECT count(*) FROM guestbook";
            }
            ~~~
        * GuestbookDao.class, LogDao.class 작성
            ~~~
            @Repository
            public class LogDao {
                private NamedParameterJdbcTemplate jdbcTemplate;
                private SimpleJdbcInsert insertAction;
            
                public LogDao(DataSource dataSource){
                    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
                    this.insertAction = new SimpleJdbcInsert(dataSource)
                            .withTableName("log")
                            .usingGeneratedKeyColumns("id");
                }
            
                public Long insert(Log log) {
                    SqlParameterSource params = new BeanPropertySqlParameterSource(log);
                    return insertAction.executeAndReturnKey(params).longValue();
                }
            }
            ~~~
            ~~~
            @Repository
            public class GuestbookDao {
                private NamedParameterJdbcTemplate jdbcTemplate;
                private SimpleJdbcInsert insertAction;
                private RowMapper<Guestbook> rowMapper = BeanPropertyRowMapper.newInstance(Guestbook.class);
            
                public GuestbookDao(DataSource dataSource) {
                    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
                    this.insertAction = new SimpleJdbcInsert(dataSource)
                            .withTableName("guestbook")
                            .usingGeneratedKeyColumns("id");
                }
            
                public List<Guestbook> selectAll(Integer start, Integer limit) {
                    Map<String, Integer> params = new HashMap<>();
                    params.put("start", start);
                    params.put("limit", limit);
                    return jdbcTemplate.query(SELECT_PAGING, params, rowMapper);
                }
            
                public Long insert(Guestbook guestbook){
                    SqlParameterSource params = new BeanPropertySqlParameterSource(guestbook);
                    return insertAction.executeAndReturnKey(params).longValue();
                }
            
                public int deleteById(Long id){
                    Map<String, ?> params = Collections.singletonMap("id", id);
                    return jdbcTemplate.update(DELETE_BY_ID, params);
                }
            
                public int selectCount() {
                    return jdbcTemplate.queryForObject(SELECT_COUNT, Collections.<String, Object>emptyMap(), Integer.class);
                }
            }
            ~~~
        * 테스트코드 작성
            ~~~
            public class GuestbookDaoTest {
                public static void main(String[] args) {
                    ApplicationContext ac = new AnnotationConfigApplicationContext(ApplicationConfig.class);
            //        GuestbookDao guestbookDao = ac.getBean(GuestbookDao.class);
            //
            //        Guestbook guestbook = new Guestbook();
            //        guestbook.setName("김주혁");
            //        guestbook.setContent("안녕하세요. 엘지트윈스 팬 입니다.");
            //        guestbook.setRegdate(new Date());
            //        Long id = guestbookDao.insert(guestbook);
            //        System.out.println("id : " + id);
            
                    LogDao logDao = ac.getBean(LogDao.class);
                    Log log = new Log();
                    log.setIp("127.0.0.1");
                    log.setMethod("insert");
                    log.setRegdate(new Date());
                    logDao.insert(log);
                }
            }
            ~~~ 
    * 서비스 계층 구현
        * GuestbookService 인터페이스 구현
            ~~~
            public interface GuestbookService {
                public static final int LIMIT = 5;
                public List<Guestbook> getGuestBooks(int start);
                public int deleteGuestbook(Long id, String ip);
                public Guestbook addGuestbook(Guestbook guestbook, String ip);
                public int getCount();
            }
            ~~~   
        * GuestbookServiceImpl 구현클래스 생성
            ~~~
            @Service
            public class GuestbookServiceImpl implements GuestbookService {
                @Autowired
                GuestbookDao guestbookDao;
            
                @Autowired
                LogDao logDao;
            
                @Override
                @Transactional
                public List<Guestbook> getGuestBooks(int start) {
                    List<Guestbook> list = guestbookDao.selectAll(start, GuestbookService.LIMIT);
                    return list;
                }
            
                @Override
                @Transactional(readOnly = false)
                public int deleteGuestbook(Long id, String ip) {
                    int deleteCount = guestbookDao.deleteById(id);
                    Log log = new Log();
                    log.setIp(ip);
                    log.setMethod("delete");
                    log.setRegdate(new Date());
                    logDao.insert(log);
                    return deleteCount;
                }
            
                @Override
                @Transactional(readOnly = false)
                public Guestbook addGuestbook(Guestbook guestbook, String ip) {
                    guestbook.setRegdate(new Date());
                    Long id = guestbookDao.insert(guestbook);
                    guestbook.setId(id);
            
                    Log log = new Log();
                    log.setIp(ip);
                    log.setMethod("insert");
                    log.setRegdate(new Date());
                    logDao.insert(log);
            
                    return guestbook;
                }
            
                @Override
                public int getCount() {
                    return guestbookDao.selectCount();
                }
            }
            ~~~
        * 테스트코드 작성
            ~~~
            public class GuestbookServiceTest {
                public static void main(String[] args) {
                    ApplicationContext ac = new AnnotationConfigApplicationContext(ApplicationConfig.class);
                    GuestbookService guestbookService = ac.getBean(GuestbookService.class);
            
                    Guestbook guestbook = new Guestbook();
                    guestbook.setName("kang kyungmi22");
                    guestbook.setContent("반갑습니다. 여러분.");
                    guestbook.setRegdate(new Date());
                    Guestbook result = guestbookService.addGuestbook(guestbook, "127.0.0.1");
                    System.out.println(result);
                }
            }
            ~~~
    * 프레젠테이션 계층 구현
        * GuestbookController.class 구현
            ~~~
            @Controller
            public class GuestbookController {
                @Autowired
                GuestbookService guestbookService;
            
                @GetMapping(path = "/list")
                public String list(@RequestParam(name = "start", required = false, defaultValue = "0") int start,
                                   ModelMap modelMap) {
                    // start로 시작하는 방명록 목록 구하기
                    List<Guestbook> list = guestbookService.getGuestBooks(start);
            
                    // 전체 페이지수 구하기
                    int count = guestbookService.getCount();
                    int pageCount = count / GuestbookService.LIMIT;
                    if (count % GuestbookService.LIMIT > 0)
                        pageCount++;
            
                    // 페이지 수만큼 start의 값을 리스트로 저장
                    // 예를 들면 페이지수가 3이면
                    // 0, 5, 10 이렇게 저장된다.
                    // list?start=0, list?start=5, list?start=10 으로 링크가 걸린다.
                    List<Integer> pageStartList = new ArrayList<>();
                    for (int i = 0; i < pageCount; i++) {
                        pageStartList.add(i * GuestbookService.LIMIT);
                    }
            
                    modelMap.addAttribute("list", list);
                    modelMap.addAttribute("count", count);
                    modelMap.addAttribute("pageStartList", pageStartList);
            
                    return "list";
                }
            
                @PostMapping(path = "/write")
                public String write(@ModelAttribute Guestbook guestbook,
                                    HttpServletRequest request) {
                    String clientIp = request.getRemoteAddr();
                    System.out.println("clientIp: " + clientIp);
                    guestbookService.addGuestbook(guestbook, clientIp);
                    return "redirect:list";
                }
            }
            ~~~
        * list.jsp 구현
            ~~~
            <%@ page contentType="text/html;charset=UTF-8" language="java" %>
            <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
            <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                <title>방명록 목록</title>
            </head>
            <body>
            
            <h1>방명록</h1>
            <br> 방명록 전체 수 : ${count }
            <br>
            <br>
            
            <c:forEach items="${list}" var="guestbook">
            
                ${guestbook.id }<br>
                ${guestbook.name }<br>
                ${guestbook.content }<br>
                ${guestbook.regdate }<br>
            
            </c:forEach>
            <br>
            
            <c:forEach items="${pageStartList}" var="pageIndex" varStatus="status">
                <a href="list?start=${pageIndex}">${status.index +1 }</a>&nbsp; &nbsp;
            </c:forEach>
            
            <br>
            <br>
            <form method="post" action="write">
                name : <input type="text" name="name"><br>
                <textarea name="content" cols="60" rows="6"></textarea>
                <br> <input type="submit" value="등록">
            </form>
            </body>
            </html>
            ~~~