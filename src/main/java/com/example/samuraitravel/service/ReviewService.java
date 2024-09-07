package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.ReviewRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {

	private ReviewRepository reviewRepository;

	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	@Transactional
	public void create(ReviewRegisterForm form, House house, User user) {
		Review review = new Review();
		review.setEvaluation(form.getEvaluation());
		review.setReview_comment(form.getReview_comment());
		review.setHouse(house);
		review.setUser(user);
		reviewRepository.save(review);
	}

	
	public boolean isReviewDone(User user, House house) {
		return reviewRepository.findByHouseAndUser(house, user) != null;

	}

	@Transactional
	public void update(ReviewEditForm reviewEditForm) {
		Review review = reviewRepository.getReferenceById(reviewEditForm.getId());

		review.setEvaluation(reviewEditForm.getEvaluation());
		review.setReview_comment(reviewEditForm.getReview_comment());

		reviewRepository.save(review);

	}

}
