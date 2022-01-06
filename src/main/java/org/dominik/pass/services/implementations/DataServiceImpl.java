package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.DataDTO;
import org.dominik.pass.data.dto.UpdateDataDTO;
import org.dominik.pass.data.dto.UpdatePasswordDTO;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Data;
import org.dominik.pass.db.repositories.DataRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@Service
public class DataServiceImpl implements DataService {
  private final DataRepository dataRepository;
  private final AccountService accountService;

  @PersistenceContext
  private EntityManager em;

  @Autowired
  public DataServiceImpl(DataRepository dataRepository, AccountService accountService) {
    this.dataRepository = dataRepository;
    this.accountService = accountService;
  }

  @Override
  @Transactional
  public DataDTO save(@NonNull String entry, @NonNull DataType type, @NonNull UUID accountPublicId) {
    AccountDTO accountDTO = accountService.findByPublicId(accountPublicId);
    Account account = em.merge(Account.fromDTO(accountDTO));
    Data data = new Data(entry, type, account);

    return DataDTO.fromData(dataRepository.save(data));
  }

  @Override
  public List<DataDTO> findAllUserDataByType(DataType type, UUID accountPublicId) {
    List<Data> data = dataRepository.findAllByTypeAndAccountPublicId(type, accountPublicId);

    return data
        .stream()
        .map(DataDTO::fromData)
        .toList();
  }

  @Override
  public List<DataDTO> findAllUserData(@NonNull UUID accountPublicId) {
    List<Data> data = dataRepository.findAllByAccountPublicId(accountPublicId);

    return data
        .stream()
        .map(DataDTO::fromData)
        .toList();
  }

  @Override
  public DataDTO findData(@NonNull UUID publicId) {
    return dataRepository
        .findByPublicId(publicId)
        .map(DataDTO::fromData)
        .orElseThrow(() -> new NotFoundException("Data with given id does not exist"));
  }

  @Override
  @Transactional
  public void deleteAllUserData(@NonNull UUID accountPublicId) {
    dataRepository.deleteByAccountPublicId(accountPublicId);
  }

  @Override
  @Transactional
  public void deleteData(@NonNull UUID publicId) {
    int deleted = dataRepository.deleteData(publicId);

    if (deleted != 1)
      throw new NotFoundException("Data with given id does not exist");
  }

  @Override
  @Transactional
  public void updateData(@NonNull String entry, @NonNull UUID publicId) {
    int updated = dataRepository.updateData(entry, publicId);

    if (updated != 1)
      throw new NotFoundException("Data with given id does not exist");
  }

  @Override
  @Transactional
  public void updateAllData(@NonNull UUID accountPublicId, @NonNull UpdatePasswordDTO passwordDTO) {
    accountService.updatePassword(accountPublicId, passwordDTO.getPassword(), passwordDTO.getSalt());

    List<UpdateDataDTO> data = passwordDTO.getData();

    if (data == null)
      return;

    data.forEach(item -> updateData(item.getEntry(), item.getPublicId()));
  }
}
