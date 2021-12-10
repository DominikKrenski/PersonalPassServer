package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.AddressDTO;

import java.util.List;
import java.util.UUID;

public interface AddressService {
  AddressDTO save(String address, String accountPublicId);
  List<AddressDTO> getAllUserAddresses(UUID accountPublicId);
  AddressDTO getAddress(UUID publicId);
  int updateAddress(String address, UUID publicId);
  int deleteAddress(UUID publicId);
}
