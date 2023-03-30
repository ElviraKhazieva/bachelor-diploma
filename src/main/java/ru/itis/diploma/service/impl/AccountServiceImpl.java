package ru.itis.diploma.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.diploma.dto.AccountDto;
import ru.itis.diploma.dto.EditAccount;
import ru.itis.diploma.exception.EntityNotFoundException;
import ru.itis.diploma.model.Account;
import ru.itis.diploma.repository.AccountRepository;
import ru.itis.diploma.service.AccountService;

import java.util.List;


@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Account getById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Override
    public List<AccountDto> getAccountsForNewGame() {
        return AccountDto.from(accountRepository.findAllByRoleNot(Account.Role.ADMIN));
    }

    @Override
    public AccountDto updateAccountInfo(Long id, EditAccount account) {
        Account byId = getById(id);
        byId.setFullName(account.getFullName());
        return AccountDto.from(accountRepository.save(byId));
    }

    @Override
    public AccountDto getByEmail(String email) {
        return AccountDto.from(accountRepository.findByEmail(email).orElseThrow(IllegalArgumentException::new));
    }

}
