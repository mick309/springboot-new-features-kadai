package com.example.samuraitravel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

	Favorite findByHouseAndUser(House house, User user);

	List<Favorite> findByUserId(Integer userId);

	Page<House> findHousesByUser(User user, Pageable pageable);

}
