package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.DataDTO;
import org.dominik.pass.data.enums.DataType;

import java.util.List;
import java.util.UUID;

public interface DataService {
  DataDTO save(String entry, DataType type,  UUID accountPublicId);
  List<DataDTO> findAllUserDataByType(DataType type, UUID accountPublicId);
  List<DataDTO> findAllUserData(UUID accountPublicId);
  DataDTO findData(UUID publicId);
  void deleteAllUserData(UUID accountPublicId);
  void deleteData(UUID publicId);
  void updateData(String entry, UUID publicId);


}
