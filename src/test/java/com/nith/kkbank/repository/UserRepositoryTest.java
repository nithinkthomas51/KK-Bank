package com.nith.kkbank.repository;

import com.nith.kkbank.entity.User;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Abc", "Def", "Ghi", "Male",
                "JKL, CHR", "KRL", "2024567890",
                new BigDecimal(BigInteger.ZERO), "abcdef07@gmail.com",
                "1234567890", "9876543210", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        user = null;
        userRepository.deleteAll();
    }

    @Test
    public void testExistsByEmail_Exists() {
        assertThat(userRepository.existsByEmail("abcdef07@gmail.com"))
                .isTrue();
    }

    @Test
    public void testExistsByEmail_Not_Exists() {
        assertThat(userRepository.existsByEmail("stuvwx07@gmail.com"))
                .isFalse();
    }

    @Test
    public void testExistsByAccountNumber_Exists() {
        assertThat(userRepository.existsByAccountNumber("2024567890"))
                .isTrue();
    }

    @Test
    public void testExistsByAccountNumber_Not_Exists() {
        assertThat(userRepository.existsByAccountNumber("2024567891"))
                .isFalse();
    }

    @Test
    public void testFindByAccountNumber() {
        User foundUser = userRepository.findByAccountNumber("2024567890");
        assertThat(foundUser.getAccountNumber()).isEqualTo(user.getAccountNumber());
        assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
    }
}
