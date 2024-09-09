package com.example.samuraitravel.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;

	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}

	@GetMapping
	public String index(@PathVariable(name = "houseId") Integer houseId,
			Pageable pageable,
			Model model) {
		// houseIdをHouseに変換
		Favorite favorite = favoriteRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id: " + houseId));

		User user = favorite.getUser();

		// 順序や条件に応じてレビューを取得（特定のhouseごと）
		Page<Favorite> reviewPage = favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);

		model.addAttribute("favorite", favorite);
		model.addAttribute("reviewPage", reviewPage);

		return "review/index";
	}

	@Transactional
	public void create(House house, User user) {
		Favorite favorite = new Favorite();
		favorite.setHouse(house);
		favorite.setUser(user);
		favoriteRepository.save(favorite);
	}

	@Transactional
	public void delete(Integer favoriteId) {
		favoriteRepository.deleteById(favoriteId);
	}

	public boolean existsByHouseAndUser(House house, User user) {
		return favoriteRepository.findByHouseAndUser(house, user) != null;
	}

}
