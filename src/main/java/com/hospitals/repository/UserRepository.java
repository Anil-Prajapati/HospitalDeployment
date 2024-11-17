package com.hospitals.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospitals.model.Users;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<Users, String> {

	public Users findByUserNameOrEmailOrContactNumber(String userName, String email, long contactNumber);
	public Users findByEmailIgnoreCase(String email);
	public Users findByContactNumber(long contactNumber);
	
	public Users findByUserName(String userName);
	
}
