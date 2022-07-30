package hello.jdbc.exception.translator;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

    }

    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammer";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeQuery();

        } catch (SQLException e) {
            // 문법 오류로 인해 예외 발생
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
            throw new BadSqlGrammarException(e);
            int errorCode = e.getErrorCode();
            log.info("errorCode = {}", errorCode);
            log.info("error", e);

        } finally {

        }
    }
}

