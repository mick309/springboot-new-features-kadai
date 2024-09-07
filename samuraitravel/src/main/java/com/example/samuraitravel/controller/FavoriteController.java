package com.example.samuraitravel.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping
public class FavoriteController {

	private final FavoriteRepository favoriteRepository;
	private final HouseRepository houseRepository;
	private final FavoriteService favoriteService;

	public FavoriteController(FavoriteRepository favoriteRepository,
			HouseRepository houseRepository,
			FavoriteService favoriteService) {
		this.favoriteRepository = favoriteRepository;
		this.houseRepository = houseRepository;
		this.favoriteService = favoriteService;
	}

	// お気に入り一覧を表示
	@GetMapping("/favorites")
	public String favorite(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {

		Integer userId = Integer.valueOf(userDetailsImpl.getUser().getId());
		List<Favorite> favorites = favoriteRepository.findByUserId(userId);

		List<Integer> houseList = new ArrayList<>();
		for (Favorite favorite : favorites) {
			Integer houseId = favorite.getHouse().getId();
			houseList.add(houseId);
		}

		Page<House> housePages;
		if (houseList.isEmpty()) {
			housePages = Page.empty(pageable);
		} else {
			housePages = houseRepository.findByIdIn(houseList, pageable);
		}

		model.addAttribute("housePages", housePages);

		return "/favorites/index";
	}

	@PostMapping("/houses/{houseId}/favorites/create")
	public String create(@PathVariable(name = "houseId") Integer houseId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

		House house = houseRepository.getReferenceById(houseId);
		User user = userDetailsImpl.getUser();

		favoriteService.create(house, user);

		return "redirect:/houses/{houseId}";
	}

	@PostMapping("/houses/{houseId}/favorites/{favoriteId}/delete")
	public String delete(@PathVariable(name = "favoriteId") Integer favoriteId, RedirectAttributes redirectAttributes) {
		favoriteRepository.deleteById(favoriteId);

		return "redirect:/houses/{houseId}";
	}

}
