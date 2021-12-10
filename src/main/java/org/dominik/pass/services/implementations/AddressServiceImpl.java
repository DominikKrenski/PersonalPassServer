package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AddressDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Address;
import org.dominik.pass.db.repositories.AddressRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public final class AddressServiceImpl implements AddressService {
  private final AddressRepository addressRepository;
  private final AccountService accountService;

  @PersistenceContext private EntityManager em;

  @Autowired
  public AddressServiceImpl(AddressRepository addressRepository, AccountService accountService) {
    this.addressRepository = addressRepository;
    this.accountService = accountService;
  }

  @Override
  public AddressDTO save(String address, String accountPublicId) {
    AccountDTO accountDTO = accountService.findByPublicId(UUID.fromString(accountPublicId));
    Account account = em.merge(Account.fromDTO(accountDTO));
    Address newAddress = new Address(address, account);

    return AddressDTO.fromAddressLazy(addressRepository.save(newAddress));
  }

  @Override
  public List<AddressDTO> getAllUserAddresses(@NonNull UUID accountPublicId) {
    List<Address> addresses = addressRepository.findAllByAccountPublicId(accountPublicId);

    return addresses
        .stream()
        .map(AddressDTO::fromAddressLazy)
        .collect(Collectors.toList());
  }

  @Override
  public AddressDTO getAddress(@NonNull UUID publicId) {
    return addressRepository
        .findByPublicId(publicId)
        .map(AddressDTO::fromAddressLazy)
        .orElseThrow(() -> new NotFoundException("Address with given id does not exist"));
  }

  @Override
  public int updateAddress(@NonNull String address, @NonNull UUID publicId) {
    return addressRepository.updateAddress(address, publicId);
  }

  @Override
  public int deleteAddress(@NonNull UUID publicId) {
    return addressRepository.deleteAddress(publicId);
  }
}
