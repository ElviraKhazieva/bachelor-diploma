package ru.itis.diploma.service;

import ru.itis.diploma.dto.AccountDto;
import ru.itis.diploma.dto.EditAccount;
import ru.itis.diploma.model.Account;

import java.util.List;

public interface AccountService {

    Account getById(Long id);

    List<AccountDto> getAccountsForNewGame();

    AccountDto updateAccountInfo(Long id, EditAccount account);

    AccountDto getByEmail(String email);

}
