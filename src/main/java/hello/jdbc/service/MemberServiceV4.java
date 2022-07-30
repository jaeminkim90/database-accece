package hello.jdbc.service;

import org.springframework.transaction.annotation.Transactional;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;

/**
 *  예외 누수 문제 해결
 *  SQLException 제거
 *
 *  MemberRepository 인터페이스 의존
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepository memberRepository; // MemberRepository 인터페이스에 의존한다

    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository; // 생성자 주입도 인터페이스를 받는다
    }

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) { // throws SQLException 제거가 가능(Repo에서 언체크로 변환됨)
        bizLogic(fromId, toId, money);
    }

    /**
     * 비즈니스 로직: member의 money update
     **/
    private void bizLogic(String fromId, String toId, int money) { // throws SQLException 제거가 가능(Repo에서 언체크로 변환됨)
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
