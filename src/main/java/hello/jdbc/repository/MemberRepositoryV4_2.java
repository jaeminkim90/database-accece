package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource); // 에러코드를 찾아서 예외를 전환해주는 구현체 사용
    }

    @Override // 인터페이스 사용 시 overide를 넣는 것이 좋다
    public Member save(Member member) { // throws SQLException 는 생략해도 된다(unchecked 예외 사용)

        String sql = "insert into member(member_id, money) values (?,?)"; // 쿼리
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 실행
            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally {
            close(con, pstmt, null); // 항상 호출을 보장해야 한다
        }

    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) { // 한 번은 호출을 해줘야 커서가 실제 데이터가 있는 쪽으로 넘어간다
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException(
                    "member not found memberId=" + memberId); // memberId를 예외 메시지에 넣지 않으면 어떤 memberId가 문제인지 알 수 없다
            }
        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs); // close 처리는 중요하다
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money =? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("update ", sql, e);
        } finally {
            close(con, pstmt, null); // 항상 호출을 보장해야 한다
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id =?";
        log.info("delete");

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null); // 항상 호출을 보장해야 한다
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs) {

        // 아래와 같은 구조로 코드를 작성하면, 각각의 객체 close 과정에서 error가 발생해도 con.close를 수행할 수 있다
        // 각각의 메서드마다 예외 처리가 되어있다.
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 주의! 트랜잭션 동기회를 사용하려면 DataSourceUtils를 사용해야 한다.
        // DataSourceUtils를 사용해 release 하면, <트랜잭션 동기화 매니저>에서 가져온 커넥션은 close하지 않고 반환한다
        DataSourceUtils.releaseConnection(con, dataSource);

    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        // repository에서 커넥션이 필요할 때 DataSourceUtils을 이용하면 <트랜잭션 동기화 매니저>에서 커넥션을 가져다 쓰게 된다.
        Connection con = DataSourceUtils.getConnection(dataSource);

        log.info("get connection = {}, class = {}", con, con.getClass());
        return con;
    }

}
