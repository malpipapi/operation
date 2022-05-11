package com.minionz.backend.user.service;

import com.minionz.backend.common.domain.Message;
import com.minionz.backend.common.exception.BadRequestException;
import com.minionz.backend.common.exception.NotEqualsException;
import com.minionz.backend.common.exception.NotFoundException;
import com.minionz.backend.user.controller.dto.JoinRequestDto;
import com.minionz.backend.user.controller.dto.LoginRequestDto;
import com.minionz.backend.user.controller.dto.LoginResponseDto;
import com.minionz.backend.user.controller.dto.UserPageResponseDto;
import com.minionz.backend.user.domain.User;
import com.minionz.backend.user.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void 회원가입_성공_테스트_유저() {
        //given
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .name("정재욱")
                .email("operation@naver.com")
                .age("라이언")
                .password("1234")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        //when
        Message message = userService.signUp(joinRequestDto);
        //then
        assertThat(message.getMessage()).isEqualTo("회원가입 성공");
    }

    @Test
    void 회원탈퇴_성공_테스트_유저() {
        //given
        User user = User.builder()
                .email("wodnr8462@naver.com")
                .name("정재욱")
                .password("1234")
                .age("13")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        User save = userRepository.save(user);
        //when
        Message message = userService.withdraw(save.getId());
        //then
        assertThat(message.getMessage()).isEqualTo("회원탈퇴 성공");
    }

    @Test
    void 회원중복_성공_테스트() {
        //given
        User user = User.builder()
                .email("wodnr8462@naver.com")
                .name("정재욱")
                .password("1234")
                .age("15")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        userRepository.save(user);
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .name("정재욱")
                .email("wodnr8462@naver.com")
                .age("라이언")
                .password("1234")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        //when
        //then
        assertThatThrownBy(() -> userService.signUp(joinRequestDto)).isInstanceOf(BadRequestException.class)
                .hasMessage("해당 유저 이메일이 중복입니다.");
    }

    @Test
    void 로그인_성공_테스트_유저() {
        //given
        JoinRequestDto joinRequestDto = JoinRequestDto.builder()
                .name("정재욱")
                .email("operation@naver.com")
                .age("라이언")
                .password("1234")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        userService.signUp(joinRequestDto);
        User findUser = userRepository.findByEmail("operation@naver.com")
                .orElseThrow(() -> new NotFoundException("회원가입 실패"));
        LoginRequestDto LoginRequestDto = new LoginRequestDto("operation@naver.com", "1234");
        //when
        LoginResponseDto login = userService.login(LoginRequestDto);
        //then
        assertThat(login.getId()).isEqualTo(findUser.getId());
    }

    @Test
    void 로그인_아이디_불일치_테스트() {
        //given
        User user = User.builder()
                .email("wodnr@naver.com")
                .name("재욱")
                .password("123456")
                .age("18")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        LoginRequestDto LoginRequestDto = new LoginRequestDto("jh3j741@naver.com", "123456");
        userRepository.save(user);
        //when
        //then
        assertThatThrownBy(() -> userService.login(LoginRequestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 유저가 존재하지 않습니다.");
    }

    @Test
    void 로그인_비밀번호_불일치_테스트() {
        //given
        User user = User.builder()
                .email("wodnr@naver.com")
                .name("재욱")
                .password("123456")
                .age("20")
                .gender("남자")
                .height("15")
                .weight("15")
                .build();
        LoginRequestDto LoginRequestDto = new LoginRequestDto("wodnr@naver.com", "12346");
        userRepository.save(user);
        //when
        //then
        assertThatThrownBy(() -> userService.login(LoginRequestDto))
                .isInstanceOf(NotEqualsException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 마이페이지_조회_성공() {
        //given
        User user = User.builder()
                .email("wodnr@naver.com")
                .name("재욱")
                .password("123456")
                .age("재욱99")
                .weight("14kg")
                .height("50cm")
                .gender("남자")
                .build();
        User save = userRepository.save(user);
        //when
        UserPageResponseDto userPageResponseDto = userService.viewMyPage(save.getId());
        //then
        assertThat(userPageResponseDto.getAge()).isEqualTo("재욱99");
        assertThat(userPageResponseDto.getWeight()).isEqualTo("14kg");
        assertThat(userPageResponseDto.getHeight()).isEqualTo("50cm");
    }

    @Test
    void 마이페이지_조회_실패() {
        //given
        User user = User.builder()
                .email("wodnr@naver.com")
                .name("재욱")
                .password("123456")
                .age("35")
                .gender("남자")
                .height("15")
                .weight("20")
                .build();
        userRepository.save(user);
        //when
        //then
        assertThatThrownBy(() -> userService.viewMyPage(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 유저가 존재하지 않습니다.");
    }
}
