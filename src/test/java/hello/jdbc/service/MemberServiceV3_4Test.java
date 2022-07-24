package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - DataSource, transactionManager 자동 등록
 */
@Slf4j
@SpringBootTest // 테스트를 실행할 때 스프링 컨테이너를 띄우고 의존관계를 등록하게 된다. 필드는 @Autowired로 의존관계 주입을 받아야 한다.
class MemberServiceV3_4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";


    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_3 memberService;


    @TestConfiguration // 테스트 안에서 사용하면, 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
    static class TestConfig {

        private final DataSource dataSource;

        // 생성자를 이용해 의존관계를 주입받는다
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    // @BeforeEach
    // void before() {
    //     // DriverManager 대신 Hikari를 사용해도 무방하다
    //     DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD); // 데이터 소스 셋팅
    //
    //     memberRepository = new MemberRepositoryV3(dataSource); // 리포지토리 주입
    //
    //     // 트랜잭션 매니저 객체 생성
    //     // 트랜잭션매니저를 만들 때 파라미터로 dataSource를 넘긴다. 트랜잭션매니저가 dataSource를 이용해 커넥션을 생성한다. dataSource가 없으면 커넥션을 만들 수 없다
    //     PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    //     memberService = new MemberServiceV3_3(memberRepository); // 트랜잭션 매니저를 파라미터에 담아 서비스 호출
    // }

    @AfterEach
    void after () throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
   void AopCheck() {
        log.info("memberService class = {}", memberService.getClass());
        log.info("membmerRepository class = {}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)); // AopProxy여부를 확인할 수 있다
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        log.info("START FX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END FX");

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when - 예외가 발생하는 상황응 검증한다
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
            .isInstanceOf(IllegalStateException.class);
        // 수동 커밋 모드로 동작하기 때문에 예외가 발생하면 rollback 처리한다

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }

}