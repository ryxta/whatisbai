package com.whatisbai.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whatisbai.Entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
}
