package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.annotation.Transactional;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

/**
 *  예외 누수 문제 해결
 *  SQLException 제거
 *
 *  MemberRepository
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepositoryV3 memberRepository; // DB 접근을 위해 repository 주입 받음

    public MemberServiceV4(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
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
