package com.ecommerce.bkd.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.userAddress.UserAddressDTO;
import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.exceptions.UnauthorizedException;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.models.UserAddress;
import com.ecommerce.bkd.repositories.UserAddressRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.services.UserAddressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserAddressDTO> getUserAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        return addresses.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void unsetOtherPrimaryAddresses(Long userId, Long excludeAddressId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        for (UserAddress addr : addresses) {
            if (!addr.getId().equals(excludeAddressId) && addr.isPrimary()) {
                System.out.println("Unsetting primary flag for address ID: " + addr.getId());
                addr.setPrimary(false);
                userAddressRepository.save(addr);
            }
        }
    }

    @Override
    @Transactional
    public UserAddressDTO addAddress(UserAddressDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + dto.getUserId()));

        UserAddress address = new UserAddress();
        address.setUser(user);
        mapDTOToEntity(dto, address);

        if (address.isPrimary()) {
            System.out.println("New address set as primary, unsetting others for user ID: " + user.getId());
            unsetOtherPrimaryAddresses(user.getId(), null);
        }

        UserAddress savedAddress = userAddressRepository.save(address);
        System.out.println("Address added successfully: " + savedAddress.getId());
        return toDTO(savedAddress);
    }

    @Override
    @Transactional
    public UserAddressDTO updateAddress(UserAddressDTO dto) {
        UserAddress address = userAddressRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + dto.getId()));

        if (!address.getUser().getId().equals(dto.getUserId())) {
            throw new UnauthorizedException("Cannot update address of another user");
        }

        boolean wasPrimary = address.isPrimary();
        mapDTOToEntity(dto, address);

        if (address.isPrimary() && !wasPrimary) {
            System.out.println("Address ID " + address.getId() + " set as primary, unsetting others");
            unsetOtherPrimaryAddresses(address.getUser().getId(), address.getId());
        }

        UserAddress updatedAddress = userAddressRepository.save(address);
        System.out.println("Address updated successfully: " + updatedAddress.getId());
        return toDTO(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + id));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot delete address of another user");
        }

        userAddressRepository.delete(address);
        System.out.println("Address deleted successfully: " + id);
    }

    private UserAddressDTO toDTO(UserAddress address) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUser().getId());
        dto.setAddress1(address.getAddress1());
        dto.setAddress2(address.getAddress2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setPincode(address.getPincode());
        dto.setType(address.getDestinationType());
        dto.setServiceable(address.isServiceable());
        dto.setServiceabilityMessage(address.getServiceabilityMessage());
        dto.setPrimary(address.isPrimary());
        return dto;
    }

    private void mapDTOToEntity(UserAddressDTO dto, UserAddress address) {
        address.setAddress1(dto.getAddress1());
        address.setAddress2(dto.getAddress2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setPincode(dto.getPincode());
        // address.setType(dto.getType());
        address.setDestinationType(dto.getType());
        // address.setServiceable(dto.isServiceable());
        // address.setServiceabilityMessage(dto.getServiceabilityMessage());
        address.setPrimary(dto.isPrimary());
    }
}