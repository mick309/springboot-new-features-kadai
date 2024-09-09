package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses/{houseId}/reviews")

public class ReviewController {

	private final ReviewService reviewService;
	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;

	public ReviewController(ReviewService reviewService, ReviewRepository reviewRepository,
			HouseRepository houseRepository) {
		this.reviewService = reviewService;
		this.reviewRepository = reviewRepository;
		this.houseRepository = houseRepository;

	}

	@GetMapping
	public String index(@PathVariable(name = "houseId") Integer houseId,
			@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
			Model model) {
		// houseIdをHouseに変換
		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id: " + houseId));

		// 順序や条件に応じてレビューを取得（特定のhouseごと）
		Page<Review> reviewPage = reviewRepository.findByHouseOrderByCreatedAtDesc(house, pageable);

		
		model.addAttribute("house", house);
		model.addAttribute("reviewPage", reviewPage);

		return "review/index";
	}

	//新規登録画面でレビューを作成
	@GetMapping("/register")
	public String register(@PathVariable(name = "houseId") Integer houseId, Model model) {
		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id:" + houseId));
		model.addAttribute("house", house);
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		return "review/register";
	}

	//新規登録機能

	@GetMapping("/create")
	public String showCreateForm(@PathVariable(name = "houseId") Integer houseId, Model model) {
		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id:" + houseId));
		model.addAttribute("house", house);
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		return "review/register";
	}

	@PostMapping("/create")
	public String create(@PathVariable(name = "houseId") Integer houseId,
			@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			House house = houseRepository.findById(houseId)
					.orElseThrow(() -> new IllegalArgumentException("Invalid house Id:" + houseId));
			model.addAttribute("house", house);
			return "review/register";
		}

		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id:" + houseId));
		User user = userDetailsImpl.getUser();

		reviewService.create(reviewRegisterForm, house, user);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを登録しました。");

		return "redirect:/houses/{houseId}"; // 登録したら民宿詳細ページへ戻す
	}

	//編集機能
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "houseId") Integer houseId,
			@PathVariable(name = "reviewId") Integer reviewId, Model model) {
		House house = houseRepository.findById(houseId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid house Id: " + houseId));
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid review Id: " + reviewId));

		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getEvaluation(),
				review.getReview_comment());

		model.addAttribute("review", review);
		model.addAttribute("house", house);
		model.addAttribute("reviewEditForm", reviewEditForm);

		return "review/edit";
	}

	//update機能
	@PostMapping("/{reviewId}/update")
	public String update(
			@PathVariable(name = "houseId") Integer houseId,
			@PathVariable(name = "reviewId") Integer reviewId,
			@ModelAttribute @Validated ReviewEditForm reviewEditForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			House house = houseRepository.getReferenceById(houseId);
			Review review = reviewRepository.getReferenceById(reviewId);
			model.addAttribute("review", review);
			model.addAttribute("house", house);
			return "review/edit";
		}

		reviewService.update(reviewEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");

		return "redirect:/houses/{houseId}"; // 編集したら民宿詳細ページへ戻す
	}

	//削除機能
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable(name = "houseId") Integer houseId,
			@PathVariable(name = "reviewId") Integer reviewId,
			RedirectAttributes redirectAttributes) {
		reviewRepository.deleteById(reviewId);

		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

		return "redirect:/houses/{houseId}";
	}
}
