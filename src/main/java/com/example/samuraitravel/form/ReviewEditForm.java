package com.example.samuraitravel.form;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewEditForm {

	@NotNull
	private Integer id;

	@NotNull(message = "評価を入力してください。")
	@Range(min = 1, max = 5, message = "評価は1～5までを選択してください。")
	private Integer evaluation;

	@NotNull(message = "コメントを入力してください。")
	@Length(max = 500, message = "コメントは500字以内で入力してください。")
	private String review_comment;

}