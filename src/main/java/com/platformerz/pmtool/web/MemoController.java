package com.platformerz.pmtool.web;

import com.platformerz.pmtool.domain.Memo;
import com.platformerz.pmtool.repository.MemoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/memo")
public class MemoController {

	private final MemoRepository memoRepository;

	public MemoController(MemoRepository memoRepository) {
		this.memoRepository = memoRepository;
	}

	@GetMapping
	public MemoResponse get(@PathVariable Long projectId) {
		return memoRepository.findById(projectId)
			.map(memo -> new MemoResponse(memo.getContent()))
			.orElseGet(() -> new MemoResponse(""));
	}

	@PutMapping
	public MemoResponse update(@PathVariable Long projectId, @RequestBody MemoRequest request) {
		Memo memo = memoRepository.findById(projectId).orElseGet(() -> new Memo(projectId));
		memo.setContent(request.content());
		memoRepository.save(memo);
		return new MemoResponse(memo.getContent());
	}

}
