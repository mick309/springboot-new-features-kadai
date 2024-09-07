package com.example.samuraitravel.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses")
public class HouseController {

	private final HouseRepository houseRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final FavoriteService favoriteService;
	private final FavoriteRepository favoriteRepository;

	public HouseController(HouseRepository houseRepository, ReviewRepository reviewRepository,
			ReviewService reviewService, FavoriteService favoriteService, FavoriteRepository favoriteRepository) {
		this.houseRepository = houseRepository;
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.favoriteService = favoriteService;
		this.favoriteRepository = favoriteRepository;
	}

	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "area", required = false) String area,
			@RequestParam(name = "price", required = false) Integer price,
			@RequestParam(name = "order", required = false) String order,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		Page<House> housePage;

		if (keyword != null && !keyword.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			} else {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			}
		} else if (area != null && !area.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
			} else {
				housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
			}
		} else if (price != null) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
			} else {
				housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		} else {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
			} else {
				housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);
			}
		}

		model.addAttribute("housePage", housePage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("area", area);
		model.addAttribute("price", price);
		model.addAttribute("order", order);

		return "/houses/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		House house = houseRepository.getReferenceById(id);
		User user = userDetailsImpl != null ? userDetailsImpl.getUser() : null;
		Favorite favorite = null;
		boolean isFavorite = user != null && favoriteService.existsByHouseAndUser(house, user);

		boolean hasUserAlreadyReviewed = false;
		if (userDetailsImpl != null) {
			hasUserAlreadyReviewed = reviewService.isReviewDone(user, house);
			if (isFavorite) {
				favorite = favoriteRepository.findByHouseAndUser(house, user);
			}
		}

		// 対象の民宿のコメントを6件表示する
		List<Review> newReviews = reviewRepository.findTop6ByHouseOrderByCreatedAtDesc(house);
		long totalReviewCount = reviewRepository.countByHouse(house);

		model.addAttribute("house", house);
		model.addAttribute("isFavorite", isFavorite);
		model.addAttribute("user", user);
		model.addAttribute("reservationInputForm", new ReservationInputForm());
		model.addAttribute("newReviews", newReviews);
		model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
		model.addAttribute("totalReviewCount", totalReviewCount);
		model.addAttribute("favorite", favorite);

		return "/houses/show";
	}

	@PostMapping("/{houseId}/favorites/add")
	public String addFavorite(@PathVariable(name = "houseId") Integer houseId,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {
		House house = houseRepository.getReferenceById(houseId);
		User user = userDetailsImpl.getUser();

		if (!favoriteService.existsByHouseAndUser(house, user)) {
			favoriteService.create(house, user);
		}

		redirectAttributes.addAttribute("houseId", houseId);
		return "redirect:/houses/{houseId}";
	}

	@PostMapping("/{houseId}/favorites/remove/{favoriteId}")
	public String removeFavorite(@PathVariable(name = "favoriteId") Integer favoriteId,
			@PathVariable(name = "houseId") Integer houseId,
			RedirectAttributes redirectAttributes) {
		favoriteService.delete(favoriteId);

		redirectAttributes.addAttribute("houseId", houseId);
		return "redirect:/houses/{houseId}";
	}

	@GetMapping("/index")
	public String index(Model model) {
		return "index";
	}

}
