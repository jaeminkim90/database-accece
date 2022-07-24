package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *  트랜잭션 - 트랜잭션 템플릿
 *  Service에서 Spring이 제공하는 TransactionManager를 연결하는 코드 실습
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository; // DB 접근을 위해 repository 주입 받음

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        // PlatformTransactionManager을 파라미터에 넣어서 TransactionTemplate를 만들 수 있다. -> 유연성이 생기기 때문에 외부 주입방식보다 좋다.
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 기존에 getTransaction()을 이용해 status를 얻어 트랜잭션매니저 설정에 사용했던 것을 아래와 같이 간소화 할 수 있다
        // 트랜잭션매니저를 이용한 commit과 rollback 기능까지 모두 대체가 가능하다
        // executeWithoutResult()가 실행되면 해당 코드 안에서 트랜잭션을 시작하고 아래 비즈니스로직을 수행한다.
        // 성공적으로 작업이 완료되면 commit을 그렇지 않으면 rollback을 한다
        txTemplate.executeWithoutResult((status) -> {
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * 비즈니스 로직: member의 money update
     **/
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        Member fromMember = memberRepository.findById(fromId); // 보내는 멤버
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money); // fromMember의 money 출금 처리
        // 예외 발생 상황 만들기
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money); // fromMember의 money 출금 처리
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
