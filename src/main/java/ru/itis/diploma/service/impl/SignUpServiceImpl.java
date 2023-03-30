package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.SignUpForm;
import ru.itis.diploma.model.Account;
import ru.itis.diploma.repository.AccountRepository;
import ru.itis.diploma.service.SignUpService;

@Service
@RequiredArgsConstructor
public class SignUpServiceImpl implements SignUpService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signUp(SignUpForm form) {
        Account account = Account.builder()
                .fullName(form.getFullName())
                .email(form.getEmail())//.toLowerCase(Locale.ROOT)
                .password(passwordEncoder.encode(form.getPassword()))
                .role(Account.Role.USER)
                .build();

        accountRepository.save(account);
    }

}
